package com.timelordmod.gallifrey.compat.ait;

import com.timelordmod.gallifrey.GallifreyMod;
import com.timelordmod.gallifrey.util.GallifreyDimensionTravelAccess;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * Takes Lost Reality and Gallifrey's TARDIS interior dimension out of AiT's dimension lists, so
 * they no longer show up when you cycle a TARDIS's dimension control (or in the environment
 * projector, or as a random destination).
 *
 * AiT builds these lists once, when the server starts, from every loaded dimension minus its
 * travel blacklist. This runs straight after that (in a later phase of the same event) and removes
 * them from the lists AiT keeps. Travel into Lost Reality is still refused by
 * GallifreyDimensionTravelAccess either way; this just stops it being offered.
 *
 * No compile-time dependency: AiT's WorldUtil is found by name. Written against AiT 1.3.0 for
 * 1.20.1. If AiT changes, this logs a warning and does nothing.
 */
public final class AitTravelWorlds {
    private AitTravelWorlds() {}

    /** Gallifrey dimensions an AiT TARDIS should never be offered. */
    public static final Set<Identifier> HIDDEN = Set.of(
            GallifreyDimensionTravelAccess.LOST_REALITY,
            new Identifier(GallifreyMod.MOD_ID, "tardis"));

    private static final Identifier AFTER_AIT = new Identifier(GallifreyMod.MOD_ID, "after_ait");
    private static final String[] WORLD_UTIL = {
            "dev.amble.ait.core.util.WorldUtil",
            "loqor.ait.core.util.WorldUtil",
            "mdteam.ait.core.util.WorldUtil"
    };

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("ait")) return;

        Class<?> worldUtil = null;
        for (String name : WORLD_UTIL) {
            try {
                worldUtil = Class.forName(name);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (worldUtil == null) {
            GallifreyMod.LOGGER.warn("[Gallifrey] AiT is installed but its WorldUtil wasn't found; Lost Reality may still be listed in AiT's dimension control.");
            return;
        }

        Method travel = find(worldUtil, "getTravelWorlds");
        Method projector = find(worldUtil, "getProjectorWorlds");
        if (travel == null) {
            GallifreyMod.LOGGER.warn("[Gallifrey] AiT's WorldUtil has no getTravelWorlds; Lost Reality may still be listed in AiT's dimension control.");
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_AIT);
        ServerLifecycleEvents.SERVER_STARTED.register(AFTER_AIT, server -> {
            removeHidden(travel);
            if (projector != null) removeHidden(projector);
        });
    }

    private static Method find(Class<?> owner, String name) {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void removeHidden(Method getter) {
        try {
            Object result = getter.invoke(null);
            if (!(result instanceof Collection<?> worlds)) return;
            boolean removed = worlds.removeIf(world -> world instanceof ServerWorld serverWorld
                    && HIDDEN.contains(serverWorld.getRegistryKey().getValue()));
            if (removed) GallifreyMod.LOGGER.info("[Gallifrey] Hid Lost Reality and the Gallifrey TARDIS from AiT's {}.", getter.getName());
        } catch (UnsupportedOperationException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] AiT's {} can't be changed; Lost Reality may still be listed.", getter.getName());
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't hide Lost Reality from AiT's {}: {}", getter.getName(), e.toString());
        }
    }
}
