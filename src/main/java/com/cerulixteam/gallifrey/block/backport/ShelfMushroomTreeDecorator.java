package com.cerulixteam.gallifrey.block.backport;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.List;

/**
 * 26.3's "shelf_mushroom" tree decorator. On a standing tree it puts shelf mushrooms on two
 * neighbouring sides of the trunk, 1-4 blocks up, never stacked. On a fallen log it lines
 * the two long sides, never next to each other. Same rules as vanilla's ShelfMushroomDecorator.
 */
public class ShelfMushroomTreeDecorator extends TreeDecorator {
    public static final Codec<ShelfMushroomTreeDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability").xmap(ShelfMushroomTreeDecorator::new, d -> d.probability).codec();

    private static final float PER_SIDE_CHANCE = 0.25F;

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
        if (logs.get(0).getY() == logs.get(logs.size() - 1).getY()) {
            onFallenLog(generator, logs, random);
        } else {
            onStandingTree(generator, logs, random);
        }
    }

    private static void onStandingTree(Generator generator, List<BlockPos> logs, Random random) {
        Direction first = Direction.Type.HORIZONTAL.random(random);
        Direction[] sides = {first, first.rotateYClockwise()};
        int baseY = logs.get(0).getY();
        for (BlockPos log : logs) {
            int dy = log.getY() - baseY;
            if (dy < 1 || dy > 4) continue;
            for (Direction side : sides) {
                if (random.nextFloat() > PER_SIDE_CHANCE) continue;
                BlockPos at = log.offset(side);
                if (canGoAt(generator, at) && !isShelf(generator, at.down())) {
                    place(generator, at, side, random);
                    break;
                }
            }
        }
    }

    private static void onFallenLog(Generator generator, List<BlockPos> logs, Random random) {
        BlockPos first = logs.get(0);
        BlockPos last = logs.get(logs.size() - 1);
        Direction[] sides = first.getX() != last.getX()
                ? new Direction[]{Direction.NORTH, Direction.SOUTH}
                : new Direction[]{Direction.EAST, Direction.WEST};
        for (BlockPos log : logs) {
            for (Direction side : sides) {
                if (random.nextFloat() > PER_SIDE_CHANCE) continue;
                BlockPos at = log.offset(side);
                if (canGoAt(generator, at) && !shelfBeside(generator, at) && !shelfBeside(generator, log)) {
                    place(generator, at, side, random);
                }
            }
        }
    }

    private static void place(Generator generator, BlockPos at, Direction side, Random random) {
        BlockState mushroom = Backport26.SHELF_MUSHROOM.getDefaultState()
                .with(ShelfMushroomBlock.AGE, random.nextInt(2)).with(ShelfMushroomBlock.FACING, side);
        generator.replace(at, mushroom);
    }

    private static boolean canGoAt(Generator generator, BlockPos pos) {
        return generator.getWorld().testBlockState(pos, BlockState::isReplaceable) && !waterAtOrBeside(generator, pos);
    }

    private static boolean waterAtOrBeside(Generator generator, BlockPos pos) {
        if (isWater(generator, pos)) return true;
        for (Direction d : Direction.Type.HORIZONTAL) {
            if (isWater(generator, pos.offset(d))) return true;
        }
        return false;
    }

    private static boolean isWater(Generator generator, BlockPos pos) {
        return generator.getWorld().testBlockState(pos, s -> s.isOf(Blocks.WATER));
    }

    private static boolean isShelf(Generator generator, BlockPos pos) {
        return generator.getWorld().testBlockState(pos, s -> s.isOf(Backport26.SHELF_MUSHROOM));
    }

    private static boolean shelfBeside(Generator generator, BlockPos pos) {
        for (Direction d : Direction.Type.HORIZONTAL) {
            if (isShelf(generator, pos.offset(d))) return true;
        }
        return false;
    }
}
