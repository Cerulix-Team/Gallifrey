package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.function.Supplier;

/** Leaves that drop falling-leaf particles underneath, like cherry leaves. 1 in "chance" per display tick: vanilla uses 1 in 50 for pale oak and 1 in 100 for poplars. */
public class BackportLeavesBlock extends LeavesBlock {
    private final Supplier<ParticleEffect> particle;
    private final int chance;

    public BackportLeavesBlock(Supplier<ParticleEffect> particle, int chance, Settings settings) {
        super(settings);
        this.particle = particle;
        this.chance = chance;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(this.chance) != 0) return;
        BlockPos below = pos.down();
        if (world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP)) return;
        world.addParticle(this.particle.get(), pos.getX() + random.nextDouble(), pos.getY() - 0.05, pos.getZ() + random.nextDouble(), 0, 0, 0);
    }
}
