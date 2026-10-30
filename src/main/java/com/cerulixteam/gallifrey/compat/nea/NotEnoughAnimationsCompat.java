package com.cerulixteam.gallifrey.compat.nea;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Set;

/**
 * Not Enough Animations (tr7zw) support, without depending on it. NEA has a list of items players
 * hold up in front of them and look at (compasses, clocks, lanterns). Adding the sonic screwdriver
 * and the Vortex Manipulator means you hold the sonic out and pointed instead of at your side.
 *
 * The items are added to NEA's in-memory config once the game has started (after NEA has loaded
 * its config, and before its first refresh). Anyone who'd rather not can remove them in NEA's own
 * settings. Client-only. Written against NEA for 1.20.1; if it changes, this does nothing.
 */
public final class NotEnoughAnimationsCompat {
    private NotEnoughAnimationsCompat() {}

    private static final String[] HELD_UP = {
            "gallifrey:sonic_screwdriver",
            "gallifrey:vortex_manipulator"
    };

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("notenoughanimations")) return;
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                Class<?> loader = Class.forName("dev.tr7zw.notenoughanimations.NEAnimationsLoader");
                Object config = loader.getField("config").get(null);
                if (config == null) return;
                @SuppressWarnings("unchecked")
                Set<String> holding = (Set<String>) config.getClass().getField("holdingItems").get(config);
                for (String id : HELD_UP) holding.add(id);
                GallifreyMod.LOGGER.info("[Gallifrey] Not Enough Animations found: sonics are held up and pointed");
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                GallifreyMod.LOGGER.warn("[Gallifrey] Not Enough Animations is installed but wasn't what Gallifrey expected", e);
            }
        });
    }
}
