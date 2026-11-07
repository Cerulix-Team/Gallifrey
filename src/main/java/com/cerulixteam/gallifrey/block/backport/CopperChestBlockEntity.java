package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** A copper chest's contents; otherwise a normal chest (it keeps its items when it rusts or is waxed). */
public class CopperChestBlockEntity extends ChestBlockEntity {
    public CopperChestBlockEntity(BlockPos pos, BlockState state) {
        super(BackportExtras.COPPER_CHEST_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return this.getCachedState().getBlock().getName();
    }
}
