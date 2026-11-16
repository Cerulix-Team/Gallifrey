package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** An exposed, weathered or oxidized Lightning Rod (1.21.9), rusting on in time like other copper. */
public class OxidizableLightningRodBlock extends LightningRodBlock implements Oxidizable {
    private final OxidationLevel level;

    public OxidizableLightningRodBlock(OxidationLevel level, Settings settings) {
        super(settings);
        this.level = level;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    public OxidationLevel getDegradationLevel() {
        return this.level;
    }
}
