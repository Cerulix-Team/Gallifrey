package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

/** Red Shrub (26.3): bone meal spreads it to an empty spot nearby. */
public class RedShrubBlock extends BackportPlantBlock implements Fertilizable {
    public RedShrubBlock(VoxelShape shape, Settings settings) {
        super(Soil.DIRT, shape, settings);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        for (int i = 0; i < 8; i++) {
            BlockPos at = pos.add(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (world.isAir(at) && state.canPlaceAt(world, at)) {
                world.setBlockState(at, state);
                return;
            }
        }
    }
}
