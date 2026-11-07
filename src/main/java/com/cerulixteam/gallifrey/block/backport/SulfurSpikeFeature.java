package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Thickness;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Sulfur Caves (26.x): a sulfur spike growing up from a cave floor or down from its ceiling, one to four
 * blocks long, on a patch of sulfur. Stands in for 26.3's "sulfur_spike" and "sulfur_spike_cluster".
 */
public class SulfurSpikeFeature extends Feature<DefaultFeatureConfig> {
    public SulfurSpikeFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        BlockPos origin = context.getOrigin();
        if (!world.isAir(origin)) return false;
        Direction grow;
        BlockPos base;
        BlockPos below = origin.down(), above = origin.up();
        if (world.getBlockState(below).isSolidBlock(world, below)) {
            grow = Direction.UP;
            base = below;
        } else if (world.getBlockState(above).isSolidBlock(world, above)) {
            grow = Direction.DOWN;
            base = above;
        } else {
            return false;
        }
        BlockState sulfur = Backport26.SULFUR[0].getDefaultState();
        for (BlockPos p : BlockPos.iterate(base.add(-1, 0, -1), base.add(1, 0, 1))) {
            if (random.nextInt(3) != 0 && world.getBlockState(p).isSolidBlock(world, p) && world.getBlockState(p).isIn(net.minecraft.registry.tag.BlockTags.BASE_STONE_OVERWORLD)) {
                world.setBlockState(p, random.nextInt(6) == 0 ? Backport26.POTENT_SULFUR.getDefaultState() : sulfur, 2);
            }
        }
        int length = 1 + random.nextInt(4);
        int placed = 0;
        for (int i = 0; i < length; i++) {
            if (!world.isAir(origin.offset(grow, i))) break;
            placed++;
        }
        if (placed == 0) return false;
        for (int i = 0; i < placed; i++) {
            int fromTip = placed - 1 - i;
            Thickness t = fromTip == 0 ? Thickness.TIP : fromTip == 1 ? Thickness.FRUSTUM : i == 0 ? Thickness.BASE : Thickness.MIDDLE;
            world.setBlockState(origin.offset(grow, i), Backport26.SULFUR_SPIKE.getDefaultState()
                    .with(SulfurSpikeBlock.VERTICAL_DIRECTION, grow).with(SulfurSpikeBlock.THICKNESS, t), 2);
        }
        return true;
    }
}
