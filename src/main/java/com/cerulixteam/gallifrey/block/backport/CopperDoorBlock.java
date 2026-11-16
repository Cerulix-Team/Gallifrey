package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * Copper Door (1.21). In 1.20.1 a door breaks if its other half turns into a different block, which is
 * exactly what rusting, scraping and waxing do. So here a half that sees the other half become another
 * copper door turns into that door too, the way 1.21 doors do. The waxed doors use this class.
 */
public class CopperDoorBlock extends DoorBlock {
    public CopperDoorBlock(Settings settings, BlockSetType type) {
        super(settings, type);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            if (neighborState.getBlock() instanceof CopperDoorBlock && neighborState.get(HALF) != half) {
                return neighborState.with(HALF, half);
            }
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
