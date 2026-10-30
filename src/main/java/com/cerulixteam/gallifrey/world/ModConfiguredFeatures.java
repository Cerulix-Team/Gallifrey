package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.CherryFoliagePlacer;
import net.minecraft.world.gen.foliage.MegaPineFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.CherryTrunkPlacer;
import net.minecraft.world.gen.trunk.GiantTrunkPlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.minecraft.world.gen.trunk.MegaJungleTrunkPlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.foliage.BushFoliagePlacer;
import net.minecraft.world.gen.foliage.SpruceFoliagePlacer;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.LeavesVineTreeDecorator;
import com.cerulixteam.gallifrey.world.tree.decorator.PrehistoricLeavesVineTreeDecorator;
import com.cerulixteam.gallifrey.world.tree.decorator.PrehistoricTrunkVineTreeDecorator;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.block.Block;

import java.util.List;

public class ModConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> SONIC_CRYSTAL_ORE_KEY = registerKey("sonic_crystal_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> NETHER_SONIC_CRYSTAL_ORE_KEY = registerKey("nether_sonic_crystal_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WHITE_POINT_ORE_KEY = registerKey("white_point_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> NETHER_WHITE_POINT_ORE_KEY = registerKey("nether_white_point_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MARS_IRON_ORE_KEY = registerKey("mars_iron_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MARS_PISS_CRYSTAL_KEY = registerKey("mars_piss_crystal");


    public static final RegistryKey<ConfiguredFeature<?, ?>> ULANDA_KEY =registerKey("ulanda");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TARDIS_TREE_KEY =registerKey("tardis_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREEBORG_KEY =registerKey("treeborg");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ASH_KEY =registerKey("ash");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MAPLE_KEY =registerKey("maple");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOONPINE_KEY =registerKey("moonpine");
    // Maple Jungle: giant 2x2 maples, normal maples and low maple bushes, picked at random
    public static final RegistryKey<ConfiguredFeature<?, ?>> MAPLE_GIANT_KEY = registerKey("maple_giant");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MAPLE_BUSH_KEY = registerKey("maple_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MAPLE_JUNGLE_TREES_KEY = registerKey("maple_jungle_trees");
    // Old Growth Moonpine Forest: the giant 2x2 moonpine mixed with a smaller single-trunk moonpine
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOONPINE_SMALL_KEY = registerKey("moonpine_small");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MOONPINE_FOREST_TREES_KEY = registerKey("moonpine_forest_trees");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PREHISTORIC_TREE_KEY = registerKey("prehistoric_tree");
    // Prehistoric jungle: giant 2x2 prehistoric trees, normal ones and low bushes, picked at random like vanilla's jungle
    public static final RegistryKey<ConfiguredFeature<?, ?>> PREHISTORIC_GIANT_TREE_KEY = registerKey("prehistoric_giant_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PREHISTORIC_BUSH_KEY = registerKey("prehistoric_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PREHISTORIC_JUNGLE_TREES_KEY = registerKey("prehistoric_jungle_trees");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WASTED_TREE_KEY = registerKey("wasted_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WASTED_OAK_TREE_KEY = registerKey("wasted_oak_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WASTED_BIRCH_TREE_KEY = registerKey("wasted_birch_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CLASSIC_TREE_KEY = registerKey("classic_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PREHISTORIC_ORE_KEY = registerKey("prehistoric_ore");

    public static final RegistryKey<ConfiguredFeature<?, ?>> ATRIUM_ORE_KEY = registerKey("atrium_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ATRIUM_ORE_SMALL_KEY = registerKey("atrium_ore_small");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplacables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplacables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // --- Atrium ---
        List<OreFeatureConfig.Target> atriumTargets = List.of(
                OreFeatureConfig.createTarget(stoneReplacables, GallifreyModBlocks.ATRIUM_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplacables, GallifreyModBlocks.DEEPSLATE_ATRIUM_ORE.getDefaultState())
        );
        register(context, ATRIUM_ORE_KEY, Feature.ORE, new OreFeatureConfig(atriumTargets, 9));
        register(context, ATRIUM_ORE_SMALL_KEY, Feature.ORE, new OreFeatureConfig(atriumTargets, 4));
        RuleTest netherReplacables = new TagMatchRuleTest(BlockTags.BASE_STONE_NETHER);
        RuleTest marsReplacables = new TagMatchRuleTest(net.minecraft.registry.tag.TagKey.of(RegistryKeys.BLOCK, GallifreyMod.id("mars_ore_replaceables")));

        // --- Sonic Crystal ---
        List<OreFeatureConfig.Target> overworldSonicCrystalOres = List.of(
                OreFeatureConfig.createTarget(stoneReplacables, GallifreyModBlocks.SONIC_CRYSTAL_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplacables, GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE.getDefaultState()));

        List<OreFeatureConfig.Target> netherSonicCrystalOres = List.of(
                OreFeatureConfig.createTarget(netherReplacables, GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE.getDefaultState()));

        register(context, SONIC_CRYSTAL_ORE_KEY, Feature.ORE, new OreFeatureConfig(overworldSonicCrystalOres, 9));
        register(context, NETHER_SONIC_CRYSTAL_ORE_KEY, Feature.ORE, new OreFeatureConfig(netherSonicCrystalOres, 9));

        // --- White Point ---
        List<OreFeatureConfig.Target> overworldWhitePointOres = List.of(
                OreFeatureConfig.createTarget(stoneReplacables, GallifreyModBlocks.WHITE_POINT_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplacables, GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE.getDefaultState()));

        List<OreFeatureConfig.Target> netherWhitePointOres = List.of(
                OreFeatureConfig.createTarget(netherReplacables, GallifreyModBlocks.NETHER_WHITE_POINT_ORE.getDefaultState()));

        register(context, WHITE_POINT_ORE_KEY, Feature.ORE, new OreFeatureConfig(overworldWhitePointOres, 3));
        register(context, NETHER_WHITE_POINT_ORE_KEY, Feature.ORE, new OreFeatureConfig(netherWhitePointOres, 3));

        // --- Mars Iron ---
        List<OreFeatureConfig.Target> marsIronOres = List.of(
                OreFeatureConfig.createTarget(marsReplacables, GallifreyModBlocks.MARS_IRON_ORE.getDefaultState()));

        register(context, MARS_IRON_ORE_KEY, Feature.ORE, new OreFeatureConfig(marsIronOres, 8));

        List<OreFeatureConfig.Target> marsPissCrystalTargets = List.of(
                OreFeatureConfig.createTarget(marsReplacables, GallifreyModBlocks.PISS_CRYSTAL.getDefaultState()));
        register(context, MARS_PISS_CRYSTAL_KEY, Feature.ORE, new OreFeatureConfig(marsPissCrystalTargets, 4));

        register(context,ULANDA_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(GallifreyModBlocks.ULANDA_LOG),
                        new CherryTrunkPlacer(
                                7,1,2,
                                UniformIntProvider.create(1,3),
                                UniformIntProvider.create(4,6),
                                UniformIntProvider.create(-4,-2),
                                UniformIntProvider.create(-1,1)
                        ),
                        BlockStateProvider.of(GallifreyModBlocks.ULANDA_LEAVES),
                new CherryFoliagePlacer(
                        ConstantIntProvider.create(4),
                        ConstantIntProvider.create(1),
                        UniformIntProvider.create(4, 5),
                        0.25F,
                        0.5F,
                        0.16666667F,
                        0.33333334F
                ),

                        new TwoLayersFeatureSize(1, 0, 1)
                )
                        .build()
        );

        register(context, TARDIS_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.TARDIS_LOG),
                new StraightTrunkPlacer(5, 4, 3),

                BlockStateProvider.of(GallifreyModBlocks.TARDIS_LEAVES),
                        new CherryFoliagePlacer(
                                ConstantIntProvider.create(4),
                                ConstantIntProvider.create(1),
                                UniformIntProvider.create(4, 5),
                                0.25F,
                                0.5F,
                                0.16666667F,
                                0.33333334F
                        ),

                        new TwoLayersFeatureSize(1, 0, 1)
                )
                        .build()
        );

        register(context, TREEBORG_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.TREEBORG_LOG),
                new StraightTrunkPlacer(5, 4, 3),

                BlockStateProvider.of(GallifreyModBlocks.TREEBORG_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(2), 4),

                new TwoLayersFeatureSize(1, 0, 3)).build());

        register(context, ASH_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(GallifreyModBlocks.ASH_LOG),
                        new StraightTrunkPlacer(5, 4, 3),

                        BlockStateProvider.of(GallifreyModBlocks.ASH_LEAVES),
                new CherryFoliagePlacer(
                        ConstantIntProvider.create(3),
                        ConstantIntProvider.create(1),
                        UniformIntProvider.create(4, 5),
                        0.25F,
                        0.5F,
                        0.16666667F,
                        0.33333334F
                ),

                        new TwoLayersFeatureSize(1, 0, 1)
                )
                        .build()
        );
        register(context, MAPLE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(GallifreyModBlocks.MAPLE_LOG),
                    new StraightTrunkPlacer(5, 3, 0),

                        BlockStateProvider.of(GallifreyModBlocks.MAPLE_LEAVES),
                        new CherryFoliagePlacer(
                                ConstantIntProvider.create(3),
                                ConstantIntProvider.create(1),
                                UniformIntProvider.create(4, 5),
                                0.25F,
                                0.5F,
                                0.16666667F,
                                0.33333334F
                        ),

                        new TwoLayersFeatureSize(1, 0, 1)
                )
                        .build()
        );

        register(context, MOONPINE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.MOONPINE_LOG),
                new GiantTrunkPlacer(13, 2, 14),
                BlockStateProvider.of(GallifreyModBlocks.MOONPINE_LEAVES),
                new MegaPineFoliagePlacer(
                        ConstantIntProvider.create(0),
                        ConstantIntProvider.create(0),
                        UniformIntProvider.create(13, 17)),
                new TwoLayersFeatureSize(1, 1, 2)).build());

        // ---------------- Maple Jungle ----------------
        // Giant maple: 2x2 trunk like vanilla's giant jungle tree, with vines on trunk and leaves.
        register(context, MAPLE_GIANT_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.MAPLE_LOG),
                new MegaJungleTrunkPlacer(10, 2, 19),
                BlockStateProvider.of(GallifreyModBlocks.MAPLE_LEAVES),
                new JungleFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 2),
                new TwoLayersFeatureSize(1, 1, 2))
                .decorators(List.of(TrunkVineTreeDecorator.INSTANCE, new LeavesVineTreeDecorator(0.25F)))
                .build());

        // Maple bush: one log with a mound of leaves, like vanilla's jungle bush.
        register(context, MAPLE_BUSH_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.MAPLE_LOG),
                new StraightTrunkPlacer(1, 0, 0),
                BlockStateProvider.of(GallifreyModBlocks.MAPLE_LEAVES),
                new BushFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(1), 2),
                new TwoLayersFeatureSize(0, 0, 0)).build());

        RegistryEntryLookup<ConfiguredFeature<?, ?>> configured = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        // Same odds as vanilla's jungle: 1 in 3 giant, otherwise half bushes, the rest normal maples.
        register(context, MAPLE_JUNGLE_TREES_KEY, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(
                new RandomFeatureEntry(checked(configured, MAPLE_GIANT_KEY, GallifreyModBlocks.MAPLE_SAPLING), 0.33333334F),
                new RandomFeatureEntry(checked(configured, MAPLE_BUSH_KEY, GallifreyModBlocks.MAPLE_SAPLING), 0.5F)),
                checked(configured, MAPLE_KEY, GallifreyModBlocks.MAPLE_SAPLING)));

        // ---------------- Old Growth Moonpine Forest ----------------
        // Smaller single-trunk moonpine, spruce-shaped, to fill between the giants.
        register(context, MOONPINE_SMALL_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.MOONPINE_LOG),
                new StraightTrunkPlacer(7, 4, 0),
                BlockStateProvider.of(GallifreyModBlocks.MOONPINE_LEAVES),
                new SpruceFoliagePlacer(UniformIntProvider.create(2, 3), UniformIntProvider.create(0, 2), UniformIntProvider.create(1, 2)),
                new TwoLayersFeatureSize(2, 0, 2)).ignoreVines().build());

        // Roughly half giants, half smaller moonpines, like vanilla's old growth pine taiga.
        register(context, MOONPINE_FOREST_TREES_KEY, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(
                new RandomFeatureEntry(checked(configured, MOONPINE_KEY, GallifreyModBlocks.MOONPINE_SAPLING), 0.5F)),
                checked(configured, MOONPINE_SMALL_KEY, GallifreyModBlocks.MOONPINE_SAPLING)));

        register(context, WASTED_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LOG),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).build());

        // Skaro forest variants: both use only the Wasted woodset, with silhouettes
        // inspired by vanilla oak and birch trees.
        register(context, WASTED_OAK_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LOG),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).build());

        register(context, WASTED_BIRCH_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LOG),
                new StraightTrunkPlacer(5, 2, 0),
                BlockStateProvider.of(GallifreyModBlocks.WASTED_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).build());

        register(context, PREHISTORIC_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LOG),
                new StraightTrunkPlacer(4, 8, 0),
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 3),
                new TwoLayersFeatureSize(1, 0, 1)
        ).build());

        // ---------------- Prehistoric jungle ----------------
        // The normal prehistoric tree above already has vanilla's jungle tree shape
        // (straight 4-12 trunk, blob leaves). These add the rest of a jungle:

        // Giant prehistoric tree: 2x2 trunk like vanilla's mega jungle tree, Prehistoric Vines on trunk and leaves.
        // Also what four prehistoric saplings in a square grow into.
        register(context, PREHISTORIC_GIANT_TREE_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LOG),
                new MegaJungleTrunkPlacer(10, 2, 19),
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LEAVES),
                new JungleFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(0), 2),
                new TwoLayersFeatureSize(1, 1, 2))
                .decorators(List.of(PrehistoricTrunkVineTreeDecorator.INSTANCE, new PrehistoricLeavesVineTreeDecorator(0.25F)))
                .build());

        // Prehistoric bush: one log with a mound of leaves, like vanilla's jungle bush.
        register(context, PREHISTORIC_BUSH_KEY, Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LOG),
                new StraightTrunkPlacer(1, 0, 0),
                BlockStateProvider.of(GallifreyModBlocks.PREHISTORIC_LEAVES),
                new BushFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(1), 2),
                new TwoLayersFeatureSize(0, 0, 0)).build());

        // Same odds as vanilla's jungle: 1 in 3 giant, otherwise half bushes, the rest normal prehistoric trees.
        RegistryEntryLookup<ConfiguredFeature<?, ?>> prehistoricConfigured = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        register(context, PREHISTORIC_JUNGLE_TREES_KEY, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(
                new RandomFeatureEntry(checked(prehistoricConfigured, PREHISTORIC_GIANT_TREE_KEY, GallifreyModBlocks.PREHISTORIC_SAPLING), 0.33333334F),
                new RandomFeatureEntry(checked(prehistoricConfigured, PREHISTORIC_BUSH_KEY, GallifreyModBlocks.PREHISTORIC_SAPLING), 0.5F)),
                checked(prehistoricConfigured, PREHISTORIC_TREE_KEY, GallifreyModBlocks.PREHISTORIC_SAPLING)));

        RuleTest prehistoricStoneReplacables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest prehistoricDeepslateReplacables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        register(context, PREHISTORIC_ORE_KEY, Feature.ORE, new OreFeatureConfig(List.of(
                OreFeatureConfig.createTarget(prehistoricStoneReplacables, GallifreyModBlocks.PREHISTORIC_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(prehistoricDeepslateReplacables, GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE.getDefaultState())
        ), 8));
    }
    /** A tree that only grows where its sapling could survive (so never on water or bare stone). */
    private static net.minecraft.registry.entry.RegistryEntry<PlacedFeature> checked(
            RegistryEntryLookup<ConfiguredFeature<?, ?>> configured, RegistryKey<ConfiguredFeature<?, ?>> tree, Block sapling) {
        return PlacedFeatures.createEntry(configured.getOrThrow(tree), PlacedFeatures.wouldSurvive(sapling));
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier(GallifreyMod.MOD_ID, name));
    }

    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(Registerable<ConfiguredFeature<?, ?>> context,
                                                                                   RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}