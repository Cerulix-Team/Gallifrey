package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/** Pale Hanging Moss (1.21.4): hangs under leaves or a solid block; the bottom one is the tip. Bone meal makes it longer. */
public class PaleHangingMossBlock extends Block implements Fertilizable {
    public static final BooleanProperty TIP = BooleanProperty.of("tip");
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
    private static final VoxelShape TIP_SHAPE = Block.createCuboidShape(1, 2, 1, 15, 16, 15);

    public PaleHangingMossBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(TIP, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TIP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(TIP) ? TIP_SHAPE : SHAPE;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.up());
        return above.isOf(this) || above.isIn(BlockTags.LEAVES) || above.isSideSolidFullSquare(world, pos.up(), Direction.DOWN);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState below = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        return this.getDefaultState().with(TIP, !below.isOf(this));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !this.canPlaceAt(state, world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        if (direction == Direction.DOWN) {
            return state.with(TIP, !neighborState.isOf(this));
        }
        return state;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.canPlaceAt(state, world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    private BlockPos tip(BlockView world, BlockPos pos) {
        BlockPos.Mutable at = pos.mutableCopy();
        while (world.getBlockState(at.down()).isOf(this)) at.move(Direction.DOWN);
        return at.toImmutable();
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return world.getBlockState(this.tip(world, pos).down()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockPos below = this.tip(world, pos).down();
        if (world.getBlockState(below).isAir()) {
            world.setBlockState(below, this.getDefaultState().with(TIP, true), Block.NOTIFY_LISTENERS);
        }
    }
}
