package com.cerulixteam.gallifrey.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * A simple vertical half-block that can be combined with its opposite-facing
 * half to make the original full block.
 */
public class VerticalSlabBlock extends HorizontalFacingBlock {
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0, 0, 0, 8, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(8, 0, 0, 16, 16, 16);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 8);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0, 0, 8, 16, 16, 16);

    private final Block fullBlock;

    public VerticalSlabBlock(Block fullBlock, AbstractBlock.Settings settings) {
        super(settings);
        this.fullBlock = fullBlock;
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = getPlacementFacing(ctx);
        BlockState existing = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (existing.isOf(this) && existing.get(FACING) != facing) {
            // Two opposite vertical halves become the original full block.
            return fullBlock.getDefaultState();
        }
        return this.getDefaultState().with(FACING, facing);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
        if (!ctx.getStack().isEmpty() && ctx.getStack().isOf(this.asItem()) && state.isOf(this)) {
            return state.get(FACING) != getPlacementFacing(ctx);
        }
        return false;
    }

    private static Direction getPlacementFacing(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();
        if (side.getAxis().isHorizontal()) {
            return side.getOpposite();
        }
        PlayerEntity player = ctx.getPlayer();
        Direction look = ctx.getPlayerLookDirection();
        if (look.getAxis().isHorizontal()) {
            return look.getOpposite();
        }
        return player != null ? player.getHorizontalFacing().getOpposite() : Direction.NORTH;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return getShape(state);
    }

    private static VoxelShape getShape(BlockState state) {
        return switch (state.get(FACING)) {
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case NORTH -> NORTH_SHAPE;
            default -> WEST_SHAPE;
        };
    }
}
