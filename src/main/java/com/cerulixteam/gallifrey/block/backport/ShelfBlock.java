package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shelf (1.21.9): holds three items you can see. Use it to swap what's in your hand with the slot you're
 * pointing at. Powered with redstone, using it swaps your whole hotbar with the row: up to three powered
 * shelves side by side, facing the same way, join up into one 9-slot row.
 */
public class ShelfBlock extends HorizontalFacingBlock implements BlockEntityProvider, Waterloggable {
    public enum SideChain implements StringIdentifiable {
        UNCONNECTED("unconnected"), RIGHT("right"), CENTER("center"), LEFT("left");
        private final String name;
        SideChain(String name) { this.name = name; }
        @Override public String asString() { return this.name; }
    }

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<SideChain> SIDE_CHAIN = EnumProperty.of("side_chain", SideChain.class);

    // The shelf for one facing north: a back panel and a ledge, from the vanilla model.
    private static final VoxelShape NORTH = VoxelShapes.union(Block.createCuboidShape(0, 0, 11, 16, 16, 16),
            Block.createCuboidShape(0, 0, 0, 16, 4, 16), Block.createCuboidShape(0, 12, 0, 16, 16, 16));

    public ShelfBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false)
                .with(WATERLOGGED, false).with(SIDE_CHAIN, SideChain.UNCONNECTED));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, WATERLOGGED, SIDE_CHAIN);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return rotate(state.get(FACING));
    }

    private static VoxelShape rotate(Direction facing) {
        int turns = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        VoxelShape out = NORTH;
        for (int i = 0; i < turns; i++) {
            VoxelShape[] next = {VoxelShapes.empty()};
            out.forEachBox((x1, y1, z1, x2, y2, z2) -> next[0] = VoxelShapes.union(next[0], VoxelShapes.cuboid(1 - z2, y1, x1, 1 - z1, y2, x2)));
            out = next[0];
        }
        return out;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        BlockState state = this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(POWERED, world.isReceivingRedstonePower(pos))
                .with(WATERLOGGED, world.getFluidState(pos).isOf(Fluids.WATER));
        return state.with(SIDE_CHAIN, chain(world, pos, state));
    }

    /** Which way along the shelf a shelf's "left" is, looking at its front. */
    private static Direction left(BlockState state) {
        return state.get(FACING).rotateYClockwise();
    }

    private static boolean joins(BlockState a, BlockState b) {
        return b.getBlock() instanceof ShelfBlock && b.get(FACING) == a.get(FACING) && a.get(POWERED) && b.get(POWERED);
    }

    private static SideChain chain(WorldAccess world, BlockPos pos, BlockState state) {
        if (!state.get(POWERED)) return SideChain.UNCONNECTED;
        Direction left = left(state);
        boolean l = joins(state, world.getBlockState(pos.offset(left)));
        boolean r = joins(state, world.getBlockState(pos.offset(left.getOpposite())));
        if (l && r) return SideChain.CENTER;
        if (l) return SideChain.RIGHT;
        if (r) return SideChain.LEFT;
        return SideChain.UNCONNECTED;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        return state.with(SIDE_CHAIN, chain(world, pos, state));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED)) {
            BlockState next = state.with(POWERED, powered);
            world.setBlockState(pos, next.with(SIDE_CHAIN, chain(world, pos, next)), Block.NOTIFY_ALL);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfBlockEntity(pos, state);
    }

    /** Which of the three slots (0 = left, looking at the front) the player's crosshair is on. */
    private static int slotAt(BlockState state, BlockPos pos, Vec3d hit) {
        Direction left = left(state);
        double along = (hit.x - pos.getX()) * -left.getOffsetX() + (hit.z - pos.getZ()) * -left.getOffsetZ();
        if (left.getOffsetX() > 0 || left.getOffsetZ() > 0) along += 1;
        return Math.max(0, Math.min(2, (int) (along * 3)));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hit.getSide() != state.get(FACING) || !(world.getBlockEntity(pos) instanceof ShelfBlockEntity shelf)) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;
        if (state.get(POWERED)) {
            this.swapRow(world, pos, state, player);
        } else {
            int slot = slotAt(state, pos, hit.getPos());
            ItemStack inHand = player.getStackInHand(hand);
            ItemStack onShelf = shelf.getStack(slot);
            shelf.setStack(slot, inHand.copy());
            player.setStackInHand(hand, onShelf);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResult.CONSUME;
    }

    /** Swaps the player's hotbar with the row of joined powered shelves (left to right). */
    private void swapRow(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction left = left(state);
        BlockPos start = pos;
        for (int i = 0; i < 2 && joins(state, world.getBlockState(start.offset(left))); i++) start = start.offset(left);
        List<ShelfBlockEntity> row = new ArrayList<>();
        BlockPos at = start;
        for (int i = 0; i < 3; i++) {
            if (!(world.getBlockEntity(at) instanceof ShelfBlockEntity shelf)) break;
            row.add(shelf);
            BlockPos next = at.offset(left.getOpposite());
            if (!joins(world.getBlockState(at), world.getBlockState(next))) break;
            at = next;
        }
        int hotbar = 0;
        for (ShelfBlockEntity shelf : row) {
            for (int s = 0; s < 3; s++, hotbar++) {
                ItemStack fromShelf = shelf.getStack(s);
                shelf.setStack(s, player.getInventory().getStack(hotbar).copy());
                player.getInventory().setStack(hotbar, fromShelf);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) ItemScatterer.spawn(world, pos, shelf);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof ShelfBlockEntity shelf)) return 0;
        int filled = 0;
        for (int i = 0; i < 3; i++) if (!shelf.getStack(i).isEmpty()) filled |= 1 << i;
        return filled * 2;
    }
}
