package com.cerulixteam.gallifrey.block.custom;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import com.cerulixteam.gallifrey.networking.ModPackets;

public class SonicWorkshopBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    /** The Geo model is a full 16x16 footprint but only 9 pixels tall. */
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 9, 16);

    public SonicWorkshopBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // GeoBlockRenderer rotates the model from this property, so the model's
        // front points back toward the player after placement.
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
        return new SonicWorkshopBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof SonicWorkshopBlockEntity workshop) {
                ItemStack sonic = workshop.removeSonic();
                if (!sonic.isEmpty()) {
                    Block.dropStack(world, pos, sonic);
                }
            }
            world.removeBlockEntity(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        // Server only: charges an installed Sonic. The client never ticks it.
        if (world.isClient || type != GallifreyModBlockEntities.SONIC_WORKSHOP_BLOCK_ENTITY) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<SonicWorkshopBlockEntity>) SonicWorkshopBlockEntity::tick;
    }

    /*
     * INTERACTION
     * ===========
     * The whole workshop acts as the Sonic port - no aiming at the gold holder:
     *   - Empty workshop + holding a Sonic   -> insert it
     *   - Sonic installed, sneak + empty hand -> take it back out
     *   - Sonic installed, otherwise          -> open the workshop GUI
     *
     * (The old version only accepted clicks on the front face, but the holder is on
     * the top of the model, so clicking it hit the top face and was ignored. The
     * Sonic never went in, and the GUI - which needs an installed Sonic - never opened.)
     *
     * Sneaking with an empty hand still reaches onUse (vanilla only skips the block
     * when you sneak while holding something), so sneak-to-remove works.
     */
    @Override
    public ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit) {

        if (!(world.getBlockEntity(pos) instanceof SonicWorkshopBlockEntity workshop)) {
            return ActionResult.PASS;
        }

        ItemStack held = player.getStackInHand(hand);

        // Only the main hand drives the workshop, so one click never fires twice.
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        // Client: claim the click so a held Sonic's own right-click doesn't run.
        // The server does the real work and syncs the result back.
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (!workshop.hasSonic()) {
            if (held.getItem() instanceof SonicScrewdriver) {
                ItemStack inserted = held.copy();
                inserted.setCount(1);
                workshop.setSonic(inserted);
                if (!player.getAbilities().creativeMode) {
                    held.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.6F, 1.4F);
            } else {
                player.sendMessage(Text.translatable("block.gallifrey.sonic_workshop.needs_sonic"), true);
            }
            return ActionResult.CONSUME;
        }

        if (player.isSneaking() && held.isEmpty()) {
            ItemStack removed = workshop.removeSonic();
            if (!removed.isEmpty()) {
                player.setStackInHand(hand, removed);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.6F, 1.4F);
            }
            return ActionResult.CONSUME;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            net.minecraft.network.PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            ServerPlayNetworking.send(serverPlayer, ModPackets.OPEN_SONIC_WORKSHOP, buf);
        }
        return ActionResult.CONSUME;
    }
}
