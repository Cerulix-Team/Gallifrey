package com.cerulixteam.gallifrey.compat.firstperson;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * First-person Model (tr7zw) support, without depending on it. That mod draws your own body in
 * first person and asks other mods not to draw anything on the head while it does, or it blocks
 * the camera. Our head accessories (fez slot, sonic shades slot, GeckoLib hats like the Omega
 * Helmet) check {@link #hideHeadwear()} and skip themselves.
 *
 * Uses dev.tr7zw.firstperson.api.FirstPersonAPI#isRenderingPlayer by reflection; client-only.
 */
public final class FirstPersonCompat {
    private FirstPersonCompat() {}

    private static final MethodHandle IS_RENDERING_PLAYER = find();

    private static MethodHandle find() {
        if (!FabricLoader.getInstance().isModLoaded("firstperson")) return null;
        try {
            Class<?> api = Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI");
            return MethodHandles.publicLookup().findStatic(api, "isRenderingPlayer", MethodType.methodType(boolean.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** True while First-person Model is drawing your own body, when head items must not be drawn. */
    public static boolean hideHeadwear() {
        if (IS_RENDERING_PLAYER == null) return false;
        try {
            return (boolean) IS_RENDERING_PLAYER.invokeExact();
        } catch (Throwable t) {
            return false;
        }
    }
}
