package com.cerulixteam.gallifrey.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Alternate Skaro City Dalek variant. It inherits the standard Dalek AI and weapons. */
public class SkaroCityDalekAltEntity extends SkaroCityDalekEntity {
    public SkaroCityDalekAltEntity(EntityType<? extends SkaroCityDalekAltEntity> type, World world) {
        super(type, world);
    }
}
