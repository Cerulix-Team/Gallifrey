package com.cerulixteam.gallifrey.world.tree.decorator;

import com.mojang.serialization.Codec;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

/**
 * Vanilla's leaves vine decorator (used by jungle trees), but placing
 * Prehistoric Vines instead of normal vines. For each side of each leaf block,
 * with the given probability, a vine hangs down up to 5 blocks.
 */
public class PrehistoricLeavesVineTreeDecorator extends TreeDecorator {
    public static final Codec<PrehistoricLeavesVineTreeDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability")
            .xmap(PrehistoricLeavesVineTreeDecorator::new, decorator -> decorator.probability)
            .codec();

    private final float probability;

    public PrehistoricLeavesVineTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return ModTreeDecorators.PREHISTORIC_LEAVES_VINE;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        generator.getLeavesPositions().forEach(pos -> {
            tryHang(random, generator, pos.west(), VineBlock.EAST);
            tryHang(random, generator, pos.east(), VineBlock.WEST);
            tryHang(random, generator, pos.north(), VineBlock.SOUTH);
            tryHang(random, generator, pos.south(), VineBlock.NORTH);
        });
    }

    private void tryHang(Random random, Generator generator, BlockPos pos, BooleanProperty facing) {
        if (random.nextFloat() < this.probability && generator.isAir(pos)) {
            placeVines(pos, facing, generator);
        }
    }

    private static void placeVines(BlockPos pos, BooleanProperty facing, Generator generator) {
        generator.replace(pos, vine(facing));
        BlockPos below = pos.down();
        for (int i = 4; i > 0 && generator.isAir(below); i--) {
            generator.replace(below, vine(facing));
            below = below.down();
        }
    }

    private static net.minecraft.block.BlockState vine(BooleanProperty facing) {
        return GallifreyModBlocks.PREHISTORIC_VINE.getDefaultState().with(facing, true);
    }
}
