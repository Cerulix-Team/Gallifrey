package com.cerulixteam.gallifrey.compat.pehkui;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;

import java.lang.reflect.Method;

/**
 * Pehkui compat, without depending on it: when you step through a TARDIS door you arrive
 * squeezed small and grow back to full size over a second, as if you've just come through
 * something much smaller than the room you're standing in.
 *
 * Only Pehkui's public API is used (virtuoel.pehkui.api), looked up by reflection. If Pehkui
 * isn't installed, or its API changes, this quietly does nothing.
 */
public final class PehkuiCompat {
    private PehkuiCompat() {}

    private static final float ARRIVAL_SCALE = 0.3f;
    private static final int GROW_TICKS = 20;

    private static boolean ready;
    private static Object baseType;
    private static Method getScaleData, setScale, setTargetScale, setScaleTickDelay;

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("pehkui")) return;
        try {
            Class<?> types = Class.forName("virtuoel.pehkui.api.ScaleTypes");
            Class<?> type = Class.forName("virtuoel.pehkui.api.ScaleType");
            Class<?> data = Class.forName("virtuoel.pehkui.api.ScaleData");
            baseType = types.getField("BASE").get(null);
            getScaleData = type.getMethod("getScaleData", Entity.class);
            setScale = data.getMethod("setScale", float.class);
            setTargetScale = data.getMethod("setTargetScale", float.class);
            setScaleTickDelay = data.getMethod("setScaleTickDelay", int.class);
            ready = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            GallifreyMod.LOGGER.warn("Pehkui is installed but its API wasn't found; TARDIS door scaling is off", e);
        }
    }

    /** Call right after an entity has been moved into a TARDIS interior. */
    public static void squeezeThroughDoor(Entity entity) {
        if (!ready) return;
        try {
            Object data = getScaleData.invoke(baseType, entity);
            setScale.invoke(data, ARRIVAL_SCALE);
            setScaleTickDelay.invoke(data, GROW_TICKS);
            setTargetScale.invoke(data, 1.0f);
        } catch (ReflectiveOperationException | RuntimeException e) {
            ready = false;
            GallifreyMod.LOGGER.warn("Pehkui scaling failed; turning TARDIS door scaling off", e);
        }
    }
}
