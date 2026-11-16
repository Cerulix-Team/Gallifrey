package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Firefly Bush (1.21.5): fireflies drift around it at night. */
public class FireflyBushBlock extends BackportPlantBlock {
    public FireflyBushBlock(Settings settings) {
        super(Soil.DIRT, box(1, 0, 1, 15, 13, 15), settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.getAmbientDarkness() < 4 && !world.getDimension().hasFixedTime()) return;
        if (random.nextInt(3) != 0) return;
        world.addParticle(BackportExtras.FIREFLY_PARTICLE, pos.getX() + random.nextDouble() * 3 - 1, pos.getY() + 0.5 + random.nextDouble() * 2,
                pos.getZ() + random.nextDouble() * 3 - 1, 0, 0, 0);
    }
}
