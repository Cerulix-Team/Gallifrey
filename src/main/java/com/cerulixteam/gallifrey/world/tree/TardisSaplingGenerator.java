package com.cerulixteam.gallifrey.world.tree;

import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;


public class TardisSaplingGenerator extends SaplingGenerator {

    private static final RegistryKey<ConfiguredFeature<?, ?>> TARDIS_TREE =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", "tardis_tree"));

    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return TARDIS_TREE;
    }
}
