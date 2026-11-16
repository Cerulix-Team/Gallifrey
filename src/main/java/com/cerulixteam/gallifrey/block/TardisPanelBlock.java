package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.tardis.TardisFlight;
import com.cerulixteam.gallifrey.tardis.TardisPanel;
import com.cerulixteam.gallifrey.tardis.TardisPanelGeometry;
import com.cerulixteam.gallifrey.tardis.TardisPanelLayout;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A TARDIS control panel (Dalek Mod style). Place it on a floor, wall or ceiling inside a TARDIS; it
 * controls the TARDIS whose interior it is in. Click a button to use it (sneak-click the increment and
 * rotation dials to turn them back). Looking at a panel shows the name of the button under the crosshair.
 *
 * "on" shows the lever or switch position, kept in step with the TARDIS; "button" is the button currently
 * pressed in (1 = the first one), which springs back after a moment.
 */
public class TardisPanelBlock extends WallMountedBlock {
    public static final BooleanProperty ON = BooleanProperty.of("on");
    public static final IntProperty BUTTON = IntProperty.of("button", 0, TardisPanelLayout.MAX_BUTTONS);
    private static final int SYNC_TICKS = 10;
    private static final int PRESS_TICKS = 8;

    private final TardisPanel panel;
    private final Map<BlockState, VoxelShape> shapes = new ConcurrentHashMap<>();

    public TardisPanelBlock(TardisPanel panel, Settings settings) {
        super(settings);
        this.panel = panel;
        setDefaultState(getStateManager().getDefaultState()
                .with(FACE, WallMountLocation.FLOOR).with(FACING, Direction.NORTH).with(ON, false).with(BUTTON, 0));
    }

    public TardisPanel getPanel() { return panel; }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, ON, BUTTON);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return null;
        // On a floor or ceiling the near edge (where you work it from) faces the player placing it.
        if (state.get(FACE) != WallMountLocation.WALL) state = state.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapes.computeIfAbsent(state.with(ON, false).with(BUTTON, 0), s -> TardisPanelGeometry.shape(s, panel));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getOutlineShape(state, world, pos, context);
    }

    /** The button at this hit position, or null. */
    @Nullable
    public TardisPanelLayout.Button buttonAt(BlockState state, BlockPos pos, Vec3d hit) {
        return TardisPanelGeometry.buttonAt(state, panel, hit.subtract(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        TardisPanelLayout.Button button = buttonAt(state, pos, hit.getPos());
        if (button == null) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;
        if (!TardisDimensionManager.isInteriorWorld(world)) {
            serverPlayer.sendMessage(Text.translatable("tardis.gallifrey.msg.inside_only"), true);
            return ActionResult.CONSUME;
        }

        TardisFlight.usePanel(serverPlayer, serverWorld, button.action(), player.isSneaking());

        BlockState now = world.getBlockState(pos);
        if (!now.isOf(this)) return ActionResult.CONSUME;
        if (panel.showsPress()) {
            now = now.with(BUTTON, TardisPanelGeometry.indexOf(panel, button) + 1);
            world.setBlockState(pos, now, Block.NOTIFY_ALL);
            world.scheduleBlockTick(pos, this, PRESS_TICKS);
        }
        if (panel.layout().lever()) {
            world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.6F, 0.55F);
        } else {
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.5F, 1.3F);
        }
        sync(serverWorld, pos, now);
        return ActionResult.CONSUME;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient && panel.hasOnState() && !oldState.isOf(this)) world.scheduleBlockTick(pos, this, SYNC_TICKS);
    }

    /** Buttons spring back; levers and switches keep following the TARDIS. */
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(BUTTON) != 0) {
            state = state.with(BUTTON, 0);
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            if (!panel.layout().lever()) {
                world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 1.2F);
            }
        }
        if (panel.hasOnState()) {
            sync(world, pos, state);
            world.scheduleBlockTick(pos, this, SYNC_TICKS);
        }
    }

    /** Puts the lever or switch where the TARDIS says it should be. */
    private void sync(ServerWorld world, BlockPos pos, BlockState state) {
        if (!panel.hasOnState() || !state.isOf(this) || !TardisDimensionManager.isInteriorWorld(world)) return;
        TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(world);
        boolean on = tardis != null && TardisFlight.panelState(tardis, panel);
        if (state.get(ON) != on) world.setBlockState(pos, state.with(ON, on), Block.NOTIFY_ALL);
    }
}
