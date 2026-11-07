package com.cerulixteam.gallifrey.mixin.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.RodBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Vanilla's Lightning Rod rusts into an Exposed one (1.21.9). Vanilla 1.20.1's rod doesn't tick, so this
 * gives it random ticks, at the same speed copper blocks rust.
 */
@Mixin(LightningRodBlock.class)
public abstract class LightningRodOxidationMixin extends RodBlock {
    protected LightningRodOxidationMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.isOf(Blocks.LIGHTNING_ROD) || super.hasRandomTicks(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.isOf(Blocks.LIGHTNING_ROD)) {
            // As Oxidizable.tickDegradation, without the neighbour check copper blocks do.
            if (random.nextFloat() < 0.05688889F && random.nextFloat() < 0.5625F) {
                Oxidizable.getIncreasedOxidationBlock(state.getBlock())
                        .ifPresent(next -> world.setBlockState(pos, next.getStateWithProperties(state), Block.NOTIFY_ALL));
            }
            return;
        }
        super.randomTick(state, world, pos, random);
    }
}
