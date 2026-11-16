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
 * Vanilla's trunk vine decorator (used by giant jungle trees), but placing
 * Prehistoric Vines instead of normal vines. Same odds: each side of every
 * log gets a vine 2 times in 3, if that spot is air.
 */
public class PrehistoricTrunkVineTreeDecorator extends TreeDecorator {
    public static final PrehistoricTrunkVineTreeDecorator INSTANCE = new PrehistoricTrunkVineTreeDecorator();
    public static final Codec<PrehistoricTrunkVineTreeDecorator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected TreeDecoratorType<?> getType() {
        return ModTreeDecorators.PREHISTORIC_TRUNK_VINE;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        generator.getLogPositions().forEach(pos -> {
            tryVine(random, generator, pos.west(), VineBlock.EAST);
            tryVine(random, generator, pos.east(), VineBlock.WEST);
            tryVine(random, generator, pos.north(), VineBlock.SOUTH);
            tryVine(random, generator, pos.south(), VineBlock.NORTH);
        });
    }

    private static void tryVine(Random random, Generator generator, BlockPos pos, BooleanProperty facing) {
        if (random.nextInt(3) > 0 && generator.isAir(pos)) {
            generator.replace(pos, GallifreyModBlocks.PREHISTORIC_VINE.getDefaultState().with(facing, true));
        }
    }
}
