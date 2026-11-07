package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Crafter (1.21): a 3x3 crafting grid that crafts when it gets a redstone pulse and spits the result out of
 * its front (into a container if there is one). Click an empty slot in its screen to switch it off, so
 * hoppers skip it. A comparator reads how many slots are filled or switched off.
 */
public class CrafterBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<JigsawOrientation> ORIENTATION = Properties.ORIENTATION;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    public static final BooleanProperty CRAFTING = BooleanProperty.of("crafting");

    public CrafterBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(ORIENTATION, JigsawOrientation.NORTH_UP).with(TRIGGERED, false).with(CRAFTING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED, CRAFTING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getPlayerLookDirection().getOpposite();
        Direction up = switch (facing) {
            case DOWN -> ctx.getHorizontalPlayerFacing().getOpposite();
            case UP -> ctx.getHorizontalPlayerFacing();
            default -> Direction.UP;
        };
        return this.getDefaultState().with(ORIENTATION, JigsawOrientation.byDirections(facing, up))
                .with(TRIGGERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrafterBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter) {
            player.openHandledScreen(crafter);
        }
        return ActionResult.success(world.isClient);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos);
        boolean triggered = state.get(TRIGGERED);
        if (powered && !triggered) {
            world.scheduleBlockTick(pos, this, 4);
            world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
        } else if (!powered && triggered) {
            world.setBlockState(pos, state.with(TRIGGERED, false).with(CRAFTING, false), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!(world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter)) return;
        Optional<CraftingRecipe> recipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, crafter, world);
        if (recipe.isEmpty()) {
            world.syncWorldEvent(1050, pos, 0); // dispenser "click": nothing to craft
            return;
        }
        ItemStack result = recipe.get().craft(crafter, world.getRegistryManager());
        if (result.isEmpty()) return;
        if (result.getItem() instanceof net.minecraft.item.BlockItem bi && bi.getBlock() instanceof CrafterBlock) {
            for (net.minecraft.server.network.ServerPlayerEntity p : world.getPlayers(p -> p.squaredDistanceTo(pos.toCenterPos()) < 25.0)) {
                com.cerulixteam.gallifrey.block.backport.BackportAdvancements.grant(p, "crafters_crafting_crafters");
            }
        }
        DefaultedList<ItemStack> remainders = recipe.get().getRemainder(crafter);
        Direction facing = state.get(ORIENTATION).getFacing();
        this.eject(world, pos, facing, result);
        for (int i = 0; i < 9; i++) {
            ItemStack in = crafter.getStack(i);
            if (!in.isEmpty()) in.decrement(1);
            ItemStack left = remainders.get(i);
            if (!left.isEmpty()) {
                if (crafter.getStack(i).isEmpty()) crafter.setStack(i, left);
                else this.eject(world, pos, facing, left);
            }
        }
        crafter.markDirty();
        world.setBlockState(pos, world.getBlockState(pos).with(CRAFTING, true), Block.NOTIFY_LISTENERS);
        world.playSound(null, pos, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_CRAFTER_CRAFT, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private void eject(ServerWorld world, BlockPos pos, Direction facing, ItemStack stack) {
        BlockPos target = pos.offset(facing);
        Inventory inventory = net.minecraft.block.entity.HopperBlockEntity.getInventoryAt(world, target);
        ItemStack left = stack;
        if (inventory != null) {
            left = net.minecraft.block.entity.HopperBlockEntity.transfer(null, inventory, stack.copy(), facing.getOpposite());
        }
        if (!left.isEmpty()) {
            Vec3d at = Vec3d.ofCenter(pos).add(facing.getOffsetX() * 0.7, facing.getOffsetY() * 0.7 - 0.15, facing.getOffsetZ() * 0.7);
            ItemEntity item = new ItemEntity(world, at.x, at.y, at.z, left);
            item.setVelocity(facing.getOffsetX() * 0.1, facing.getOffsetY() * 0.1 + 0.05, facing.getOffsetZ() * 0.1);
            world.spawnEntity(item);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter) ItemScatterer.spawn(world, pos, crafter);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter ? crafter.comparatorOutput() : 0;
    }
}
