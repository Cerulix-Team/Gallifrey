package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 26.3's poplar trunk: a straight log with 1-4 short sideways branches poking out
 * just below where the leaves start. Same maths as vanilla's PoplarTrunkPlacer.
 */
public class PoplarTrunkPlacer extends TrunkPlacer {
    public static final Codec<PoplarTrunkPlacer> CODEC = RecordCodecBuilder.create(i -> fillTrunkPlacerFields(i)
            .and(i.group(
                    IntProvider.createValidatingCodec(0, 8).fieldOf("trunk_height_above_branches").forGetter((PoplarTrunkPlacer t) -> t.trunkHeightAboveBranches),
                    IntProvider.createValidatingCodec(1, 4).fieldOf("branch_amount").forGetter((PoplarTrunkPlacer t) -> t.branchAmount)))
            .apply(i, PoplarTrunkPlacer::new));

    private final IntProvider trunkHeightAboveBranches;
    private final IntProvider branchAmount;

    public PoplarTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, IntProvider trunkHeightAboveBranches, IntProvider branchAmount) {
        super(baseHeight, heightRandA, heightRandB);
        this.trunkHeightAboveBranches = trunkHeightAboveBranches;
        this.branchAmount = branchAmount;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return Backport26.POPLAR_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random,
                                                 int height, BlockPos startPos, TreeFeatureConfig config) {
        setToDirt(world, replacer, random, startPos.down(), config);
        int branchesAt = height - this.trunkHeightAboveBranches.get(random);

        for (int y = 0; y < height; y++) {
            this.getAndSetState(world, replacer, random, startPos.up(y), config);
            List<Direction> directions = shuffledHorizontal(random);
            if (branchesAt - 1 == y) {
                int branches = this.branchAmount.get(random);
                for (int b = 0; b < branches; b++) {
                    Direction dir = directions.get(b);
                    this.getAndSetState(world, replacer, random, startPos.up(y).offset(dir, 1), config, sideways(dir));
                }
            }
        }

        return List.of(new FoliagePlacer.TreeNode(startPos.up(branchesAt), 0, false));
    }

    static Function<BlockState, BlockState> sideways(Direction dir) {
        return state -> state.withIfExists(PillarBlock.AXIS, dir.getAxis());
    }

    private static List<Direction> shuffledHorizontal(Random random) {
        return Direction.shuffle(random).stream().filter(d -> d.getAxis().isHorizontal()).toList();
    }
}
