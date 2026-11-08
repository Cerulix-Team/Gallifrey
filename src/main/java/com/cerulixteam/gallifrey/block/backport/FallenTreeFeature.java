package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 1.21.5's "fallen_tree" feature: a one-block stump with a log lying on the ground 2-3 blocks
 * away from it. Same rules as vanilla's FallenTreeFeature.
 */
public class FallenTreeFeature extends Feature<FallenTreeFeature.Config> {
    private static final TagKey<Block> REPLACEABLE_BY_TREES = TagKey.of(RegistryKeys.BLOCK, new Identifier("minecraft", "replaceable_by_trees"));

    public record Config(BlockStateProvider trunkProvider, IntProvider logLength,
                         List<TreeDecorator> stumpDecorators, List<TreeDecorator> logDecorators) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(i -> i.group(
                        BlockStateProvider.TYPE_CODEC.fieldOf("trunk_provider").forGetter(Config::trunkProvider),
                        IntProvider.createValidatingCodec(0, 16).fieldOf("log_length").forGetter(Config::logLength),
                        TreeDecorator.TYPE_CODEC.listOf().fieldOf("stump_decorators").forGetter(Config::stumpDecorators),
                        TreeDecorator.TYPE_CODEC.listOf().fieldOf("log_decorators").forGetter(Config::logDecorators))
                .apply(i, Config::new));
    }

    public FallenTreeFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        Config config = context.getConfig();
        BlockPos origin = context.getOrigin();

        BlockPos stump = this.placeLog(world, random, config, origin.mutableCopy(), Function.identity());
        decorate(world, random, Set.of(stump), config.stumpDecorators());

        Direction direction = Direction.Type.HORIZONTAL.random(random);
        int length = config.logLength().get(random) - 2;
        BlockPos.Mutable start = origin.offset(direction, 2 + random.nextInt(2)).mutableCopy();
        this.findGround(world, start);
        if (this.fits(world, length, start, direction)) {
            Set<BlockPos> log = new HashSet<>();
            for (int i = 0; i < length; i++) {
                log.add(this.placeLog(world, random, config, start, PoplarTrunkPlacer.sideways(direction)));
                start.move(direction);
            }
            decorate(world, random, log, config.logDecorators());
        }
        return true;
    }

    private void findGround(StructureWorldAccess world, BlockPos.Mutable pos) {
        pos.move(Direction.UP, 1);
        for (int i = 0; i < 6; i++) {
            if (canReplace(world, pos) && overSolidGround(world, pos)) return;
            pos.move(Direction.DOWN);
        }
    }

    private boolean fits(StructureWorldAccess world, int length, BlockPos.Mutable start, Direction direction) {
        int gap = 0;
        for (int i = 0; i < length; i++) {
            if (!canReplace(world, start)) return false;
            if (!overSolidGround(world, start)) {
                if (++gap > 2) return false;
            } else {
                gap = 0;
            }
            start.move(direction);
        }
        start.move(direction.getOpposite(), length);
        return true;
    }

    private BlockPos placeLog(StructureWorldAccess world, Random random, Config config, BlockPos.Mutable pos,
                              Function<BlockState, BlockState> modifier) {
        world.setBlockState(pos, modifier.apply(config.trunkProvider().get(random, pos)), Block.NOTIFY_ALL);
        this.markBlocksAboveForPostProcessing(world, pos);
        return pos.toImmutable();
    }

    private static void decorate(StructureWorldAccess world, Random random, Set<BlockPos> logs, List<TreeDecorator> decorators) {
        if (decorators.isEmpty()) return;
        BiConsumer<BlockPos, BlockState> setter = (pos, state) -> world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE);
        TreeDecorator.Generator generator = new TreeDecorator.Generator(world, setter, random, logs, Set.of(), Set.of());
        decorators.forEach(d -> d.generate(generator));
    }

    private static boolean canReplace(TestableWorld world, BlockPos pos) {
        return world.testBlockState(pos, s -> s.isAir() || s.isIn(REPLACEABLE_BY_TREES));
    }

    private static boolean overSolidGround(StructureWorldAccess world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP);
    }
}
