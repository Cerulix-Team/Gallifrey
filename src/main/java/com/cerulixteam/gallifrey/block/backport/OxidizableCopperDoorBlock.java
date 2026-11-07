package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** The rusting (unwaxed) version of {@link CopperDoorBlock}. Rusts over time; scrape it with an axe, wax it with honeycomb. */
public class OxidizableCopperDoorBlock extends CopperDoorBlock implements Oxidizable {
    private final OxidationLevel level;

    public OxidizableCopperDoorBlock(OxidationLevel level, Settings settings, BlockSetType type) {
        super(settings, type);
        this.level = level;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) this.tickDegradation(state, world, pos, random);
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
