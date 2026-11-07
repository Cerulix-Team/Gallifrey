package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.List;

/** Puts a Creaking Heart in a pale oak's trunk, between two logs (26.3's "creaking_heart" tree decorator). */
public class CreakingHeartTreeDecorator extends TreeDecorator {
    public static final Codec<CreakingHeartTreeDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability").xmap(CreakingHeartTreeDecorator::new, d -> d.probability).codec();

    private final float probability;

    public CreakingHeartTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return BackportBiomes.CREAKING_HEART_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        if (random.nextFloat() >= this.probability) return;
        List<BlockPos> logs = generator.getLogPositions();
        if (logs.size() < 3) return;
        for (int i = 0; i < 8; i++) {
            BlockPos log = logs.get(random.nextInt(logs.size()));
            if (logs.contains(log.up()) && logs.contains(log.down())) {
                generator.replace(log, BackportExtras.CREAKING_HEART.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Y)
                        .with(CreakingHeartBlock.STATE, CreakingHeartBlock.State.DORMANT));
                return;
            }
        }
    }
}
