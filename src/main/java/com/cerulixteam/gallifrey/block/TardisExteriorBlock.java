package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.BlockView;
import net.minecraft.sound.SoundCategory;
import com.cerulixteam.gallifrey.GallifreySounds;
import com.cerulixteam.gallifrey.tardis.TardisFlight;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class TardisExteriorBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape TARDIS_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 32, 16);
    public static final IntProperty ROTATION = IntProperty.of("rotation", 0, 7);
    public static final BooleanProperty OPEN = BooleanProperty.of("open");

    public TardisExteriorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(ROTATION, 0).with(OPEN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, OPEN);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        float yaw = ctx.getPlayerYaw();
        int rotation = MathHelper.floor((double)((yaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7;
        return getDefaultState().with(ROTATION, rotation);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // A TARDIS must never be placed inside another TARDIS pocket dimension,
        // and its full two-block footprint must be clear.
        return !TardisDimensionManager.isInteriorWorld(world)
                && super.canPlaceAt(state, world, pos)
                && world.getBlockState(pos.up()).isAir();
    }

    /** While it's faded away mid take-off or landing there's nothing there to touch or click. */
    private static boolean intangible(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis && tardis.isIntangible();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return intangible(world, pos) ? VoxelShapes.empty() : TARDIS_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Open doors: you can walk in (see onEntityCollision).
        if (state.get(OPEN) || intangible(world, pos)) return VoxelShapes.empty();
        return TARDIS_SHAPE;
    }

    /** Dalek Mod style: with the doors open, walk in through the front to go inside. */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient || !state.get(OPEN) || !(entity instanceof ServerPlayerEntity player)) return;
        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) || tardis.isFlightPending()) return;
        Vec3d front = TardisFlight.frontVector(state.get(ROTATION));
        Vec3d offset = player.getPos().subtract(Vec3d.ofBottomCenter(pos));
        // Only through the doors: the player must be on the front half, heading into the box.
        if (offset.dotProduct(front) < -0.05 || player.getRotationVector().dotProduct(front) > 0.2) return;
        if (!TardisDimensionManager.canUseDoors(player)) return;
        if (TardisDimensionManager.enter(player, tardis)) {
            com.cerulixteam.gallifrey.tardis.TardisEntryTracker.entered(player.getUuid(),
                    world.getRegistryKey(), pos,
                    player.getWorld().getRegistryKey(), player.getPos());
            com.cerulixteam.gallifrey.compat.pehkui.PehkuiCompat.squeezeThroughDoor(player);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (!world.isClient && placer instanceof ServerPlayerEntity player
                && world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) {
            tardis.ensureInitialized(player);
            if (tardis.getTardisId() != null) {
                player.sendMessage(Text.literal("New TARDIS created: " + tardis.getTardisId()), true);
            }
        }
    }

    /** Puts the invisible top half (TardisExteriorTopBlock) above the box, so the top half can be clicked too. */
    public static void ensureTop(World world, BlockPos pos) {
        if (world.isClient) return;
        BlockPos up = pos.up();
        BlockState above = world.getBlockState(up);
        if (above.getBlock() instanceof TardisExteriorTopBlock) return;
        if (above.isAir() || above.isReplaceable()) {
            world.setBlockState(up, GallifreyModBlocks.TARDIS_EXTERIOR_TOP.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!oldState.isOf(this)) ensureTop(world, pos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && !world.isClient
                && world.getBlockState(pos.up()).getBlock() instanceof TardisExteriorTopBlock) {
            world.removeBlock(pos.up(), false);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisExteriorBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, Hand hand, net.minecraft.util.hit.BlockHitResult hit) {
        // A sonic in TARDIS mode is used on the box (to lock or unlock it), so let the sonic have the click.
        net.minecraft.item.ItemStack held = player.getStackInHand(hand);
        if (held.getItem() instanceof com.cerulixteam.gallifrey.item.custom.SonicScrewdriver
                && com.cerulixteam.gallifrey.item.custom.SonicScrewdriver.getMode(held)
                == com.cerulixteam.gallifrey.sonic.SonicMode.TARDIS) {
            return ActionResult.PASS;
        }
        if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.SUCCESS;
        }

        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis)) {
            return ActionResult.PASS;
        }
        if (tardis.isFlightPending()) return ActionResult.PASS;

        tardis.ensureInitialized(serverPlayer);

        if (serverPlayer.isSneaking()) {
            if (!tardis.canPilot(serverPlayer.getUuid())) {
                serverPlayer.sendMessage(Text.literal("Only the TARDIS owner can operate its locks."), true);
                return ActionResult.SUCCESS;
            }
            tardis.setLocked(!tardis.isLocked());
            if (tardis.isLocked()) TardisFlight.setDoors(tardis, false, false);
            world.playSound(null, pos, tardis.isLocked()
                    ? GallifreySounds.POLICE_BOX_DOOR_CLOSE
                    : GallifreySounds.POLICE_BOX_DOOR_OPEN,
                    SoundCategory.BLOCKS, 0.8F, 1.0F);
            serverPlayer.sendMessage(Text.literal(tardis.isLocked()
                    ? "TARDIS locked. Isomorphic security active."
                    : "TARDIS unlocked."), true);
            return ActionResult.SUCCESS;
        }

        if (!tardis.canAccess(serverPlayer.getUuid())) {
            serverPlayer.sendMessage(Text.literal("The TARDIS is locked."), true);
            return ActionResult.SUCCESS;
        }

        // Click to open or close the doors (both sides at once); walk in through them to enter.
        boolean open = !state.get(OPEN);
        TardisFlight.setDoors(tardis, open, true);
        return ActionResult.SUCCESS;
    }
}
