package com.cerulixteam.gallifrey.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/** Supreme Council Dalek variant. It inherits the standard Dalek AI and weapons. */
public class SupremeCouncilDalekEntity extends SkaroCityDalekEntity {
    public SupremeCouncilDalekEntity(EntityType<? extends SupremeCouncilDalekEntity> type, World world) {
        super(type, world);
    }
}
