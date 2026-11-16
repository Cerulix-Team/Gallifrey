package com.cerulixteam.gallifrey.block.backport.trial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Vault (1.21). Use a Trial Key on it (an Ominous Trial Key on an ominous one) and it unlocks and hands out
 * its loot, once per player.
 */
public class VaultBlock extends BlockWithEntity {
    public enum State implements StringIdentifiable {
        INACTIVE("inactive", 6), ACTIVE("active", 12), UNLOCKING("unlocking", 12), EJECTING("ejecting", 12);

        private final String name;
        public final int light;

        State(String name, int light) {
            this.name = name;
            this.light = light;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<State> STATE = EnumProperty.of("vault_state", State.class);
    public static final BooleanProperty OMINOUS = BooleanProperty.of("ominous");

    public VaultBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH).with(STATE, State.INACTIVE).with(OMINOUS, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE, OMINOUS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (world.getBlockEntity(pos) instanceof VaultBlockEntity vault && world instanceof ServerWorld server) {
            return vault.tryUnlock(server, state, player, player.getStackInHand(hand)) ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VaultBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? checkType(type, BackportTrials.VAULT_ENTITY, VaultBlockEntity::clientTick)
                : checkType(type, BackportTrials.VAULT_ENTITY, VaultBlockEntity::serverTick);
    }
}
