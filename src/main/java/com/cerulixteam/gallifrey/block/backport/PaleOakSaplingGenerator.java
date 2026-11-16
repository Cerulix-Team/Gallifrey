package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.sapling.LargeTreeSaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/** Pale Oak saplings only grow as a 2x2, like Dark Oak. The tree is data/gallifrey/worldgen/configured_feature/pale_oak.json. */
public class PaleOakSaplingGenerator extends LargeTreeSaplingGenerator {
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_OAK =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", "pale_oak"));

    @Nullable
    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
        return null;
    }

    @Override
    protected RegistryKey<ConfiguredFeature<?, ?>> getLargeTreeFeature(Random random) {
        return PALE_OAK;
    }
}
