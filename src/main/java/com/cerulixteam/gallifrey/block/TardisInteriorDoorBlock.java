package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisInteriorDoorBlockEntity;
import com.cerulixteam.gallifrey.GallifreySounds;
import net.minecraft.sound.SoundCategory;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.tardis.TardisRegistryState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TardisInteriorDoorBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);

    public TardisInteriorDoorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Like a normal Minecraft door, the front faces the player placing it.
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Open: walk out through it (onEntityCollision).
        if (world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door && door.isOpen()) {
            return net.minecraft.util.shape.VoxelShapes.empty();
        }
        return SHAPE;
    }

    /** The side of the door the room is on (the side its front faces). */
    public static Direction roomSide(BlockState state) {
        return state.get(FACING);
    }

    /** Dalek Mod style: with the doors open, walk into the door to step outside. */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, net.minecraft.entity.Entity entity) {
        if (world.isClient || !(entity instanceof ServerPlayerEntity player)) return;
        if (!(world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door) || !door.isOpen()) return;
        Direction room = roomSide(state);
        // Only when walking in from the room side.
        net.minecraft.util.math.Vec3d offset = player.getPos().subtract(net.minecraft.util.math.Vec3d.ofBottomCenter(pos));
        if (offset.x * room.getOffsetX() + offset.z * room.getOffsetZ() < -0.05) return;
        // ...and facing the door, not just arrived and still facing into the room.
        net.minecraft.util.math.Vec3d look = player.getRotationVector();
        if (look.x * room.getOffsetX() + look.z * room.getOffsetZ() > 0.2) return;
        if (!TardisDimensionManager.canUseDoors(player)) return;
        TardisDimensionManager.exit(player);
    }


    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world.isClient || !(placer instanceof ServerPlayerEntity player) || !(world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door)) return;
        TardisRegistryState registry = TardisRegistryState.get(player.getServer());
        java.util.UUID id = registry.getActiveTardis(player.getUuid());
        TardisRegistryState.Record record = id == null ? null : registry.get(id);
        if (record == null || record.owner() == null || !record.owner().equals(player.getUuid()) || !TardisDimensionManager.isInteriorWorld(world)) return;
        door.setTardisId(id);
        door.setExteriorStyle(record.id() != null ? exteriorStyleFor(player) : "policebox");
        door.setPowered(findPower(player));
    }

    private String exteriorStyleFor(ServerPlayerEntity player) {
        TardisRegistryState.Record record = TardisRegistryState.get(player.getServer()).get(TardisRegistryState.get(player.getServer()).getActiveTardis(player.getUuid()));
        if (record == null) return "policebox";
        net.minecraft.server.world.ServerWorld world = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new net.minecraft.util.Identifier(record.world())));
        net.minecraft.util.math.BlockPos pos = record == null ? net.minecraft.util.math.BlockPos.ORIGIN : net.minecraft.util.math.BlockPos.fromLong(record.pos());
        if (world != null && world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis) return tardis.getExteriorStyle();
        return "policebox";
    }

    private boolean findPower(ServerPlayerEntity player) {
        TardisRegistryState.Record record = TardisRegistryState.get(player.getServer()).get(TardisRegistryState.get(player.getServer()).getActiveTardis(player.getUuid()));
        if (record == null) return false;
        net.minecraft.server.world.ServerWorld world = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new net.minecraft.util.Identifier(record.world())));
        net.minecraft.util.math.BlockPos pos = net.minecraft.util.math.BlockPos.fromLong(record.pos());
        return world != null && world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis && tardis.isPowered();
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisInteriorDoorBlockEntity(pos, state);
    }

    /** Click to open or close the doors (the outside doors open and close with it). */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof net.minecraft.server.world.ServerWorld interior)) return ActionResult.PASS;
        com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(interior);
        if (tardis == null) return ActionResult.FAIL;
        boolean open = !tardis.getCachedState().get(TardisExteriorBlock.OPEN);
        if (open && tardis.isFlightPending()) {
            serverPlayer.sendMessage(net.minecraft.text.Text.translatable("tardis.gallifrey.msg.doors_in_flight"), true);
        } else if (open && tardis.isLocked() && !tardis.canPilot(serverPlayer.getUuid())) {
            serverPlayer.sendMessage(net.minecraft.text.Text.translatable("tardis.gallifrey.msg.doors_locked"), true);
        } else {
            com.cerulixteam.gallifrey.tardis.TardisFlight.setDoors(tardis, open, true);
        }
        return ActionResult.CONSUME;
    }
}
