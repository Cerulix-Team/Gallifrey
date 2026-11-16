package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Set;

/** Only Cybermen, Cybermats and Daleks can survive as living mobs on Mars and the Moon. */
public final class VacuumMobSurvivalHandler {
    private static final Set<Identifier> SURVIVORS = Set.of(
            new Identifier("gallifrey", "mondasian_cyberman"),
            new Identifier("gallifrey", "proto_cyberman"),
            new Identifier("gallifrey", "cybermat"),
            new Identifier("gallifrey", "skaro_city_dalek"),
            new Identifier("gallifrey", "skaro_city_dalek_alt"),
            new Identifier("gallifrey", "supreme_council_dalek")
    );

    private VacuumMobSurvivalHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!world.getRegistryKey().equals(ModDimensions.MARS_LEVEL_KEY)
                    && !world.getRegistryKey().equals(ModDimensions.MOON_LEVEL_KEY)) {
                return;
            }

            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof LivingEntity) || entity.isPlayer()) {
                    continue;
                }
                Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
                if (!SURVIVORS.contains(id)) {
                    entity.kill();
                }
            }
        });
    }
}
