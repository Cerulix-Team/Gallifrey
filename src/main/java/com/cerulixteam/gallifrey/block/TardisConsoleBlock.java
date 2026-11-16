package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisConsoleBlockEntity;
import com.cerulixteam.gallifrey.tardis.TardisMonitorNetworking;
import com.cerulixteam.gallifrey.tardis.TardisRegistryState;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * The actual TARDIS control console. The supplied Hartnell console Geo model
 * is rendered by the block entity renderer; right-clicking the console opens
 * the full TARDIS control screen.
 */
public class TardisConsoleBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    // The supplied Hartnell model is roughly 3 blocks wide, so give the
    // physical console a matching interaction/outline footprint.  This keeps
    // clicks on the visible console (not just its centre block) opening the UI.
    private static final VoxelShape SHAPE = Block.createCuboidShape(-16, 0, -16, 32, 16, 32);

    public TardisConsoleBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.util.BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.util.BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisConsoleBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient || !(placer instanceof ServerPlayerEntity player)
                || !TardisDimensionManager.isInteriorWorld(world)) return;

        TardisRegistryState registry = TardisRegistryState.get(player.getServer());
        java.util.UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) return;
        TardisRegistryState.Record record = registry.get(id);
        if (record == null) return;
        ServerWorld exterior = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(
                net.minecraft.registry.RegistryKeys.WORLD, new net.minecraft.util.Identifier(record.world())));
        if (exterior == null) return;
        BlockPos exteriorPos = BlockPos.fromLong(record.pos());
        if (exterior.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis
                && id.equals(tardis.getTardisId())) {
            tardis.setConsolePos(pos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (!world.isClient && TardisDimensionManager.isInteriorWorld(world)
                    && world.getBlockEntity(pos) instanceof TardisConsoleBlockEntity) {
                // Clear the stored position if this was the registered console.
                // The next placed console inside the TARDIS will re-register itself.
                for (TardisRegistryState.Record record : TardisRegistryState.get(world.getServer()).records()) {
                    ServerWorld exterior = world.getServer().getWorld(net.minecraft.registry.RegistryKey.of(
                            net.minecraft.registry.RegistryKeys.WORLD, new net.minecraft.util.Identifier(record.world())));
                    if (exterior == null) continue;
                    BlockPos exteriorPos = BlockPos.fromLong(record.pos());
                    if (exterior.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis
                            && pos.equals(tardis.getConsolePos())) {
                        tardis.setConsolePos(null);
                    }
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
        if (!(world.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console)) return ActionResult.PASS;

        if (!console.isPowered()) {
            player.sendMessage(net.minecraft.text.Text.literal("The TARDIS console is offline."), true);
            return ActionResult.CONSUME;
        }

        return TardisMonitorNetworking.openFor(serverPlayer)
                ? ActionResult.CONSUME
                : ActionResult.FAIL;
    }
}
