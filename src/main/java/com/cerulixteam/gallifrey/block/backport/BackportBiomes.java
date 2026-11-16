package com.cerulixteam.gallifrey.block.backport;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

/**
 * The backported biomes, placed in the Overworld with Biolith:
 * Pale Garden (1.21.4) in part of the Dark Forest, Dappled Forest (26.x, poplars) in part of the Forest,
 * and Sulfur Caves (26.x) in part of the Dripstone Caves. Their data is in data/gallifrey/worldgen/biome.
 */
public final class BackportBiomes {
    private BackportBiomes() {}

    public static final RegistryKey<Biome> PALE_GARDEN = key("pale_garden");
    public static final RegistryKey<Biome> DAPPLED_FOREST = key("dappled_forest");
    public static final RegistryKey<Biome> SULFUR_CAVES = key("sulfur_caves");

    public static final TreeDecoratorType<CreakingHeartTreeDecorator> CREAKING_HEART_DECORATOR = Registry.register(Registries.TREE_DECORATOR_TYPE,
            new Identifier("gallifrey", "creaking_heart"), new TreeDecoratorType<>(CreakingHeartTreeDecorator.CODEC));
    public static final Feature<DefaultFeatureConfig> SULFUR_SPIKE = Registry.register(Registries.FEATURE,
            new Identifier("gallifrey", "sulfur_spike"), new SulfurSpikeFeature());

    private static RegistryKey<Biome> key(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, new Identifier("gallifrey", name));
    }

    public static void register() {
        BiomePlacement.replaceOverworld(BiomeKeys.DARK_FOREST, PALE_GARDEN, 0.35);
        BiomePlacement.replaceOverworld(BiomeKeys.FOREST, DAPPLED_FOREST, 0.2);
        BiomePlacement.replaceOverworld(BiomeKeys.DRIPSTONE_CAVES, SULFUR_CAVES, 0.4);
    }
}
