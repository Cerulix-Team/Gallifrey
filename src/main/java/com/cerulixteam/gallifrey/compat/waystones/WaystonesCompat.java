package com.cerulixteam.gallifrey.compat.waystones;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Waystones support: you can't teleport into a TARDIS with a waystone (or sharestone, warp
 * scroll, warp stone...), so a waystone placed inside one can't be used to skip its doors.
 * Waystones inside a TARDIS still work for leaving it, and the TARDIS's own doors and travel are
 * untouched.
 *
 * It cancels Waystones' own "about to teleport" event, so Waystones shows its normal "teleport
 * cancelled" feedback and nothing is charged.
 *
 * No compile-time dependency: Waystones' event is subscribed to through Balm (the library
 * Waystones runs on) by name, and none of this runs without Waystones. Written against
 * Waystones 14.1 for 1.20.1. If it changes, this logs a warning and switches itself off.
 */
public final class WaystonesCompat {
    private WaystonesCompat() {}

    private static final RegistryKey<World> TARDIS =
            RegistryKey.of(RegistryKeys.WORLD, new Identifier(GallifreyMod.MOD_ID, "tardis"));

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("waystones")) return;
        try {
            Class<?> pre = Class.forName("net.blay09.mods.waystones.api.WaystoneTeleportEvent$Pre");
            Method getContext = pre.getMethod("getContext");
            Method getDestination = pre.getMethod("getDestination");
            Method setCanceled = pre.getMethod("setCanceled", boolean.class);
            Method getLevel = Class.forName("net.blay09.mods.waystones.api.TeleportDestination").getMethod("getLevel");
            Method getEntity = Class.forName("net.blay09.mods.waystones.api.IWaystoneTeleportContext").getMethod("getEntity");

            Consumer<Object> handler = event -> {
                try {
                    Object destination = getDestination.invoke(event);
                    if (destination == null) return;
                    Object level = getLevel.invoke(destination);
                    if (!(level instanceof ServerWorld world) || !TARDIS.equals(world.getRegistryKey())) return;

                    setCanceled.invoke(event, true);
                    Object entity = getEntity.invoke(getContext.invoke(event));
                    if (entity instanceof PlayerEntity player) {
                        player.sendMessage(Text.literal("§cThe TARDIS won't let a waystone take you inside. Use the door."), true);
                    }
                } catch (ReflectiveOperationException | RuntimeException e) {
                    GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't check a waystone teleport", e);
                }
            };

            Object events = Class.forName("net.blay09.mods.balm.api.Balm").getMethod("getEvents").invoke(null);
            Class.forName("net.blay09.mods.balm.api.event.BalmEvents")
                    .getMethod("onEvent", Class.class, Consumer.class)
                    .invoke(events, pre, handler);
            GallifreyMod.LOGGER.info("[Gallifrey] Waystones found: waystones can't teleport into a TARDIS");
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Waystones is installed but wasn't what Gallifrey expected; waystone compat is off", e);
        }
    }
}
