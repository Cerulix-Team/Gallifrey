package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.SpawnEggItem;

/** A spawn egg drawn from its own full-colour texture (26.x style) rather than tinted by vanilla's egg colours. */
public class BackportSpawnEggItem extends SpawnEggItem {
    public BackportSpawnEggItem(EntityType<? extends MobEntity> type, int primary, int secondary, Settings settings) {
        super(type, primary, secondary, settings);
    }

    @Override
    public int getColor(int tintIndex) {
        return 0xFFFFFF;
    }
}
