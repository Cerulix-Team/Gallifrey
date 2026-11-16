package com.cerulixteam.gallifrey.world.tree;

import com.cerulixteam.gallifrey.world.ModConfiguredFeatures;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class MoonpineSaplingGenerator extends LargeTreeSaplingGenerator {

    // A single sapling never grows: Moonpine only grows from a 2x2 of saplings.
    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return null;
    }

    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random) {
        return ModConfiguredFeatures.MOONPINE_KEY;
    }
}
