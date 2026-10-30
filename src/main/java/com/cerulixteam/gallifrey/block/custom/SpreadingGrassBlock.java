package com.cerulixteam.gallifrey.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SnowBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.function.Supplier;

/**
 * A grass block that behaves like vanilla grass, but for its own dirt:
 *  - turns back into its dirt when covered
 *  - spreads onto nearby dirt of the same kind when there is enough light
 *  - can be bone-mealed, which scatters the given plants on nearby grass
 *
 * The dirt and plants are passed as Suppliers because those blocks are
 * registered after the grass block in GallifreyModBlocks.
 */
public class SpreadingGrassBlock extends Block implements Fertilizable {
    private final Supplier<Block> dirt;
    private final Supplier<List<Block>> plants;

    public SpreadingGrassBlock(Supplier<Block> dirt, Supplier<List<Block>> plants, Settings settings) {
        super(settings.ticksRandomly());
        this.dirt = dirt;
        this.plants = plants;
    }

    /** The dirt this grass turns into (used by the sheep grazing mixin too). */
    public Block getDirt() {
        return this.dirt.get();
    }

    /** True if grass can stay alive at this position (nothing solid or watery on top). */
    private static boolean canSurvive(WorldView world, BlockPos pos) {
        BlockPos abovePos = pos.up();
        BlockState above = world.getBlockState(abovePos);
        if (above.isOf(Blocks.SNOW) && above.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (above.getFluidState().getLevel() == 8) {
            return false;
        }
        return above.getOpacity(world, abovePos) < world.getMaxLightLevel();
    }

    private static boolean canSpread(WorldView world, BlockPos pos) {
        return canSurvive(world, pos) && !world.getFluidState(pos.up()).isIn(FluidTags.WATER);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Block dirtBlock = this.dirt.get();
        if (!canSurvive(world, pos)) {
            world.setBlockState(pos, dirtBlock.getDefaultState());
            return;
        }
        if (world.getLightLevel(pos.up()) >= 9) {
            BlockState grass = this.getDefaultState();
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (world.getBlockState(target).isOf(dirtBlock) && canSpread(world, target)) {
                    world.setBlockState(target, grass);
                }
            }
        }
    }

    // ---- Bone meal ----

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return world.getBlockState(pos.up()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        List<Block> options = this.plants.get();
        if (options.isEmpty()) {
            return;
        }
        for (int i = 0; i < 32; i++) {
            BlockPos target = pos.up().add(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            if (!world.getBlockState(target.down()).isOf(this) || !world.getBlockState(target).isAir()) {
                continue;
            }
            if (random.nextInt(3) == 0) {
                continue;
            }
            BlockState plant = options.get(random.nextInt(options.size())).getDefaultState();
            if (plant.canPlaceAt(world, target)) {
                world.setBlockState(target, plant, Block.NOTIFY_ALL);
            }
        }
    }
}
