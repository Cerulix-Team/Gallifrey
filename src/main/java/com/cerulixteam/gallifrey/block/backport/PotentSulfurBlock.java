package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * Potent Sulfur (26.2). Under 1 to 4 blocks of water source it bubbles:
 * <ul>
 *   <li>on its own, it makes a cloud of nausea gas on the water's surface, up to 3 blocks around;</li>
 *   <li>on top of magma it's a geyser that erupts now and then, and on top of lava one that never stops,
 *   pushing anything above it upwards.</li>
 * </ul>
 */
public class PotentSulfurBlock extends Block {
    private static final int MAX_WATER = 4;

    public PotentSulfurBlock(Settings settings) {
        super(settings);
    }

    /** How many water source blocks are stacked on top (0 to 4), stopping at the first that isn't one. */
    private static int water(World world, BlockPos pos) {
        int depth = 0;
        for (int i = 1; i <= MAX_WATER + 1; i++) {
            if (!world.getFluidState(pos.up(i)).isOf(Fluids.WATER)) break;
            depth = i;
        }
        return depth;
    }

    private static boolean onLava(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isOf(Blocks.LAVA) || world.getFluidState(pos.down()).isIn(FluidTags.LAVA);
    }

    private static boolean onMagma(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).isOf(Blocks.MAGMA_BLOCK);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, 20);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        world.scheduleBlockTick(pos, this, 20);
        return state;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int depth = water(world, pos);
        if (depth == 0 || depth > MAX_WATER) return;
        double surface = pos.getY() + depth + 1;

        if (onLava(world, pos) || (onMagma(world, pos) && random.nextInt(4) == 0)) {
            // Geyser: push everything in the column above upwards.
            Box column = new Box(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, surface + 6, pos.getZ() + 1);
            for (Entity entity : world.getOtherEntities(null, column)) {
                Vec3d v = entity.getVelocity();
                entity.setVelocity(v.x, Math.max(v.y, 1.1), v.z);
                entity.velocityModified = true;
                entity.fallDistance = 0;
            }
            world.spawnParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, surface, pos.getZ() + 0.5, 20, 0.2, 1.5, 0.2, 0.05);
            world.playSound(null, pos.up(depth), SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 1.0F, 0.7F);
        } else {
            // Gas: a nausea cloud on the surface, spreading up to 3 blocks across the water.
            Box gas = new Box(pos.getX() - 3, surface - 0.5, pos.getZ() - 3, pos.getX() + 4, surface + 1.5, pos.getZ() + 4);
            for (LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, gas)) {
                if (world.getFluidState(entity.getBlockPos().down()).isOf(Fluids.WATER) || entity.isTouchingWater()) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
                }
            }
            world.spawnParticles(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.5, surface + 0.2, pos.getZ() + 0.5, 12, 2.0, 0.2, 2.0, 1.0);
        }
        world.scheduleBlockTick(pos, this, 20);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int depth = water(world, pos);
        if (depth == 0 || depth > MAX_WATER) return;
        for (int i = 0; i < 3; i++) {
            world.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.04, 0);
        }
    }
}
