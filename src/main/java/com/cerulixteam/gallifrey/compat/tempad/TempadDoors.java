package com.cerulixteam.gallifrey.compat.tempad;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.sonic.SonicMode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tempad support (the Loki-style time door mod).
 *
 *  - TARDIS protection: a time door whose destination is the TARDIS dimension (gallifrey:tardis)
 *    is cut off and closes (with Tempad's own closing animation) before anything can go through,
 *    and nearby players are told why. Checked every tick, so it also catches doors opened any
 *    other way. Doors opened inside the TARDIS leading out are left alone, and the TARDIS's own
 *    doors and travel are untouched.
 *  - Sonic screwdriver: point it at an open time door.
 *      Overload mode: the door closes (Tempad's own closing animation).
 *      Scan mode: tells you the saved location's name, dimension and coordinates.
 *
 * No compile-time dependency: the time door is found by its entity id and Tempad's methods are
 * looked up by name, so none of this runs without Tempad. Written against Tempad 2.3.4 for
 * 1.20.1. If Tempad changes, this logs a warning and switches itself off rather than crashing.
 */
public final class TempadDoors {
    private TempadDoors() {}

    private static final String MOD_ID = "tempad";
    private static final Identifier DOOR_ID = new Identifier(MOD_ID, "timedoor");
    private static final RegistryKey<World> TARDIS =
            RegistryKey.of(RegistryKeys.WORLD, new Identifier(GallifreyMod.MOD_ID, "tardis"));

    private static boolean active = false;
    private static Method getLocation;     // TimedoorEntity#getLocation() -> LocationData (null on the far end)
    private static Method setLocation;     // TimedoorEntity#setLocation(LocationData)
    private static Method setClosingTime;  // TimedoorEntity#setClosingTime(int): closes at that tick, then animates shut
    private static Method getLevelKey;     // LocationData#getLevelKey()
    private static Method getBlockPos;     // LocationData#getBlockPos()
    private static Method getName;         // LocationData#getName()

    /** Time doors currently loaded on the server. */
    private static final Set<Entity> DOORS = Collections.newSetFromMap(new WeakHashMap<>());

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            Class<?> door = Class.forName("me.codexadrian.tempad.common.entity.TimedoorEntity");
            Class<?> location = Class.forName("me.codexadrian.tempad.common.data.LocationData");
            getLocation = door.getMethod("getLocation");
            setLocation = door.getMethod("setLocation", location);
            setClosingTime = door.getMethod("setClosingTime", int.class);
            getLevelKey = location.getMethod("getLevelKey");
            getBlockPos = location.getMethod("getBlockPos");
            getName = location.getMethod("getName");

            ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
                if (isDoor(entity)) DOORS.add(entity);
            });
            // START of the world tick: before the door's own tick, which is when it teleports.
            ServerTickEvents.START_WORLD_TICK.register(TempadDoors::checkDoors);

            active = true;
            GallifreyMod.LOGGER.info("[Gallifrey] Tempad found: time doors can't enter the TARDIS, sonics can close them");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            GallifreyMod.LOGGER.warn("[Gallifrey] Tempad is installed but wasn't what Gallifrey expected; time door compat is off", e);
        }
    }

    private static boolean isDoor(Entity entity) {
        return DOOR_ID.equals(Registries.ENTITY_TYPE.getId(entity.getType()));
    }

    /** Starts Tempad's own closing animation; the door disappears once it finishes. */
    private static void close(Entity door) throws ReflectiveOperationException {
        setClosingTime.invoke(door, door.age);
    }

    // ------------------------------------------------------------------
    // Keeping time doors out of the TARDIS
    // ------------------------------------------------------------------

    private static void checkDoors(ServerWorld world) {
        if (DOORS.isEmpty()) return;
        for (Entity door : DOORS.toArray(new Entity[0])) {
            if (door.isRemoved()) {
                DOORS.remove(door);
                continue;
            }
            if (door.getWorld() != world) continue;
            try {
                Object location = getLocation.invoke(door);
                if (location == null || !TARDIS.equals(getLevelKey.invoke(location))) continue;

                setLocation.invoke(door, (Object) null); // cut the link so nothing goes through while it closes
                close(door);
                DOORS.remove(door);
                world.playSound(null, door.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(),
                        SoundCategory.BLOCKS, 1.0F, 0.6F);
                for (PlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(door) < 32 * 32)) {
                    player.sendMessage(Text.literal("§cThe TARDIS won't let a time door in. Use the door."), true);
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Sonic screwdriver
    // ------------------------------------------------------------------

    /**
     * Handles a sonic aimed at a time door. Returns true if the target was a time door and this
     * mode does something to it (so the sonic shouldn't also run its normal action).
     */
    public static boolean handleSonic(PlayerEntity player, HitResult target, SonicMode mode) {
        if (!active || !(target instanceof EntityHitResult hit) || !isDoor(hit.getEntity())) return false;
        Entity door = hit.getEntity();
        try {
            switch (mode) {
                case OVERLOAD -> {
                    close(door);
                    player.sendMessage(Text.literal("§bSONIC: Time door closing."), true);
                    return true;
                }
                case SCAN -> {
                    Object location = getLocation.invoke(door);
                    if (location == null) {
                        player.sendMessage(Text.literal("§bSONIC: This is the far end of a time door."), true);
                    } else {
                        Object key = getLevelKey.invoke(location);
                        BlockPos pos = (BlockPos) getBlockPos.invoke(location);
                        String dim = key instanceof RegistryKey<?> k ? k.getValue().toString() : "unknown";
                        player.sendMessage(Text.literal(String.format("§bSONIC: Time door to §f%s §b(%s) at §f%d, %d, %d",
                                getName.invoke(location), dim, pos.getX(), pos.getY(), pos.getZ())), true);
                    }
                    return true;
                }
                default -> {
                    return false;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
