package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.List;

/**
 * 1.21.5's "place_on_ground" tree decorator: scatters a block (leaf litter here) on the ground
 * around the base of the tree. Same rules as vanilla's PlaceOnGroundDecorator.
 */
public class PlaceOnGroundTreeDecorator extends TreeDecorator {
    public static final Codec<PlaceOnGroundTreeDecorator> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.POSITIVE_INT.optionalFieldOf("tries", 128).forGetter((PlaceOnGroundTreeDecorator p) -> p.tries),
                    Codecs.NONNEGATIVE_INT.optionalFieldOf("radius", 2).forGetter((PlaceOnGroundTreeDecorator p) -> p.radius),
                    Codecs.NONNEGATIVE_INT.optionalFieldOf("height", 1).forGetter((PlaceOnGroundTreeDecorator p) -> p.height),
                    BlockStateProvider.TYPE_CODEC.fieldOf("block_state_provider").forGetter((PlaceOnGroundTreeDecorator p) -> p.provider))
            .apply(i, PlaceOnGroundTreeDecorator::new));

    private final int tries;
    private final int radius;
    private final int height;
    private final BlockStateProvider provider;

    public PlaceOnGroundTreeDecorator(int tries, int radius, int height, BlockStateProvider provider) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
        this.provider = provider;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return Backport26.PLACE_ON_GROUND_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        List<BlockPos> logs = generator.getLogPositions();
        if (logs.isEmpty()) return;
        BlockPos origin = logs.get(0);
        int minY = origin.getY();
        int minX = origin.getX(), maxX = minX, minZ = origin.getZ(), maxZ = minZ;
        for (BlockPos p : logs) {
            if (p.getY() == minY) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
            }
        }
        minX -= radius; maxX += radius; minZ -= radius; maxZ += radius;
        int lowY = minY - height, highY = minY + height;

        Random random = generator.getRandom();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int i = 0; i < this.tries; i++) {
            pos.set(between(random, minX, maxX), between(random, lowY, highY), between(random, minZ, maxZ));
            this.tryPlaceAbove(generator, pos.toImmutable());
        }
    }

    private void tryPlaceAbove(Generator generator, BlockPos pos) {
        BlockPos above = pos.up();
        var world = generator.getWorld();
        if (world.testBlockState(above, s -> s.isAir() || s.isOf(Blocks.VINE))
                && world.testBlockState(pos, s -> s.isOpaque() && s.isSolid())
                && world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= above.getY()) {
            generator.replace(above, this.provider.get(generator.getRandom(), above));
        }
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
