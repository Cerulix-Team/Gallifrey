package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

/**
 * 26.3's poplar canopy: a tall, slightly lopsided rhombus of leaves with random side holes,
 * and a few logs poking through the middle. Same maths as vanilla's PoplarFoliagePlacer.
 */
public class PoplarFoliagePlacer extends FoliagePlacer {
    public static final Codec<PoplarFoliagePlacer> CODEC = RecordCodecBuilder.create(i -> fillFoliagePlacerFields(i)
            .and(i.group(
                    IntProvider.createValidatingCodec(5, 16).fieldOf("height").forGetter((PoplarFoliagePlacer p) -> p.height),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("side_hole_chance").forGetter((PoplarFoliagePlacer p) -> p.sideHoleChance)))
            .apply(i, PoplarFoliagePlacer::new));

    private final IntProvider height;
    private final float sideHoleChance;

    public PoplarFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider height, float sideHoleChance) {
        super(radius, offset);
        this.height = height;
        this.sideHoleChance = sideHoleChance;
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return Backport26.POPLAR_FOLIAGE_PLACER;
    }

    @Override
    protected void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight,
                            TreeNode node, int foliageHeight, int radius, int offset) {
        boolean giant = node.isGiantTrunk();
        BlockPos origin = node.getCenter().up(offset);
        int r = radius + node.getFoliageRadius() - 1;
        boolean flip = random.nextBoolean();
        int h = foliageHeight;

        this.row(world, placer, random, config, origin, r - 2, h - 1, giant, h, flip);
        this.row(world, placer, random, config, origin, r - 1, h - 2, giant, h, flip);
        this.row(world, placer, random, config, origin, r - 1, h - 3, giant, h, flip);
        for (int y = h - 4; y >= 1; y--) {
            this.row(world, placer, random, config, origin, r, y, giant, h, flip);
        }
        this.replaceLeavesWithLog(world, placer, random, config, origin, r, h - 4, giant, h, flip);
        this.row(world, placer, random, config, origin, r - 1, 0, giant, h, flip);
        this.row(world, placer, random, config, origin, MathHelper.clamp(r - 2, 1, 2), -1, giant, h, flip);
    }

    private void row(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos origin,
                     int r, int y, boolean giant, int h, boolean flip) {
        int extra = giant ? 1 : 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -r; dx <= r + extra; dx++) {
            for (int dz = -r; dz <= r + extra; dz++) {
                if (!this.skip(random, dx, y, dz, r, h, flip)) {
                    pos.set(origin, dx, y, dz);
                    placeFoliageBlock(world, placer, random, config, pos);
                }
            }
        }
    }

    private void replaceLeavesWithLog(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos origin,
                                      int r, int y, boolean giant, int h, boolean flip) {
        int extra = giant ? 1 : 0;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -r; dx <= r + extra; dx++) {
            for (int dz = -r; dz <= r + extra; dz++) {
                int adx = MathHelper.abs(dx);
                int adz = MathHelper.abs(dz);
                if (inRhombus(r, adx, adz, cornerCut(dx, dz, r, partial(h, y), flip), 2)
                        && (adz == 0 && r - adx >= 4 || adx == 0 && r - adz >= 4)) {
                    pos.set(origin, dx, y, dz);
                    BlockPos at = pos.toImmutable();
                    BlockState leaves = config.foliageProvider.get(random, at);
                    if (world.testBlockState(at, s -> s.equals(leaves))) {
                        Direction axisDir = Direction.from(adz == 0 ? Direction.Axis.X : Direction.Axis.Z, Direction.AxisDirection.POSITIVE);
                        placer.placeBlock(at, PoplarTrunkPlacer.sideways(axisDir).apply(config.trunkProvider.get(random, at)));
                    }
                }
            }
        }
    }

    private boolean skip(Random random, int dx, int y, int dz, int r, int h, boolean flip) {
        boolean partial = partial(h, y);
        int cut = cornerCut(dx, dz, r, partial, flip);
        int adx = MathHelper.abs(dx);
        int adz = MathHelper.abs(dz);
        if (partial && (adx == r || adz == r)) return true;
        int hole = random.nextFloat() <= this.sideHoleChance ? 1 : 0;
        return !inRhombus(r, adx, adz, cut, hole);
    }

    private static int cornerCut(int dx, int dz, int r, boolean partial, boolean flip) {
        boolean smallCorner = flip ? (dx > 0 && dz > 0 || dz < 0 && dx < 0) : (dx > 0 && dz < 0 || dz > 0 && dx < 0);
        return smallCorner ? r - 1 : (partial ? r + 1 : r);
    }

    private static boolean inRhombus(int r, int adx, int adz, int cut, int extraRemoval) {
        return adx + adz <= r * 2 - (cut + extraRemoval);
    }

    private static boolean partial(int h, int y) {
        return h - 1 == y || h - 2 == y;
    }

    @Override
    public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
        return this.height.get(random);
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return false; // not used: the shape is worked out in row() instead
    }
}
