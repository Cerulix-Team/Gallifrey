package com.cerulixteam.gallifrey.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Ghast variant used by the Classic Nether.  The AI is inherited from the
 * 1.20.1 ghast, which preserves the old-style floating/fireball behaviour
 * while keeping it isolated from the modern Nether's mob registry.
 */
public class AlphaGhastEntity extends GhastEntity {
    public AlphaGhastEntity(EntityType<? extends AlphaGhastEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("minecraft", "entities/ghast");
    }
}
