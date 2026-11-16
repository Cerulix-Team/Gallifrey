package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * Sulfur Spike (26.2): pointed like dripstone. Hangs from a ceiling or stands on a floor, stacks into
 * longer spikes, and joins up when a stalactite meets a stalagmite (unless you sneak while placing).
 * A stalactite that loses its support falls and hurts what it lands on; a stalagmite just breaks.
 * Thrown tridents break them.
 */
public class SulfurSpikeBlock extends Block {
    public static final DirectionProperty VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
    public static final EnumProperty<Thickness> THICKNESS = Properties.THICKNESS;

    private static final VoxelShape TIP_MERGE = Block.createCuboidShape(5, 0, 5, 11, 16, 11);
    private static final VoxelShape TIP_UP = Block.createCuboidShape(5, 0, 5, 11, 11, 11);
    private static final VoxelShape TIP_DOWN = Block.createCuboidShape(5, 5, 5, 11, 16, 11);
    private static final VoxelShape FRUSTUM = Block.createCuboidShape(4, 0, 4, 12, 16, 12);
    private static final VoxelShape MIDDLE = Block.createCuboidShape(3, 0, 3, 13, 16, 13);
    private static final VoxelShape BASE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);

    public SulfurSpikeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(VERTICAL_DIRECTION, Direction.UP).with(THICKNESS, Thickness.TIP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION, THICKNESS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(THICKNESS)) {
            case TIP_MERGE -> TIP_MERGE;
            case TIP -> state.get(VERTICAL_DIRECTION) == Direction.DOWN ? TIP_DOWN : TIP_UP;
            case FRUSTUM -> FRUSTUM;
            case MIDDLE -> MIDDLE;
            default -> BASE;
        };
    }

    private boolean isSpike(BlockState state, Direction direction) {
        return state.isOf(this) && state.get(VERTICAL_DIRECTION) == direction;
    }

    /** Whether a spike pointing this way can stay here: the block behind it is solid, or another spike pointing the same way. */
    private boolean supported(WorldView world, BlockPos pos, Direction direction) {
        BlockPos behind = pos.offset(direction.getOpposite());
        BlockState state = world.getBlockState(behind);
        return state.isSideSolidFullSquare(world, behind, direction) || this.isSpike(state, direction);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.supported(world, pos, state.get(VERTICAL_DIRECTION));
    }

    private Thickness thickness(WorldView world, BlockPos pos, Direction direction, boolean merge) {
        BlockState ahead = world.getBlockState(pos.offset(direction));
        if (this.isSpike(ahead, direction.getOpposite())) {
            return merge || ahead.get(THICKNESS) == Thickness.TIP_MERGE ? Thickness.TIP_MERGE : Thickness.TIP;
        }
        if (!this.isSpike(ahead, direction)) return Thickness.TIP;
        Thickness next = ahead.get(THICKNESS);
        if (next == Thickness.TIP || next == Thickness.TIP_MERGE) return Thickness.FRUSTUM;
        BlockState behind = world.getBlockState(pos.offset(direction.getOpposite()));
        return this.isSpike(behind, direction) ? Thickness.MIDDLE : Thickness.BASE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction direction = ctx.getVerticalPlayerLookDirection().getOpposite();
        if (ctx.getSide() == Direction.DOWN) direction = Direction.DOWN;
        else if (ctx.getSide() == Direction.UP) direction = Direction.UP;
        if (!this.supported(world, pos, direction)) {
            direction = direction.getOpposite();
            if (!this.supported(world, pos, direction)) return null;
        }
        return this.getDefaultState().with(VERTICAL_DIRECTION, direction)
                .with(THICKNESS, this.thickness(world, pos, direction, !ctx.shouldCancelInteraction()));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() != Direction.Axis.Y) return state;
        Direction pointing = state.get(VERTICAL_DIRECTION);
        if (direction == pointing.getOpposite() && !this.canPlaceAt(state, world, pos)) {
            world.scheduleBlockTick(pos, this, pointing == Direction.DOWN ? 2 : 1);
            return state;
        }
        boolean merge = state.get(THICKNESS) == Thickness.TIP_MERGE;
        return state.with(THICKNESS, this.thickness(world, pos, pointing, merge));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.canPlaceAt(state, world, pos)) return;
        if (state.get(VERTICAL_DIRECTION) == Direction.DOWN && FallingBlock.canFallThrough(world.getBlockState(pos.down()))) {
            FallingBlockEntity falling = FallingBlockEntity.spawnFromBlock(world, pos, state.with(THICKNESS, Thickness.TIP));
            falling.setHurtEntities(2.0F, 40);
            falling.dropItem = true;
        } else {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient && projectile instanceof TridentEntity && projectile.getVelocity().length() > 0.6) {
            world.breakBlock(hit.getBlockPos(), true);
        }
    }
}
