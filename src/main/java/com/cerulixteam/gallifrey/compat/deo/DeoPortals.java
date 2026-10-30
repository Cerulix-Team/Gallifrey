package com.cerulixteam.gallifrey.compat.deo;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.sonic.SonicMode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Deo's Portal Gun Mod support.
 *
 *  - TARDIS protection: a portal whose destination is the TARDIS dimension (gallifrey:tardis)
 *    closes as soon as it opens, before anything can go through, and nearby players are told why.
 *    Checked every tick, so re-aiming an open portal at the TARDIS doesn't work either. Portals
 *    opened inside the TARDIS leading out are left alone. This only looks at Deo's portals, so
 *    the TARDIS's own doors and travel are untouched.
 *  - Sonic screwdriver: point it at an open portal.
 *      Overload mode: the portal collapses (Deo's own closing timer, so it shuts within half a second).
 *      Scan mode: tells you which dimension and coordinates the portal leads to.
 *
 * The recipe side (Gallifrey circuits standing in for Deo's modules) is plain data:
 * data/gallifrey/recipes/compat/deos_portal_gun/.
 *
 * No compile-time dependency: Deo's portal is found by its entity id and read through its fields
 * by name, and none of this runs without the mod. Written against Deo's Portal Gun 1.1.1 for
 * 1.20.1. If the mod changes, this logs a warning and switches itself off rather than crashing.
 */
public final class DeoPortals {
    private DeoPortals() {}

    private static final String MOD_ID = "deos-portal-gun-mod";
    private static final Identifier PORTAL_ID = new Identifier(MOD_ID, "portal");
    private static final RegistryKey<World> TARDIS =
            RegistryKey.of(RegistryKeys.WORLD, new Identifier(GallifreyMod.MOD_ID, "tardis"));
    /** Deo's portals start shutting once their countdown is at or below this. */
    private static final int CLOSING_TICKS = 10;

    private static boolean active = false;
    private static Field destWorld;     // RegistryKey<World> destWorld
    private static Field xcoord, ycoord, zcoord;
    private static Field tickcounter;   // counts down; the portal closes at 10 and vanishes at 0

    /** Portals currently loaded on the server. */
    private static final Set<Entity> PORTALS = Collections.newSetFromMap(new WeakHashMap<>());

    public static boolean isActive() {
        return active;
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            Class<?> portal = Class.forName("net.deo.dpgm.entities.custom.PortalEntity");
            destWorld = portal.getField("destWorld");
            xcoord = portal.getField("xcoord");
            ycoord = portal.getField("ycoord");
            zcoord = portal.getField("zcoord");
            tickcounter = portal.getField("tickcounter");

            ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
                if (isPortal(entity)) PORTALS.add(entity);
            });
            // START of the world tick: after player packets (which can re-aim a portal) and before
            // the portal's own tick (which teleports whatever is standing in it).
            ServerTickEvents.START_WORLD_TICK.register(DeoPortals::checkPortals);

            active = true;
            GallifreyMod.LOGGER.info("[Gallifrey] Deo's Portal Gun found: portals can't enter the TARDIS, sonics can close them");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            GallifreyMod.LOGGER.warn("[Gallifrey] Deo's Portal Gun is installed but wasn't what Gallifrey expected; portal compat is off", e);
        }
    }

    private static boolean isPortal(Entity entity) {
        return PORTAL_ID.equals(net.minecraft.registry.Registries.ENTITY_TYPE.getId(entity.getType()));
    }

    // ------------------------------------------------------------------
    // Keeping portals out of the TARDIS
    // ------------------------------------------------------------------

    private static void checkPortals(ServerWorld world) {
        if (PORTALS.isEmpty()) return;
        for (Entity portal : PORTALS.toArray(new Entity[0])) {
            if (portal.isRemoved()) {
                PORTALS.remove(portal);
                continue;
            }
            if (portal.getWorld() != world) continue;
            try {
                if (TARDIS.equals(destWorld.get(portal))) {
                    portal.discard();
                    PORTALS.remove(portal);
                    world.playSound(null, portal.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(),
                            SoundCategory.BLOCKS, 1.0F, 0.6F);
                    for (PlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(portal) < 32 * 32)) {
                        player.sendMessage(Text.literal("§cThe TARDIS won't let a portal in. Use the door."), true);
                    }
                }
            } catch (IllegalAccessException e) {
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Sonic screwdriver
    // ------------------------------------------------------------------

    /**
     * Handles a sonic aimed at one of Deo's portals. Returns true if the target was a portal and
     * this mode does something to it (so the sonic shouldn't also run its normal action).
     */
    public static boolean handleSonic(PlayerEntity player, HitResult target, SonicMode mode) {
        if (!active || !(target instanceof EntityHitResult hit) || !isPortal(hit.getEntity())) return false;
        Entity portal = hit.getEntity();
        try {
            switch (mode) {
                case OVERLOAD -> {
                    int left = tickcounter.getInt(portal);
                    if (left > CLOSING_TICKS) tickcounter.setInt(portal, CLOSING_TICKS);
                    player.sendMessage(Text.literal("§bSONIC: Portal collapsing."), true);
                    return true;
                }
                case SCAN -> {
                    Object dest = destWorld.get(portal);
                    String where = dest instanceof RegistryKey<?> key ? key.getValue().toString() : "unknown";
                    player.sendMessage(Text.literal(String.format("§bSONIC: Portal to §f%s §bat §f%d, %d, %d",
                            where,
                            (int) Math.floor(xcoord.getDouble(portal)),
                            (int) Math.floor(ycoord.getDouble(portal)),
                            (int) Math.floor(zcoord.getDouble(portal)))), true);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        } catch (IllegalAccessException | RuntimeException e) {
            return false;
        }
    }
}
