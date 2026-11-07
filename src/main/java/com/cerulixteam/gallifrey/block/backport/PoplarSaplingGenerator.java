package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;

/** A Poplar Sapling grows into a red, orange or yellow poplar, at random. */
public class PoplarSaplingGenerator extends SaplingGenerator {
    private static final RegistryKey<ConfiguredFeature<?, ?>>[] TREES = keys("red_poplar", "orange_poplar", "yellow_poplar");

    @SuppressWarnings("unchecked")
    private static RegistryKey<ConfiguredFeature<?, ?>>[] keys(String... names) {
        RegistryKey<ConfiguredFeature<?, ?>>[] out = new RegistryKey[names.length];
        for (int i = 0; i < names.length; i++) out[i] = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", names[i]));
        return out;
    }

    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return TREES[random.nextInt(TREES.length)];
    }
}
