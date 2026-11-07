package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

/** Leaf Litter (1.21.5): up to four piles per block, like Pink Petals, on any solid top. Bone meal does nothing. */
public class LeafLitterBlock extends FlowerbedBlock {
    public LeafLitterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return false;
    }
}
