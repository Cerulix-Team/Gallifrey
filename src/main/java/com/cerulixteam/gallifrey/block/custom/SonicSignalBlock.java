package com.cerulixteam.gallifrey.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SonicSignalBlock extends Block {

    public SonicSignalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(
            BlockState state,
            net.minecraft.world.BlockView world,
            BlockPos pos,
            net.minecraft.util.math.Direction direction
    ) {
        return 15;
    }

    @Override
    public int getStrongRedstonePower(
            BlockState state,
            net.minecraft.world.BlockView world,
            BlockPos pos,
            net.minecraft.util.math.Direction direction
    ) {
        return 15;
    }
}
