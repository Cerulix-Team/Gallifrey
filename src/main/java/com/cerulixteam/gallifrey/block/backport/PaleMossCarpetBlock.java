package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WallShape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 1.21.4's pale moss carpet (vanilla MossyCarpetBlock). It creeps up the sides of any solid block
 * next to it (short on its own block, full height on the block above), and bone meal grows a
 * new layer on top. Only the bottom layer drops anything.
 */
public class PaleMossCarpetBlock extends Block implements Fertilizable {
    public static final BooleanProperty BASE = Properties.BOTTOM;
    public static final EnumProperty<WallShape> NORTH = Properties.NORTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> EAST = Properties.EAST_WALL_SHAPE;
    public static final EnumProperty<WallShape> SOUTH = Properties.SOUTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> WEST = Properties.WEST_WALL_SHAPE;
    public static final Map<Direction, EnumProperty<WallShape>> PROPERTY_BY_DIRECTION = new EnumMap<>(Map.of(
            Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));

    private static final VoxelShape BOTTOM = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
    private static final Map<Direction, VoxelShape> LOW = Map.of(
            Direction.NORTH, Block.createCuboidShape(0, 0, 0, 16, 10, 1),
            Direction.SOUTH, Block.createCuboidShape(0, 0, 15, 16, 10, 16),
            Direction.WEST, Block.createCuboidShape(0, 0, 0, 1, 10, 16),
            Direction.EAST, Block.createCuboidShape(15, 0, 0, 16, 10, 16));
    private static final Map<Direction, VoxelShape> TALL = Map.of(
            Direction.NORTH, Block.createCuboidShape(0, 0, 0, 16, 16, 1),
            Direction.SOUTH, Block.createCuboidShape(0, 0, 15, 16, 16, 16),
            Direction.WEST, Block.createCuboidShape(0, 0, 0, 1, 16, 16),
            Direction.EAST, Block.createCuboidShape(15, 0, 0, 16, 16, 16));
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public PaleMossCarpetBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(BASE, true)
                .with(NORTH, WallShape.NONE).with(EAST, WallShape.NONE).with(SOUTH, WallShape.NONE).with(WEST, WallShape.NONE));
        for (BlockState state : this.getStateManager().getStates()) {
            this.shapes.put(state, makeShape(state));
        }
    }

    private static VoxelShape makeShape(BlockState state) {
        VoxelShape shape = state.get(BASE) ? BOTTOM : VoxelShapes.empty();
        for (Map.Entry<Direction, EnumProperty<WallShape>> e : PROPERTY_BY_DIRECTION.entrySet()) {
            switch (state.get(e.getValue())) {
                case LOW -> shape = VoxelShapes.union(shape, LOW.get(e.getKey()));
                case TALL -> shape = VoxelShapes.union(shape, TALL.get(e.getKey()));
                default -> { }
            }
        }
        return shape.isEmpty() ? VoxelShapes.fullCube() : shape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapes.get(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(BASE) ? BOTTOM : VoxelShapes.empty();
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        return state.get(BASE) ? !below.isAir() : below.isOf(this) && below.get(BASE);
    }

    private static boolean hasFaces(BlockState state) {
        if (state.get(BASE)) return true;
        for (EnumProperty<WallShape> property : PROPERTY_BY_DIRECTION.values()) {
            if (state.get(property) != WallShape.NONE) return true;
        }
        return false;
    }

    private static boolean canSupportAtFace(BlockView world, BlockPos pos, Direction direction) {
        if (direction == Direction.UP) return false;
        BlockPos neighbour = pos.offset(direction);
        return MultifaceGrowthBlock.canGrowOn(world, direction, neighbour, world.getBlockState(neighbour));
    }

    private static BlockState getUpdatedState(BlockState state, BlockView world, BlockPos pos, boolean createSides) {
        BlockState above = null;
        BlockState below = null;
        createSides |= state.get(BASE);
        Block carpet = BackportBlocks.PALE_MOSS_CARPET;

        for (Direction direction : Direction.Type.HORIZONTAL) {
            EnumProperty<WallShape> property = PROPERTY_BY_DIRECTION.get(direction);
            WallShape side = canSupportAtFace(world, pos, direction) ? (createSides ? WallShape.LOW : state.get(property)) : WallShape.NONE;
            if (side == WallShape.LOW) {
                if (above == null) above = world.getBlockState(pos.up());
                if (above.isOf(carpet) && above.get(property) != WallShape.NONE && !above.get(BASE)) side = WallShape.TALL;
                if (!state.get(BASE)) {
                    if (below == null) below = world.getBlockState(pos.down());
                    if (below.isOf(carpet) && below.get(property) == WallShape.NONE) side = WallShape.NONE;
                }
            }
            state = state.with(property, side);
        }
        return state;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getUpdatedState(this.getDefaultState(), ctx.getWorld(), ctx.getBlockPos(), true);
    }

    /** Places a carpet with a randomly grown layer on top, like vanilla worldgen does. */
    public static void placeAt(WorldAccess world, BlockPos pos, Random random, int flags) {
        BlockState carpet = getUpdatedState(BackportBlocks.PALE_MOSS_CARPET.getDefaultState(), world, pos, true);
        world.setBlockState(pos, carpet, flags);
        BlockState topper = createTopperWithSideChance(world, pos, random::nextBoolean);
        if (!topper.isAir()) {
            world.setBlockState(pos.up(), topper, flags);
            world.setBlockState(pos, getUpdatedState(carpet, world, pos, true), flags);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world.isClient) return;
        Random random = world.getRandom();
        BlockState topper = createTopperWithSideChance(world, pos, random::nextBoolean);
        if (!topper.isAir()) world.setBlockState(pos.up(), topper);
    }

    private static BlockState createTopperWithSideChance(BlockView world, BlockPos pos, BooleanSupplier keepSide) {
        Block carpet = BackportBlocks.PALE_MOSS_CARPET;
        BlockPos up = pos.up();
        BlockState previous = world.getBlockState(up);
        boolean carpetAbove = previous.isOf(carpet);
        if ((carpetAbove && previous.get(BASE)) || (!carpetAbove && !previous.isReplaceable())) {
            return Blocks.AIR.getDefaultState();
        }
        BlockState topper = getUpdatedState(carpet.getDefaultState().with(BASE, false), world, up, true);
        for (Direction direction : Direction.Type.HORIZONTAL) {
            EnumProperty<WallShape> property = PROPERTY_BY_DIRECTION.get(direction);
            if (topper.get(property) != WallShape.NONE && !keepSide.getAsBoolean()) {
                topper = topper.with(property, WallShape.NONE);
            }
        }
        return hasFaces(topper) && topper != previous ? topper : Blocks.AIR.getDefaultState();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
                                                BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) return Blocks.AIR.getDefaultState();
        BlockState updated = getUpdatedState(state, world, pos, false);
        return hasFaces(updated) ? updated : Blocks.AIR.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BASE, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90 -> state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case CLOCKWISE_90 -> state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case FRONT_BACK -> state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(BASE) && !createTopperWithSideChance(world, pos, () -> true).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockState topper = createTopperWithSideChance(world, pos, () -> true);
        if (!topper.isAir()) world.setBlockState(pos.up(), topper);
    }
}
