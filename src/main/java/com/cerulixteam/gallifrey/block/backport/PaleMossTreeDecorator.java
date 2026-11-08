package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.Comparator;
import java.util.List;

/**
 * 1.21.4's "pale_moss" tree decorator. Most pale oaks get a patch of pale moss on the ground around
 * their base (the "gallifrey:pale_moss_patch" feature), and pale hanging moss under some leaves and
 * trunk blocks. Same rules as vanilla's PaleMossDecorator.
 */
public class PaleMossTreeDecorator extends TreeDecorator {
    public static final Codec<PaleMossTreeDecorator> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter((PaleMossTreeDecorator p) -> p.leavesProbability),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter((PaleMossTreeDecorator p) -> p.trunkProbability),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter((PaleMossTreeDecorator p) -> p.groundProbability))
            .apply(i, PaleMossTreeDecorator::new));

    private static final RegistryKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", "pale_moss_patch"));

    private final float leavesProbability;
    private final float trunkProbability;
    private final float groundProbability;

    public PaleMossTreeDecorator(float leavesProbability, float trunkProbability, float groundProbability) {
        this.leavesProbability = leavesProbability;
        this.trunkProbability = trunkProbability;
        this.groundProbability = groundProbability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return Backport26.PALE_MOSS_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        List<BlockPos> logs = Util.copyShuffled(generator.getLogPositions(), random);
        if (logs.isEmpty()) return;
        BlockPos origin = logs.stream().min(Comparator.comparingInt(BlockPos::getY)).orElseThrow();

        if (random.nextFloat() < this.groundProbability && generator.getWorld() instanceof StructureWorldAccess world) {
            world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getOrEmpty(PALE_MOSS_PATCH).ifPresent(patch ->
                    patch.generate(world, world.toServerWorld().getChunkManager().getChunkGenerator(), random, origin.up()));
        }

        for (BlockPos pos : generator.getLogPositions()) {
            if (random.nextFloat() < this.trunkProbability && generator.isAir(pos.down())) hang(pos.down(), generator);
        }
        for (BlockPos pos : generator.getLeavesPositions()) {
            if (random.nextFloat() < this.leavesProbability && generator.isAir(pos.down())) hang(pos.down(), generator);
        }
    }

    private static void hang(BlockPos pos, Generator generator) {
        while (generator.isAir(pos.down()) && !(generator.getRandom().nextFloat() < 0.5F)) {
            generator.replace(pos, BackportBlocks.PALE_HANGING_MOSS.getDefaultState().with(PaleHangingMossBlock.TIP, false));
            pos = pos.down();
        }
        generator.replace(pos, BackportBlocks.PALE_HANGING_MOSS.getDefaultState().with(PaleHangingMossBlock.TIP, true));
    }
}
