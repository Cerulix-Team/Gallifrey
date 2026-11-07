package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * Shelf Mushroom (26.3): grows out of the side of a log. Bone meal makes a small one large; a large one
 * drops two. Bouncy to land on, like a bed.
 */
public class ShelfMushroomBlock extends HorizontalFacingBlock implements Fertilizable {
    public static final IntProperty AGE = IntProperty.of("age", 0, 1);

    // Shapes for a mushroom on the south side of its log (facing north), from the vanilla models.
    private static final VoxelShape SMALL = VoxelShapes.union(Block.createCuboidShape(3, 9, 9, 13, 11, 16), Block.createCuboidShape(5, 8, 12, 11, 9, 16));
    private static final VoxelShape LARGE = VoxelShapes.union(Block.createCuboidShape(1, 8, 6, 15, 11, 16), Block.createCuboidShape(4, 6, 10, 12, 8, 16));

    public ShelfMushroomBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
    }

    private static VoxelShape rotate(VoxelShape shape, Direction facing) {
        int turns = switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        VoxelShape out = shape;
        for (int i = 0; i < turns; i++) {
            VoxelShape[] next = {VoxelShapes.empty()};
            out.forEachBox((x1, y1, z1, x2, y2, z2) -> next[0] = VoxelShapes.union(next[0], VoxelShapes.cuboid(1 - z2, y1, x1, 1 - z1, y2, x2)));
            out = next[0];
        }
        return out;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return rotate(state.get(AGE) == 0 ? SMALL : LARGE, state.get(FACING));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos behind = pos.offset(facing.getOpposite());
        BlockState log = world.getBlockState(behind);
        return log.isIn(BlockTags.LOGS) || log.isSideSolidFullSquare(world, behind, facing);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();
        if (side.getAxis().isHorizontal()) {
            BlockState state = this.getDefaultState().with(FACING, side);
            return state.canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? state : null;
        }
        for (Direction direction : ctx.getPlacementDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState state = this.getDefaultState().with(FACING, direction.getOpposite());
                if (state.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) return state;
            }
        }
        return null;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)) {
            return net.minecraft.block.Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance * 0.5F);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity.bypassesLandingEffects()) {
            super.onEntityLand(world, entity);
            return;
        }
        Vec3d v = entity.getVelocity();
        if (v.y < 0) {
            double bounce = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setVelocity(v.x, -v.y * 0.66 * bounce, v.z);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(AGE) == 0;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(AGE, 1), Block.NOTIFY_LISTENERS);
    }
}
