package com.cerulixteam.gallifrey.world.biome;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.world.ModPlacedFeatures;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;

import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.function.Consumer;

public class ModBiomes {

    //declare all keys here
    public static final RegistryKey<Biome> GALLIFREYAN_PLAINS = registerKey("gallifreyan_plains");
    public static final RegistryKey<Biome> GALLIFREYAN_BIRCH_FOREST = registerKey("gallifreyan_birch_forest");
    public static final RegistryKey<Biome> GALLIFREYAN_TAIGA = registerKey("gallifreyan_taiga");
    public static final RegistryKey<Biome> GALLIFREYAN_ULANDA_FOREST = registerKey("gallifreyan_ulanda_forest");
    public static final RegistryKey<Biome> GALLIFREYAN_TARDIS_BIOME = registerKey("gallifreyan_tardis_biome");
    public static final RegistryKey<Biome> GALLIFREYAN_WASTELAND = registerKey("gallifreyan_wasteland");
    public static final RegistryKey<Biome> TREEBORG_FOREST = registerKey("treeborg_forest");
    public static final RegistryKey<Biome> GALLIFREYAN_TREEBORG_FOREST = registerKey("gallifreyan_treeborg_forest");
    public static final RegistryKey<Biome> GALLIFREYAN_ASH_FOREST = registerKey("gallifreyan_ash_forest");
    public static final RegistryKey<Biome> GALLIFREYAN_MAPLE_JUNGLE = registerKey("gallifreyan_maple_jungle");
    public static final RegistryKey<Biome> GALLIFREYAN_MOONPINE_FOREST = registerKey("gallifreyan_moonpine_forest");

    private static RegistryKey<Biome> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, new Identifier(GallifreyMod.MOD_ID, name));
    }

    public static void bootstrap(Registerable<Biome> context) {
        var placedFeatures = context.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        var carvers = context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);

        //Register all biomes easily
        context.register(GALLIFREYAN_PLAINS, createBiome(true, false, 0.8F, 0.4F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    DefaultBiomeFeatures.addPlainsTallGrass(generationSettings);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        context.register(GALLIFREYAN_BIRCH_FOREST, createBiome(true, false, 0.6F, 0.6F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    DefaultBiomeFeatures.addBirchTrees(generationSettings);
                    DefaultBiomeFeatures.addLargeFerns(generationSettings);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        context.register(GALLIFREYAN_TAIGA, createBiome(true, false, 0.25F, 0.8F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    DefaultBiomeFeatures.addTaigaTrees(generationSettings);
                    DefaultBiomeFeatures.addSweetBerryBushes(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        // New: Ulanda Forest - dense Ulanda tree cover via the ULANDA_PLACED_KEY placed
        // feature (registered in ModPlacedFeatures), not a DefaultBiomeFeatures call,
        // since this is our own custom tree, not a vanilla default
        context.register(GALLIFREYAN_ULANDA_FOREST, createBiome(true, false, 0.5F, 0.6F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.ULANDA_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        // New: Tardis biome - placed at an extreme continentalness value in the
        // dimension's biome source (see gallifrey.json), matching real mushroom
        // fields rarity rather than approximating it with a placement chance
        context.register(GALLIFREYAN_TARDIS_BIOME, createBiome(true, false, 0.5F, 0.5F, 10638337, 15105551, 15215, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.TARDIS_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        // New: Ocean - claims continentalness [-1.05, -0.19] (combining vanilla's deep
        // ocean + ocean bands into one), which is what actually contains the Tardis
        // biome to its own extreme sliver instead of winning by default with nothing
        // else covering negative continentalness
        context.register(GALLIFREYAN_WASTELAND, createBiome(true, false, 0.5F, 0.5F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    DefaultBiomeFeatures.addSeagrassOnStone(generationSettings);
                    DefaultBiomeFeatures.addKelp(generationSettings);
                }));

        context.register(GALLIFREYAN_TREEBORG_FOREST, createBiome(true, false, 0.5F, 0.6F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.TREEBORG_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        // Ash Forest - Gallifrey's ash trees, a bit sparser than the Treeborg forest
        context.register(GALLIFREYAN_ASH_FOREST, createBiome(true, false, 0.5F, 0.6F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.ASH_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));

        // Maple Jungle - Gallifrey's take on the vanilla jungle: dense giant maples, maple
        // bushes, hanging vines, jungle grass and melons. Warm and wet.
        context.register(GALLIFREYAN_MAPLE_JUNGLE, createBiome(true, false, 0.95F, 0.9F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.MAPLE_JUNGLE_TREES_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addJungleGrass(generationSettings);
                    DefaultBiomeFeatures.addMelons(generationSettings);
                    DefaultBiomeFeatures.addVines(generationSettings);
                }));

        // Old Growth Moonpine Forest - like vanilla's old growth pine taiga (giant 2x2 trees,
        // mossy boulders, large ferns, berry bushes) but without the podzol: the ground stays
        // normal grass because the surface rules only put podzol in vanilla's own biomes.
        context.register(GALLIFREYAN_MOONPINE_FOREST, createBiome(true, false, 0.3F, 0.8F, 10638337, 15105551, 14641191, 15109680,
                placedFeatures, carvers, generationSettings -> {
                    DefaultBiomeFeatures.addMossyRocks(generationSettings);
                    DefaultBiomeFeatures.addLargeFerns(generationSettings);
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.MOONPINE_FOREST_TREES_PLACED_KEY);
                    DefaultBiomeFeatures.addTaigaGrass(generationSettings);
                    DefaultBiomeFeatures.addSweetBerryBushes(generationSettings);
                }));

        // Overworld biome (swapped in for vanilla forest by Biolith). It must NOT add the
        // Gallifrey ores itself: GallifreyMod already adds the sonic crystal ore to every
        // overworld biome, and listing it twice crashes with "Feature order cycle found".
        context.register(TREEBORG_FOREST, createBiome(false, false, 0.5F, 0.6F, 12638463, 7972607, 5274162, 5877296,
                placedFeatures, carvers, generationSettings -> {
                    generationSettings.feature(GenerationStep.Feature.VEGETAL_DECORATION, ModPlacedFeatures.TREEBORG_PLACED_KEY);
                    DefaultBiomeFeatures.addDefaultFlowers(generationSettings);
                    DefaultBiomeFeatures.addDefaultGrass(generationSettings);
                }));
    }

    // Helper method to eliminate copy-pasted boilerplate. featureCustomizer supplies
    // whatever's unique to that biome (its trees, its extra plants); the shared
    // defaults (carvers, ores, mushrooms) are added here once for all 4 biomes.
    // gallifreyOres: true for biomes in the Gallifrey dimension, false for overworld biomes.
    private static Biome createBiome(boolean gallifreyOres, boolean precipitation, float temp, float downfall, int fog, int sky, int grass, int foliage,
                                     RegistryEntryLookup<PlacedFeature> placedFeatures, RegistryEntryLookup<ConfiguredCarver<?>> carvers,
                                     Consumer<GenerationSettings.LookupBackedBuilder> featureCustomizer) {
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(placedFeatures, carvers);
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addDefaultOres(generationSettings);
        if (gallifreyOres) {
            generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.SONIC_CRYSTAL_ORE_PLACED_KEY);
            generationSettings.feature(GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.WHITE_POINT_ORE_PLACED_KEY);
        }
        featureCustomizer.accept(generationSettings);
        DefaultBiomeFeatures.addDefaultMushrooms(generationSettings);

        return new Biome.Builder()
                .precipitation(precipitation)
                .temperature(temp)
                .downfall(downfall)
                .effects(new BiomeEffects.Builder()
                        .fogColor(fog)
                        .skyColor(sky)
                        .waterColor(4159204)
                        .waterFogColor(2635560)
                        .grassColor(grass)
                        .foliageColor(foliage)
                        .moodSound(BiomeMoodSound.CAVE)
                        .build())
                .spawnSettings(spawnSettings.build())
                .generationSettings(generationSettings.build())
                .build();
    }
}