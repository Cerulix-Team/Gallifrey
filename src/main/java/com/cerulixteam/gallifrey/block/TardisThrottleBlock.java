package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.GallifreyTardisControlTicker;
import com.cerulixteam.gallifrey.block.entity.TardisThrottleBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The TARDIS throttle (GeckoLib model), Dalek Mod style: a physical lever inside the TARDIS.
 * Set a destination on the monitor, then pull the throttle to take off. The lever stays forward
 * for the whole flight and springs back by itself once the TARDIS has landed. If the TARDIS can't
 * go (no destination, no fuel, nowhere to land, not your TARDIS) it says why and stays put.
 *
 * Only works inside a TARDIS interior; it controls the TARDIS whose interior it's in.
 */
public class TardisThrottleBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty PULLED = BooleanProperty.of("pulled");
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 8, 12);

    public TardisThrottleBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(PULLED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PULLED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisThrottleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : GallifreyTardisControlTicker.throttle(type);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                              Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity pilot) || !(world instanceof ServerWorld serverWorld)) return ActionResult.PASS;

        if (!TardisDimensionManager.isInteriorWorld(world)) {
            pilot.sendMessage(Text.literal("The throttle only works inside a TARDIS."), true);
            return ActionResult.CONSUME;
        }
        TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(serverWorld);
        if (tardis == null) {
            pilot.sendMessage(Text.literal("This throttle isn't connected to a TARDIS."), true);
            return ActionResult.CONSUME;
        }

        // Works like the flight lever: take off, and once you've arrived pull it again to land.
        world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.6F, 0.6F);
        com.cerulixteam.gallifrey.tardis.TardisFlight.pullFlightLever(pilot, tardis);
        TardisExteriorBlockEntity now = TardisDimensionManager.findTardisForInterior(serverWorld);
        boolean flying = now != null && now.isFlightPending();
        if (state.get(PULLED) != flying) world.setBlockState(pos, state.with(PULLED, flying), Block.NOTIFY_ALL);
        return ActionResult.CONSUME;
    }
}
