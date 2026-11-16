package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.tardis.TardisControl;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.tardis.TardisFlight;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * One separate TARDIS control (Dalek Mod style): handbrake, door control, refueller, landing type,
 * X/Y/Z, increment, facing, dimension, randomiser or fast return. Place it anywhere inside a TARDIS.
 *
 * Right-click to use it; sneak + right-click to turn a number control the other way.
 * Levers show their position (and follow the TARDIS, e.g. the door lever flips when the doors
 * are opened from outside); buttons press in and spring back.
 */
public class TardisControlBlock extends HorizontalFacingBlock {
    /** Lever: on/off. Button: pressed. */
    public static final BooleanProperty ON = BooleanProperty.of("on");
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 12, 15);
    private static final int SYNC_TICKS = 20;
    private static final int BUTTON_TICKS = 8;

    private final TardisControl control;

    public TardisControlBlock(TardisControl control, Settings settings) {
        super(settings);
        this.control = control;
        setDefaultState(getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(ON, false));
    }

    public TardisControl getControl() { return control; }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // The front (where the lever or button is worked from) faces the player placing it.
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
        if (!TardisDimensionManager.isInteriorWorld(world)) {
            serverPlayer.sendMessage(Text.translatable("tardis.gallifrey.msg.inside_only"), true);
            return ActionResult.CONSUME;
        }

        TardisFlight.useControl(serverPlayer, serverWorld, control, player.isSneaking() ? -1 : 1);

        if (control.kind() == TardisControl.Kind.BUTTON) {
            world.setBlockState(pos, state.with(ON, true), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.5F, 1.2F);
            world.scheduleBlockTick(pos, this, BUTTON_TICKS);
        } else {
            boolean on = syncLever(serverWorld, pos, world.getBlockState(pos));
            world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5F, on ? 0.6F : 0.5F);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient && control.kind() == TardisControl.Kind.LEVER && !oldState.isOf(this)) {
            world.scheduleBlockTick(pos, this, SYNC_TICKS);
        }
    }

    /** Buttons spring back; levers keep following the TARDIS once a second. */
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (control.kind() == TardisControl.Kind.BUTTON) {
            if (state.get(ON)) {
                world.setBlockState(pos, state.with(ON, false), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 1.1F);
            }
            return;
        }
        if (TardisDimensionManager.isInteriorWorld(world)) syncLever(world, pos, state);
        world.scheduleBlockTick(pos, this, SYNC_TICKS);
    }

    /** Puts the lever where the TARDIS says it should be. Returns its position. */
    private boolean syncLever(ServerWorld world, BlockPos pos, BlockState state) {
        if (!state.isOf(this)) return false;
        TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(world);
        boolean on = tardis != null && TardisFlight.leverState(tardis, control);
        if (state.get(ON) != on) world.setBlockState(pos, state.with(ON, on), Block.NOTIFY_ALL);
        return on;
    }
}
