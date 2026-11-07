package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.List;

/** Puts a few shelf mushrooms on the sides of a tree's trunk (26.3's "shelf_mushroom" tree decorator). */
public class ShelfMushroomTreeDecorator extends TreeDecorator {
    public static final Codec<ShelfMushroomTreeDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability").xmap(ShelfMushroomTreeDecorator::new, d -> d.probability).codec();

    private final float probability;

    public ShelfMushroomTreeDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> getType() {
        return Backport26.SHELF_MUSHROOM_DECORATOR;
    }

    @Override
    public void generate(Generator generator) {
        Random random = generator.getRandom();
        if (random.nextFloat() >= this.probability) return;
        List<BlockPos> logs = generator.getLogPositions();
        if (logs.isEmpty()) return;
        int bottom = logs.get(0).getY();
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count * 3 && count > 0; i++) {
            BlockPos log = logs.get(random.nextInt(logs.size()));
            if (log.getY() < bottom + 1) continue;
            Direction side = Direction.Type.HORIZONTAL.random(random);
            BlockPos at = log.offset(side);
            if (!generator.isAir(at)) continue;
            BlockState mushroom = Backport26.SHELF_MUSHROOM.getDefaultState()
                    .with(ShelfMushroomBlock.FACING, side).with(ShelfMushroomBlock.AGE, random.nextInt(2));
            generator.replace(at, mushroom);
            count--;
        }
    }
}
