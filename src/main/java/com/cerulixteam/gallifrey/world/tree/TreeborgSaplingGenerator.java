package com.cerulixteam.gallifrey.world.tree;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;


public class TreeborgSaplingGenerator extends SaplingGenerator {

    private static final RegistryKey<ConfiguredFeature<?, ?>> TREEBORG_TREE =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", "treeborg"));

    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return TREEBORG_TREE;
    }
}
