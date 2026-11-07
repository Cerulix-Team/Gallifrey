package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.function.Supplier;

/** Eyeblossom (1.21.4): opens at night and closes in the day, in dimensions with a day cycle. */
public class EyeblossomBlock extends FlowerBlock {
    private final boolean open;
    private final Supplier<Block> other;

    public EyeblossomBlock(boolean open, Supplier<Block> other, StatusEffect effect, int seconds, Settings settings) {
        super(effect, seconds, settings);
        this.open = open;
        this.other = other;
    }

    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.getDimension().natural()) return;
        boolean shouldBeOpen = !world.isDay();
        if (shouldBeOpen != this.open) {
            world.setBlockState(pos, this.other.get().getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, shouldBeOpen ? SoundEvents.BLOCK_AZALEA_LEAVES_PLACE : SoundEvents.BLOCK_AZALEA_LEAVES_BREAK,
                    SoundCategory.BLOCKS, 0.6F, shouldBeOpen ? 1.3F : 0.9F);
        }
    }
}
