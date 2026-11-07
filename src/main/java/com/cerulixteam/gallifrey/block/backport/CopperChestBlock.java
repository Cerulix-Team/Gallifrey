package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/**
 * Copper Chest (1.21.9): a chest that rusts. The two halves of a double chest rust together.
 * {@link #level} is used for its texture; waxed chests have {@code rusting} false.
 */
public class CopperChestBlock extends ChestBlock implements Oxidizable {
    public final OxidationLevel level;
    private final boolean rusting;

    public CopperChestBlock(OxidationLevel level, boolean rusting, Settings settings) {
        super(settings, () -> BackportExtras.COPPER_CHEST_ENTITY);
        this.level = level;
        this.rusting = rusting;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CopperChestBlockEntity(pos, state);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return this.rusting && Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.rusting) return;
        if (state.get(CHEST_TYPE) == ChestType.LEFT) return; // the right half rusts both
        BlockState before = state;
        this.tickDegradation(state, world, pos, random);
        BlockState after = world.getBlockState(pos);
        if (after != before && state.get(CHEST_TYPE) == ChestType.RIGHT) {
            BlockPos other = pos.offset(ChestBlock.getFacing(state));
            BlockState otherState = world.getBlockState(other);
            if (otherState.getBlock() instanceof ChestBlock) {
                world.setBlockState(other, after.getBlock().getStateWithProperties(otherState), Block.NOTIFY_ALL);
            }
        }
    }

    /** Rusting, scraping and waxing swap the block: keep the chest's contents instead of spilling them. */
    @Override
    public void onStateReplaced(BlockState state, net.minecraft.world.World world, BlockPos pos, BlockState newState, boolean moved) {
        if (newState.getBlock() instanceof CopperChestBlock) return;
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return this.level;
    }
}
