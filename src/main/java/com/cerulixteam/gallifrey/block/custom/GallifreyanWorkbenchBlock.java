package com.cerulixteam.gallifrey.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * The Gallifreyan Workbench: the Time Lord Trader's job site block (see GallifreyVillagers).
 * It's a table with a full-width top and a slightly inset body, so its shape matches the model
 * and it doesn't hide the faces of the blocks around it.
 */
public class GallifreyanWorkbenchBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 15, 0, 16, 16, 16),   // table top
            Block.createCuboidShape(1, 0, 1, 15, 15, 15)     // body
    );

    public GallifreyanWorkbenchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
