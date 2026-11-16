package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.ChainBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** The rusting (unwaxed) version of {@link ChainBlock}. Rusts over time; scrape it with an axe, wax it with honeycomb. */
public class OxidizableChainBlock extends ChainBlock implements Oxidizable {
    private final OxidationLevel level;

    public OxidizableChainBlock(OxidationLevel level, Settings settings) {
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
