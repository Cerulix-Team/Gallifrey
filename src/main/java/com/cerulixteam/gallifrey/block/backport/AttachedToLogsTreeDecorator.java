package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.List;

/**
 * 1.21.5's "attached_to_logs" tree decorator: puts a block (a brown mushroom on fallen poplars)
 * next to some of the logs. Same rules as vanilla's AttachedToLogsDecorator.
 */
public class AttachedToLogsTreeDecorator extends TreeDecorator {
    public static final Codec<AttachedToLogsTreeDecorator> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((AttachedToLogsTreeDecorator p) -> p.probability),
                    BlockStateProvider.TYPE_CODEC.fieldOf("block_provider").forGetter((AttachedToLogsTreeDecorator p) -> p.provider),
                    Codecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter((AttachedToLogsTreeDecorator p) -> p.directions))
            .apply(i, AttachedToLogsTreeDecorator::new));

    private final float probability;
    private final BlockStateProvider provider;
    private final List<Direction> directions;

    public AttachedToLogsTreeDecorator(float probability, BlockStateProvider provider, List<Direction> directions) {
        this.probability = probability;
        this.provider = provider;
        this.directions = directions;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return Backport26.ATTACHED_TO_LOGS_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        for (BlockPos log : Util.copyShuffled(generator.getLogPositions(), random)) {
            Direction dir = Util.getRandom(this.directions, random);
            BlockPos at = log.offset(dir);
            if (random.nextFloat() <= this.probability && generator.isAir(at)) {
                generator.replace(at, this.provider.get(random, at));
            }
        }
    }
}
