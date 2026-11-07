package com.cerulixteam.gallifrey.block.backport;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;

import static com.cerulixteam.gallifrey.block.backport.BackportBlocks.*;

/**
 * Sets up the backported blocks (see {@link BackportBlocks}). Call {@link #register()} from
 * GallifreyMod.onInitialize, after GallifreyModBlocks.register().
 */
public final class Backport {
    private Backport() {}

    public static void register() {
        BackportBlocks.register();
        BackportItems.registerBoat();

        StrippableBlockRegistry.register(PALE_OAK_LOG, STRIPPED_PALE_OAK_LOG);
        StrippableBlockRegistry.register(PALE_OAK_WOOD, STRIPPED_PALE_OAK_WOOD);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        for (Block block : new Block[]{PALE_OAK_LOG, PALE_OAK_WOOD, STRIPPED_PALE_OAK_LOG, STRIPPED_PALE_OAK_WOOD}) flammable.add(block, 5, 5);
        for (Block block : new Block[]{PALE_OAK_PLANKS, PALE_OAK_STAIRS, PALE_OAK_SLAB, PALE_OAK_FENCE, PALE_OAK_FENCE_GATE}) flammable.add(block, 5, 20);
        flammable.add(PALE_OAK_LEAVES, 30, 60);
        for (Block block : new Block[]{PALE_MOSS_BLOCK, PALE_MOSS_CARPET, PALE_HANGING_MOSS, LEAF_LITTER, WILDFLOWERS, BUSH, FIREFLY_BUSH,
                SHORT_DRY_GRASS, TALL_DRY_GRASS, OPEN_EYEBLOSSOM, CLOSED_EYEBLOSSOM, CACTUS_FLOWER}) flammable.add(block, 60, 100);

        CompostingChanceRegistry compost = CompostingChanceRegistry.INSTANCE;
        compost.add(PALE_OAK_LEAVES, 0.3F);
        compost.add(PALE_OAK_SAPLING, 0.3F);
        compost.add(LEAF_LITTER, 0.3F);
        compost.add(SHORT_DRY_GRASS, 0.3F);
        compost.add(TALL_DRY_GRASS, 0.3F);
        compost.add(PALE_HANGING_MOSS, 0.3F);
        compost.add(PALE_MOSS_CARPET, 0.3F);
        compost.add(BUSH, 0.3F);
        compost.add(CACTUS_FLOWER, 0.3F);
        compost.add(WILDFLOWERS, 0.3F);
        compost.add(FIREFLY_BUSH, 0.3F);
        compost.add(OPEN_EYEBLOSSOM, 0.65F);
        compost.add(CLOSED_EYEBLOSSOM, 0.65F);
        compost.add(PALE_MOSS_BLOCK, 0.65F);

        // Tags (mining tools, walls, logs and so on) are in this always-on built-in data pack, so they
        // merge with the datagen tag files instead of clashing with them.
        FabricLoader.getInstance().getModContainer("gallifrey").ifPresent(mod ->
                ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("gallifrey", "backported"), mod,
                        Text.translatable("pack.gallifrey.backported"), ResourcePackActivationType.ALWAYS_ENABLED));

        // Where the new plants and trees grow, so they can be found in survival.
        add("backport_pale_oak", BiomeKeys.DARK_FOREST);
        add("backport_pale_moss_patch", BiomeKeys.DARK_FOREST);
        add("backport_eyeblossom_patch", BiomeKeys.DARK_FOREST);
        add("backport_leaf_litter", BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.DARK_FOREST, BiomeKeys.WINDSWEPT_FOREST);
        add("backport_wildflowers", BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.MEADOW);
        add("backport_bush", BiomeKeys.PLAINS, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.WINDSWEPT_HILLS);
        add("backport_firefly_bush", BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP);
        add("backport_dry_grass", BiomeKeys.DESERT, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS, BiomeKeys.ERODED_BADLANDS);
        add("backport_cactus_flower", BiomeKeys.DESERT, BiomeKeys.BADLANDS);
    }

    @SafeVarargs
    private static void add(String feature, RegistryKey<net.minecraft.world.biome.Biome>... biomes) {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(biomes), GenerationStep.Feature.VEGETAL_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("gallifrey", feature)));
    }

    /** Everything in the Backported section of the Gallifrey tab, in order. */
    public static List<ItemConvertible> tabItems() {
        List<ItemConvertible> items = new ArrayList<>(TAB);
        // Put the extra items next to their blocks.
        insertAfter(items, COPPER_LANTERN[7].asItem(), BackportItems.COPPER_TORCH, BackportItems.COPPER_NUGGET);
        insertAfter(items, PALE_OAK_BUTTON.asItem(), BackportItems.PALE_OAK_SIGN, BackportItems.PALE_OAK_HANGING_SIGN,
                BackportItems.PALE_OAK_BOAT, BackportItems.PALE_OAK_CHEST_BOAT);
        insertAfter(items, RESIN_BLOCK.asItem(), BackportItems.RESIN_BRICK);
        return items;
    }

    public static ItemStack icon() {
        return new ItemStack(TUFF_BRICKS);
    }

    private static void insertAfter(List<ItemConvertible> items, ItemConvertible after, ItemConvertible... add) {
        int at = items.indexOf(after);
        if (at < 0) at = items.size() - 1;
        items.addAll(at + 1, List.of(add));
    }
}
