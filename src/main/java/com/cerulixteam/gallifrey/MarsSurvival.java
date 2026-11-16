
package com.cerulixteam.gallifrey;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class MarsSurvival {

    // Change this if your Mars dimension uses a different ID.
    public static final RegistryKey<World> MARS =
            RegistryKey.of(
                    RegistryKeys.WORLD,
                    new Identifier("gallifrey", "mars")
            );

    private MarsSurvival() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.getRegistryKey().equals(MARS)) {
                return;
            }

            for (ServerPlayerEntity player : world.getPlayers()) {
                // The Gallifrey suit, or any Ad Astra suit (see SpaceSuits).
                boolean fullSuit =
                        com.cerulixteam.gallifrey.item.custom.SpaceSuits.hasFullSuit(player);

                if (!fullSuit && !player.isCreative()
                        && !player.isSpectator()) {
                    DamageSource source = player.getDamageSources().dryOut();

                    player.damage(source, 1.0F);
                }
            }
        });
    }
}

