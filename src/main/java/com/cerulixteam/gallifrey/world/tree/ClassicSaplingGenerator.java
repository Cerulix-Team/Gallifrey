package com.cerulixteam.gallifrey.world.tree;

import com.cerulixteam.gallifrey.world.ModConfiguredFeatures;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class ClassicSaplingGenerator extends SaplingGenerator {
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return ModConfiguredFeatures.CLASSIC_TREE_KEY;
    }
}
