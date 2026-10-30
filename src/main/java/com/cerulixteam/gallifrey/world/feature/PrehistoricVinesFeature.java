package com.cerulixteam.gallifrey.world.feature;

import com.mojang.serialization.Codec;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class PrehistoricVinesFeature extends Feature<DefaultFeatureConfig> {
    public PrehistoricVinesFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos pos = context.getOrigin();

        if (!world.isAir(pos)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }

            BlockPos support = pos.offset(direction);
            if (VineBlock.shouldConnectTo(world, support, direction)) {
                world.setBlockState(
                        pos,
                        GallifreyModBlocks.PREHISTORIC_VINE.getDefaultState()
                                .with(VineBlock.getFacingProperty(direction), true),
                        2
                );
                return true;
            }
        }

        return false;
    }
}
