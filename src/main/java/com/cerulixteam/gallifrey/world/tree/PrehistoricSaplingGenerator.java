package com.cerulixteam.gallifrey.world.tree;

import com.cerulixteam.gallifrey.world.ModConfiguredFeatures;
import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Works like a jungle sapling: one sapling grows a normal prehistoric tree,
 * four in a 2x2 square grow a giant one.
 */
public class PrehistoricSaplingGenerator extends LargeTreeSaplingGenerator {
    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return ModConfiguredFeatures.PREHISTORIC_TREE_KEY;
    }

    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random) {
        return ModConfiguredFeatures.PREHISTORIC_GIANT_TREE_KEY;
    }
}
