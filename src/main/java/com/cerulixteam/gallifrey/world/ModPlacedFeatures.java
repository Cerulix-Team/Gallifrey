package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

import java.util.List;

public class ModPlacedFeatures {
    public static final RegistryKey<PlacedFeature> SONIC_CRYSTAL_ORE_PLACED_KEY = registerKey("sonic_crystal_ore_placed");
    public static final RegistryKey<PlacedFeature> RUBY_ORE_PLACED_KEY = registerKey("ruby_ore");
    public static final RegistryKey<PlacedFeature> SAPPHIRE_ORE_PLACED_KEY = registerKey("sapphire_ore");
    public static final RegistryKey<PlacedFeature> NETHER_SONIC_CRYSTAL_ORE_PLACED_KEY = registerKey("nether_sonic_crystal_ore_placed");
    public static final RegistryKey<PlacedFeature> WHITE_POINT_ORE_PLACED_KEY = registerKey("white_point_ore_placed");
    public static final RegistryKey<PlacedFeature> NETHER_WHITE_POINT_ORE_PLACED_KEY = registerKey("nether_white_point_ore_placed");
    public static final RegistryKey<PlacedFeature> MARS_IRON_ORE_PLACED_KEY = registerKey("mars_iron_ore_placed");
    public static final RegistryKey<PlacedFeature> MARS_PISS_CRYSTAL_PLACED_KEY = registerKey("mars_piss_crystal_placed");


    public static final RegistryKey<PlacedFeature> ULANDA_PLACED_KEY = registerKey("ulanda_placed");
    public static final RegistryKey<PlacedFeature> TARDIS_PLACED_KEY = registerKey("tardis_placed");
    public static final RegistryKey<PlacedFeature> TREEBORG_PLACED_KEY = registerKey("treeborg_placed");
    public static final RegistryKey<PlacedFeature> ASH_PLACED_KEY = registerKey("ash_placed");
    public static final RegistryKey<PlacedFeature> MAPLE_PLACED_KEY = registerKey("maple_placed");
    public static final RegistryKey<PlacedFeature> MOONPINE_PLACED_KEY = registerKey("moonpine_placed");
    public static final RegistryKey<PlacedFeature> MAPLE_JUNGLE_TREES_PLACED_KEY = registerKey("maple_jungle_trees_placed");
    public static final RegistryKey<PlacedFeature> MOONPINE_FOREST_TREES_PLACED_KEY = registerKey("moonpine_forest_trees_placed");
    public static final RegistryKey<PlacedFeature> PREHISTORIC_PLACED_KEY = registerKey("prehistoric_placed");
    public static final RegistryKey<PlacedFeature> PREHISTORIC_ORE_PLACED_KEY = registerKey("prehistoric_ore_placed");

    public static final RegistryKey<PlacedFeature> ATRIUM_ORE_UPPER_PLACED_KEY = registerKey("atrium_ore_upper");
    public static final RegistryKey<PlacedFeature> ATRIUM_ORE_MIDDLE_PLACED_KEY = registerKey("atrium_ore_middle");
    public static final RegistryKey<PlacedFeature> ATRIUM_ORE_SMALL_PLACED_KEY = registerKey("atrium_ore_small");
    public static final RegistryKey<PlacedFeature> WASTED_OAK_PLACED_KEY = registerKey("wasted_oak_placed");
    public static final RegistryKey<PlacedFeature> WASTED_BIRCH_PLACED_KEY = registerKey("wasted_birch_placed");

    public static void boostrap(Registerable<PlacedFeature> context) {
        var configuredFeatureRegistryEntryLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        register(context, ULANDA_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.ULANDA_KEY),
                List.of(
                        CountPlacementModifier.of(10),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.ULANDA_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, TARDIS_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.TARDIS_TREE_KEY),
                List.of(
                        CountPlacementModifier.of(2),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.TARDIS_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, TREEBORG_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.TREEBORG_KEY),
                List.of(
                        CountPlacementModifier.of(10),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.TREEBORG_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, ASH_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.ASH_KEY),
                List.of(
                        CountPlacementModifier.of(4),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.ASH_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, MAPLE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MAPLE_KEY),
                List.of(
                        CountPlacementModifier.of(6),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.MAPLE_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, MOONPINE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MOONPINE_KEY),
                List.of(
                        CountPlacementModifier.of(2),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.MOONPINE_SAPLING),
                        BiomePlacementModifier.of()
                ));

        // Maple Jungle: thick tree cover. Vanilla's jungle uses 50 per chunk; maples are
        // wider than jungle trees, so 35 gives the same packed-in feel without being a wall.
        // treeModifiers() also keeps trees off water (surface water depth filter).
        register(context, MAPLE_JUNGLE_TREES_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MAPLE_JUNGLE_TREES_KEY),
                VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(35, 0.1F, 1)));

        // Old Growth Moonpine Forest: same density as vanilla's old growth pine taiga.
        register(context, MOONPINE_FOREST_TREES_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MOONPINE_FOREST_TREES_KEY),
                VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(10, 0.1F, 1)));

        register(context, SONIC_CRYSTAL_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.SONIC_CRYSTAL_ORE_KEY),
                 ModOrePlacement.modifiersWithCount(4, // Gold-like rarity
                    HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(32))));
        register(context, NETHER_SONIC_CRYSTAL_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.NETHER_SONIC_CRYSTAL_ORE_KEY),
                ModOrePlacement.modifiersWithCount(4, // Gold-like rarity
                    HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(96))));

        register(context, WHITE_POINT_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.WHITE_POINT_ORE_KEY),
                ModOrePlacement.modifiersWithCount(1, // Netherite-like rarity
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(16))));
        register(context, NETHER_WHITE_POINT_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.NETHER_WHITE_POINT_ORE_KEY),
                ModOrePlacement.modifiersWithCount(1, // Netherite-like rarity
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(32))));


        register(context, MARS_IRON_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MARS_IRON_ORE_KEY),
        ModOrePlacement.modifiersWithCount(8,
        HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(80))));

        register(context, MARS_PISS_CRYSTAL_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.MARS_PISS_CRYSTAL_KEY),
        ModOrePlacement.modifiersWithCount(5,
        HeightRangePlacementModifier.uniform(YOffset.fixed(-48), YOffset.fixed(48))));

        // Prehistoric trees: as dense as vanilla's jungle (50 per chunk, same as trees_jungle).
        // treeModifiers() also keeps them off water; each tree checks its sapling could survive.
        register(context, PREHISTORIC_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.PREHISTORIC_JUNGLE_TREES_KEY),
                VegetationPlacedFeatures.treeModifiers(PlacedFeatures.createCountExtraModifier(50, 0.1F, 1)));
        register(context, PREHISTORIC_ORE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.PREHISTORIC_ORE_KEY),
                ModOrePlacement.modifiersWithCount(10,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-48), YOffset.fixed(96))));

        // Atrium mirrors Minecraft 1.20.1 iron's three Overworld placements:
        // upper: 90 veins, middle: 10 veins, small: 10 veins.
        register(context, ATRIUM_ORE_UPPER_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.ATRIUM_ORE_KEY),
                List.of(
                        CountPlacementModifier.of(90),
                        SquarePlacementModifier.of(),
                        HeightRangePlacementModifier.trapezoid(YOffset.fixed(80), YOffset.fixed(384)),
                        BiomePlacementModifier.of()
                ));
        register(context, ATRIUM_ORE_MIDDLE_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.ATRIUM_ORE_KEY),
                ModOrePlacement.modifiersWithCount(10,
                        HeightRangePlacementModifier.uniform(YOffset.fixed(-24), YOffset.fixed(56))));
        register(context, ATRIUM_ORE_SMALL_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.ATRIUM_ORE_SMALL_KEY),
                ModOrePlacement.modifiersWithCount(10,
                        HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(72))));

        register(context, WASTED_OAK_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.WASTED_OAK_TREE_KEY),
                List.of(
                        CountPlacementModifier.of(3),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.WASTED_SAPLING),
                        BiomePlacementModifier.of()
                ));

        register(context, WASTED_BIRCH_PLACED_KEY, configuredFeatureRegistryEntryLookup.getOrThrow(ModConfiguredFeatures.WASTED_BIRCH_TREE_KEY),
                List.of(
                        CountPlacementModifier.of(2),
                        SquarePlacementModifier.of(),
                        HeightmapPlacementModifier.of(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES),
                        PlacedFeatures.wouldSurvive(GallifreyModBlocks.WASTED_SAPLING),
                        BiomePlacementModifier.of()
                ));
    }

    public static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(GallifreyMod.MOD_ID, name));
    }

    private static void register(Registerable<PlacedFeature> context, RegistryKey<PlacedFeature> key, RegistryEntry<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}