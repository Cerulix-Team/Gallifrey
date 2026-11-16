package com.cerulixteam.gallifrey.compat.dehydration;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SpaceSuits;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Dehydration (Globox1997) support, without depending on it.
 *
 *  - Maple syrup quenches a little thirst: data/dehydration/hydration_items/gallifrey.json.
 *  - Hot, dry worlds dry you out faster: on Mars and Skaro you lose water about half as fast
 *    again as normal, unless you're sealed in a full space suit.
 *
 * Dehydration's player thirst is reached through its ThirstManagerAccess interface by reflection.
 * Written against Dehydration 1.3.x for 1.20.1; if that changes, this logs a warning and stops.
 */
public final class DehydrationCompat {
    private DehydrationCompat() {}

    /** Extra dehydration per second. Dehydration takes a point of thirst every 4.0. */
    private static final float EXTRA_PER_SECOND = 0.05F;
    private static final Set<String> DRY_WORLDS = Set.of("mars", "skaro");

    private static Method getThirstManager, addDehydration;

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("dehydration")) return;
        try {
            Class<?> access = Class.forName("net.dehydration.access.ThirstManagerAccess");
            Class<?> manager = Class.forName("net.dehydration.thirst.ThirstManager");
            getThirstManager = access.getMethod("getThirstManager");
            addDehydration = manager.getMethod("addDehydration", float.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Dehydration is installed but wasn't what Gallifrey expected; dry-world thirst is off", e);
            return;
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isCreative() || player.isSpectator()) continue;
                Identifier world = player.getWorld().getRegistryKey().getValue();
                if (!world.getNamespace().equals(GallifreyMod.MOD_ID) || !DRY_WORLDS.contains(world.getPath())) continue;
                if (SpaceSuits.hasFullSuit(player)) continue;
                try {
                    addDehydration.invoke(getThirstManager.invoke(player), EXTRA_PER_SECOND);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    return;
                }
            }
        });
        GallifreyMod.LOGGER.info("[Gallifrey] Dehydration found: maple syrup quenches thirst, Mars and Skaro dry you out");
    }
}
