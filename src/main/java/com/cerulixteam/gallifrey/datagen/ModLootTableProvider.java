package com.cerulixteam.gallifrey.datagen;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {

        //TARDIS WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.TARDIS_SAPLING);
        addDrop(GallifreyModBlocks.TARDIS_LEAVES, leavesDrops(GallifreyModBlocks.TARDIS_LEAVES, GallifreyModBlocks.TARDIS_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.TARDIS_LOG);
        addDrop(GallifreyModBlocks.STRIP_TARDIS_LOG);
        addDrop(GallifreyModBlocks.TARDIS_WOOD);
        addDrop(GallifreyModBlocks.STRIP_TARDIS_WOOD);
        addDrop(GallifreyModBlocks.TARDIS_PLANKS);
        addDrop(GallifreyModBlocks.TARDIS_STAIRS);
        addDrop(GallifreyModBlocks.GALLIFREYAN_WORKBENCH);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_TARDIS_SAPLING);
        addDrop(GallifreyModBlocks.TARDIS_SLAB, slabDrops(GallifreyModBlocks.TARDIS_SLAB));
        addDrop(GallifreyModBlocks.TARDIS_BUTTON);
        addDrop(GallifreyModBlocks.TARDIS_FENCE);
        addDrop(GallifreyModBlocks.TARDIS_FENCE_GATE);
        addDrop(GallifreyModBlocks.TARDIS_WOOD_DOOR, doorDrops(GallifreyModBlocks.TARDIS_WOOD_DOOR));
        addDrop(GallifreyModBlocks.TARDIS_TRAPDOOR);
        addDrop(GallifreyModBlocks.TARDIS_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_TARDIS_SIGN);
        addDrop(GallifreyModBlocks.HANGING_TARDIS_SIGN);

        //ULANDA WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.ULANDA_SAPLING);
        addDrop(GallifreyModBlocks.ULANDA_LEAVES, leavesDrops(GallifreyModBlocks.ULANDA_LEAVES, GallifreyModBlocks.ULANDA_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.ULANDA_LOG);
        addDrop(GallifreyModBlocks.STRIP_ULANDA_LOG);
        addDrop(GallifreyModBlocks.ULANDA_WOOD);
        addDrop(GallifreyModBlocks.STRIP_ULANDA_WOOD);
        addDrop(GallifreyModBlocks.ULANDA_PLANKS);
        addDrop(GallifreyModBlocks.ULANDA_STAIRS);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_ULANDA_SAPLING);
        addDrop(GallifreyModBlocks.ULANDA_SLAB, slabDrops(GallifreyModBlocks.ULANDA_SLAB));
        addDrop(GallifreyModBlocks.ULANDA_BUTTON);
        addDrop(GallifreyModBlocks.ULANDA_FENCE);
        addDrop(GallifreyModBlocks.ULANDA_FENCE_GATE);
        addDrop(GallifreyModBlocks.ULANDA_DOOR, doorDrops(GallifreyModBlocks.ULANDA_DOOR));
        addDrop(GallifreyModBlocks.ULANDA_TRAPDOOR);
        addDrop(GallifreyModBlocks.ULANDA_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_ULANDA_SIGN);
        addDrop(GallifreyModBlocks.HANGING_ULANDA_SIGN);

        //TREE-BORG WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.TREEBORG_SAPLING);
        addDrop(GallifreyModBlocks.TREEBORG_LEAVES, leavesDrops(GallifreyModBlocks.TREEBORG_LEAVES, GallifreyModBlocks.TREEBORG_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.TREEBORG_LOG);
        addDrop(GallifreyModBlocks.STRIP_TREEBORG_LOG);
        addDrop(GallifreyModBlocks.TREEBORG_WOOD);
        addDrop(GallifreyModBlocks.STRIP_TREEBORG_WOOD);
        addDrop(GallifreyModBlocks.TREEBORG_PLANKS);
        addDrop(GallifreyModBlocks.TREEBORG_STAIRS);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_TREEBORG_SAPLING);
        addDrop(GallifreyModBlocks.TREEBORG_SLAB, slabDrops(GallifreyModBlocks.TREEBORG_SLAB));
        addDrop(GallifreyModBlocks.TREEBORG_BUTTON);
        addDrop(GallifreyModBlocks.TREEBORG_FENCE);
        addDrop(GallifreyModBlocks.TREEBORG_FENCE_GATE);
        addDrop(GallifreyModBlocks.TREEBORG_DOOR, doorDrops(GallifreyModBlocks.TREEBORG_DOOR));
        addDrop(GallifreyModBlocks.TREEBORG_TRAPDOOR);
        addDrop(GallifreyModBlocks.TREEBORG_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_TREEBORG_SIGN);
        addDrop(GallifreyModBlocks.HANGING_TREEBORG_SIGN);

        //ASH WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.ASH_SAPLING);
        addDrop(GallifreyModBlocks.ASH_LEAVES, leavesDrops(GallifreyModBlocks.ASH_LEAVES, GallifreyModBlocks.ASH_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.ASH_LOG);
        addDrop(GallifreyModBlocks.STRIP_ASH_LOG);
        addDrop(GallifreyModBlocks.ASH_WOOD);
        addDrop(GallifreyModBlocks.STRIP_ASH_WOOD);
        addDrop(GallifreyModBlocks.ASH_PLANKS);
        addDrop(GallifreyModBlocks.ASH_STAIRS);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_ASH_SAPLING);
        addDrop(GallifreyModBlocks.ASH_SLAB, slabDrops(GallifreyModBlocks.ASH_SLAB));
        addDrop(GallifreyModBlocks.ASH_BUTTON);
        addDrop(GallifreyModBlocks.ASH_FENCE);
        addDrop(GallifreyModBlocks.ASH_FENCE_GATE);
        addDrop(GallifreyModBlocks.ASH_DOOR, doorDrops(GallifreyModBlocks.ASH_DOOR));
        addDrop(GallifreyModBlocks.ASH_TRAPDOOR);
        addDrop(GallifreyModBlocks.ASH_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_ASH_SIGN);
        addDrop(GallifreyModBlocks.HANGING_ASH_SIGN);

        //MAPLE WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.MAPLE_SAPLING);
        addDrop(GallifreyModBlocks.MAPLE_LEAVES, leavesDrops(GallifreyModBlocks.MAPLE_LEAVES, GallifreyModBlocks.MAPLE_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.MAPLE_LOG);
        addDrop(GallifreyModBlocks.STRIP_MAPLE_LOG);
        addDrop(GallifreyModBlocks.MAPLE_WOOD);
        addDrop(GallifreyModBlocks.STRIP_MAPLE_WOOD);
        addDrop(GallifreyModBlocks.MAPLE_PLANKS);
        addDrop(GallifreyModBlocks.MAPLE_STAIRS);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_MAPLE_SAPLING);
        addDrop(GallifreyModBlocks.MAPLE_SLAB, slabDrops(GallifreyModBlocks.MAPLE_SLAB));
        addDrop(GallifreyModBlocks.MAPLE_BUTTON);
        addDrop(GallifreyModBlocks.MAPLE_FENCE);
        addDrop(GallifreyModBlocks.MAPLE_FENCE_GATE);
        addDrop(GallifreyModBlocks.MAPLE_DOOR, doorDrops(GallifreyModBlocks.MAPLE_DOOR));
        addDrop(GallifreyModBlocks.MAPLE_TRAPDOOR);
        addDrop(GallifreyModBlocks.MAPLE_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_MAPLE_SIGN);
        addDrop(GallifreyModBlocks.HANGING_MAPLE_SIGN);

        //MOONPINE WOOD SET BLOCK DROPS
        addDrop(GallifreyModBlocks.MOONPINE_SAPLING);
        addDrop(GallifreyModBlocks.MOONPINE_LEAVES, leavesDrops(GallifreyModBlocks.MOONPINE_LEAVES, GallifreyModBlocks.MOONPINE_SAPLING, 0.0025f));
        addDrop(GallifreyModBlocks.MOONPINE_LOG);
        addDrop(GallifreyModBlocks.STRIP_MOONPINE_LOG);
        addDrop(GallifreyModBlocks.MOONPINE_WOOD);
        addDrop(GallifreyModBlocks.STRIP_MOONPINE_WOOD);
        addDrop(GallifreyModBlocks.MOONPINE_PLANKS);
        addDrop(GallifreyModBlocks.MOONPINE_STAIRS);
        addPottedPlantDrops(GallifreyModBlocks.POTTED_MOONPINE_SAPLING);
        addDrop(GallifreyModBlocks.MOONPINE_SLAB, slabDrops(GallifreyModBlocks.MOONPINE_SLAB));
        addDrop(GallifreyModBlocks.MOONPINE_BUTTON);
        addDrop(GallifreyModBlocks.MOONPINE_FENCE);
        addDrop(GallifreyModBlocks.MOONPINE_FENCE_GATE);
        addDrop(GallifreyModBlocks.MOONPINE_DOOR, doorDrops(GallifreyModBlocks.MOONPINE_DOOR));
        addDrop(GallifreyModBlocks.MOONPINE_TRAPDOOR);
        addDrop(GallifreyModBlocks.MOONPINE_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.STANDING_MOONPINE_SIGN);
        addDrop(GallifreyModBlocks.HANGING_MOONPINE_SIGN);

        // CLASSIC BLOCK DROPS — mirror the vanilla block they are based on.
        addDrop(GallifreyModBlocks.CLASSIC_STONE, drops(GallifreyModBlocks.CLASSIC_STONE, WITH_SILK_TOUCH, ItemEntry.builder(GallifreyModBlocks.CLASSIC_COBBLE)));
        addDrop(GallifreyModBlocks.CLASSIC_GRASS, drops(GallifreyModBlocks.CLASSIC_GRASS, WITH_SILK_TOUCH, ItemEntry.builder(GallifreyModBlocks.CLASSIC_DIRT)));
        addDrop(GallifreyModBlocks.CLASSIC_DIRT);
        addDrop(GallifreyModBlocks.CLASSIC_COBBLE);
        addDrop(GallifreyModBlocks.CLASSIC_PLANKS);
        addDrop(GallifreyModBlocks.CLASSIC_STAIRS);
        addDrop(GallifreyModBlocks.CLASSIC_SLAB, slabDrops(GallifreyModBlocks.CLASSIC_SLAB));
        addDrop(GallifreyModBlocks.CLASSIC_FENCE);
        addDrop(GallifreyModBlocks.CLASSIC_FENCE_GATE);
        addDrop(GallifreyModBlocks.CLASSIC_LOG);
        addDrop(GallifreyModBlocks.CLASSIC_SAPLING);
        addDrop(GallifreyModBlocks.CLASSIC_LEAVES, leavesDrops(GallifreyModBlocks.CLASSIC_LEAVES, GallifreyModBlocks.CLASSIC_SAPLING, 0.05f));
        addDrop(GallifreyModBlocks.CLASSIC_SAND);
        addDrop(GallifreyModBlocks.CLASSIC_GRAVEL);
        addDrop(GallifreyModBlocks.CLASSIC_GOLD);
        addDrop(GallifreyModBlocks.CLASSIC_IRON);
        addDrop(GallifreyModBlocks.CLASSIC_GLASS, dropsWithSilkTouch(GallifreyModBlocks.CLASSIC_GLASS));
        addDrop(GallifreyModBlocks.CLASSIC_GLASS_PANE, dropsWithSilkTouch(GallifreyModBlocks.CLASSIC_GLASS_PANE));
        addDrop(GallifreyModBlocks.CLASSIC_WOODEN_BUTTON);
        addDrop(GallifreyModBlocks.CLASSIC_STONE_BUTTON);
        addDrop(GallifreyModBlocks.CLASSIC_WOODEN_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.CLASSIC_STONE_PRESSURE_PLATE);
        addDrop(GallifreyModBlocks.CLASSIC_SPONGE);
        addDrop(GallifreyModBlocks.CLASSIC_BRICKS);
        addDrop(GallifreyModBlocks.CLASSIC_TNT);
        addDrop(GallifreyModBlocks.CLASSIC_RED_FLOWER);
        addDrop(GallifreyModBlocks.CLASSIC_YELLOW_FLOWER);
        addDrop(GallifreyModBlocks.CLASSIC_OBSIDIAN);

        // Classic ores mirror the corresponding vanilla ore drops, including Fortune/Silk Touch behavior.
        addDrop(GallifreyModBlocks.CLASSIC_COAL_ORE, oreDrops(GallifreyModBlocks.CLASSIC_COAL_ORE, net.minecraft.item.Items.COAL));
        addDrop(GallifreyModBlocks.CLASSIC_IRON_ORE, oreDrops(GallifreyModBlocks.CLASSIC_IRON_ORE, net.minecraft.item.Items.IRON_INGOT));
        addDrop(GallifreyModBlocks.CLASSIC_GOLD_ORE, oreDrops(GallifreyModBlocks.CLASSIC_GOLD_ORE, net.minecraft.item.Items.GOLD_INGOT));
        addDrop(GallifreyModBlocks.CLASSIC_REDSTONE_ORE, oreDrops(GallifreyModBlocks.CLASSIC_REDSTONE_ORE, net.minecraft.item.Items.REDSTONE));
        addDrop(GallifreyModBlocks.CLASSIC_LAPIS_ORE, oreDrops(GallifreyModBlocks.CLASSIC_LAPIS_ORE, net.minecraft.item.Items.LAPIS_LAZULI));
        addDrop(GallifreyModBlocks.CLASSIC_DIAMOND_ORE, oreDrops(GallifreyModBlocks.CLASSIC_DIAMOND_ORE, net.minecraft.item.Items.DIAMOND));

        // Classic Nether blocks mirror their vanilla Nether counterparts.
        addDrop(GallifreyModBlocks.CLASSIC_NETHER_NETHERRACK);
        addDrop(GallifreyModBlocks.CLASSIC_NETHER_SOUL_SAND);
        addDrop(GallifreyModBlocks.CLASSIC_NETHER_GLOWSTONE, drops(GallifreyModBlocks.CLASSIC_NETHER_GLOWSTONE, WITH_SILK_TOUCH,
                ItemEntry.builder(net.minecraft.item.Items.GLOWSTONE_DUST)
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0F, 4.0F)))
                        .apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE, 1))));
        addDrop(GallifreyModBlocks.CLASSIC_NETHER_MAGMA);
        addDrop(GallifreyModBlocks.CLASSIC_NETHER_QUARTZ_ORE, oreDrops(GallifreyModBlocks.CLASSIC_NETHER_QUARTZ_ORE, net.minecraft.item.Items.QUARTZ));

        // MARS BLOCK DROPS
        addDrop(GallifreyModBlocks.MARS_SAND);
        addDrop(GallifreyModBlocks.MARS_SANDSTONE);
        addDrop(GallifreyModBlocks.MARS_STONE);
        addDrop(GallifreyModBlocks.MARS_COBBLESTONE);
        addDrop(GallifreyModBlocks.MARS_ANDESITE);
        addDrop(GallifreyModBlocks.MARS_DIORITE);
        addDrop(GallifreyModBlocks.MARS_GRANITE);
        addDrop(GallifreyModBlocks.POLISHED_MARS_STONE);
        addDrop(GallifreyModBlocks.MARS_POLISHED_ANDESITE);
        addDrop(GallifreyModBlocks.MARS_POLISHED_DIORITE);
        addDrop(GallifreyModBlocks.MARS_POLISHED_GRANITE);
        addDrop(GallifreyModBlocks.MARS_STONE_BRICKS);
        addDrop(GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED);
        addDrop(GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS);
        addDrop(GallifreyModBlocks.MARS_IRON_ORE, oreDrops(GallifreyModBlocks.MARS_IRON_ORE, net.minecraft.item.Items.RAW_IRON));

        // MISC BLOCK DROPS
        addDrop(GallifreyModBlocks.RAW_STEEL_BLOCK);
        addDrop(GallifreyModBlocks.STEEL_BLOCK);
        addDrop(GallifreyModBlocks.PISS_CRYSTAL);
        addDrop(GallifreyModBlocks.REINFORCED_STEEL_BLOCK);
        addDrop(GallifreyModBlocks.HARTNELL_BLOCK);
        addDrop(GallifreyModBlocks.HARTNELL_WALL);

        // ROUNDELS
        addDrop(GallifreyModBlocks.BASALT_ROUNDEL);
        addDrop(GallifreyModBlocks.BONE_ROUNDEL);
        addDrop(GallifreyModBlocks.STRUCTURE_ROUNDEL);
        addDrop(GallifreyModBlocks.LODESTONE_ROUNDEL);
        addDrop(GallifreyModBlocks.QUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.AMBQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.BLUEQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.BLACK_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.BLUE_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.BROWN_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.COPPER_ROUNDEL);
        addDrop(GallifreyModBlocks.CRIMQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.OBSIQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.VERDQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.VIOQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.CYQUARTZ_ROUNDEL);
        addDrop(GallifreyModBlocks.DIRT_ROUNDEL);
        addDrop(GallifreyModBlocks.END_STONE_BRICKS_ROUNDEL);
        addDrop(GallifreyModBlocks.EXPOSED_COPPER_ROUNDEL);
        addDrop(GallifreyModBlocks.GRAY_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.GREEN_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.LIGHT_BLUE_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.LIGHT_GRAY_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.LIME_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.MAGENTA_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.MOSS_ROUNDEL);
        addDrop(GallifreyModBlocks.ORANGE_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.OXIDIZED_COPPER_ROUNDEL);
        addDrop(GallifreyModBlocks.POLISHED_DIORITE_ROUNDEL);
        addDrop(GallifreyModBlocks.POLISHED_ANDESITE_ROUNDEL);
        addDrop(GallifreyModBlocks.POLISHED_DEEPSLATE_ROUNDEL);
        addDrop(GallifreyModBlocks.PINK_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.POLISHED_GRANITE_ROUNDEL);
        addDrop(GallifreyModBlocks.PURPLE_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.RED_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.SANDSTONE_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_ACACIA_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_BIRCH_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_CHERRY_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_DARK_OAK_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_JUNGLE_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_MANGROVE_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_OAK_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.STRIPPED_SPRUCE_LOG_ROUNDEL);
        addDrop(GallifreyModBlocks.WEATHERED_COPPER_ROUNDEL);
        addDrop(GallifreyModBlocks.WHITE_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.YELLOW_CONCRETE_ROUNDEL);
        addDrop(GallifreyModBlocks.HARTNELL_ROUNDEL);
        addDrop(GallifreyModBlocks.TREE_TAPPER);
        addDrop(GallifreyModBlocks.SONIC_CRYSTAL_ORE, oreDrops(GallifreyModBlocks.SONIC_CRYSTAL_ORE, GallifreyModItems.RAW_SONIC_CRYSTAL));
        addDrop(GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE, oreDrops(GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE, GallifreyModItems.RAW_SONIC_CRYSTAL));
        addDrop(GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE, oreDrops(GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE, GallifreyModItems.RAW_SONIC_CRYSTAL));
        addDrop(GallifreyModBlocks.WHITE_POINT_ORE, oreDrops(GallifreyModBlocks.WHITE_POINT_ORE, GallifreyModItems.WHITE_POINT_STAR));
        addDrop(GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE, oreDrops(GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE, GallifreyModItems.WHITE_POINT_STAR));
        addDrop(GallifreyModBlocks.NETHER_WHITE_POINT_ORE, oreDrops(GallifreyModBlocks.NETHER_WHITE_POINT_ORE, GallifreyModItems.WHITE_POINT_STAR));
        addDrop(GallifreyModBlocks.PREHISTORIC_ORE, oreDrops(GallifreyModBlocks.PREHISTORIC_ORE, GallifreyModItems.PREHISTORIC_INGOT));
        addDrop(GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE, oreDrops(GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE, GallifreyModItems.PREHISTORIC_INGOT));

        // Atrium block was previously omitted from the loot provider.
        addDrop(GallifreyModBlocks.ATRIUM_BLOCK);

        // Skaro blocks
        addDrop(GallifreyModBlocks.EXQUISITE_CAT);
        addDrop(GallifreyModBlocks.GOOD_HEAVENS);
        addDrop(GallifreyModBlocks.SKARO_STONE,
                drops(GallifreyModBlocks.SKARO_STONE, GallifreyModBlocks.SKARO_COBBLESTONE));
        addDrop(GallifreyModBlocks.SKARO_COBBLESTONE);
        addDrop(GallifreyModBlocks.SKARO_ANDESITE);
        addDrop(GallifreyModBlocks.SKARO_DIORITE);
        addDrop(GallifreyModBlocks.SKARO_GRANITE);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_ANDESITE);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_DIORITE);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_GRANITE);
        addDrop(GallifreyModBlocks.POLISHED_SKARO_STONE);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE,
                drops(GallifreyModBlocks.SKARO_DEEPSLATE, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE));
        addDrop(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_TILES);
        addDrop(GallifreyModBlocks.SKARO_STONE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_STONE_SLAB, slabDrops(GallifreyModBlocks.SKARO_STONE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_COBBLESTONE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_COBBLESTONE_SLAB, slabDrops(GallifreyModBlocks.SKARO_COBBLESTONE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_ANDESITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_ANDESITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_ANDESITE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_DIORITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_DIORITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_DIORITE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_GRANITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_GRANITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_GRANITE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_POLISHED_DIORITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_POLISHED_GRANITE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB, slabDrops(GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB));
        addDrop(GallifreyModBlocks.POLISHED_SKARO_STONE_STAIRS);
        addDrop(GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB, slabDrops(GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_SLAB, slabDrops(GallifreyModBlocks.SKARO_DEEPSLATE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB, slabDrops(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB));
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_STAIRS);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB, slabDrops(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB));
        addDrop(GallifreyModBlocks.SKARO_STONE_WALL);
        addDrop(GallifreyModBlocks.SKARO_COBBLESTONE_WALL);
        addDrop(GallifreyModBlocks.SKARO_ANDESITE_WALL);
        addDrop(GallifreyModBlocks.SKARO_DIORITE_WALL);
        addDrop(GallifreyModBlocks.SKARO_GRANITE_WALL);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL);
        addDrop(GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL);
        addDrop(GallifreyModBlocks.POLISHED_SKARO_STONE_WALL);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_WALL);
        addDrop(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL);
        addDrop(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL);
        addDrop(GallifreyModBlocks.KALETITE_BRICKS);
        addDrop(GallifreyModBlocks.WASTED_DIRT);
        addDrop(GallifreyModBlocks.LOST_DIRT);
        addDrop(GallifreyModBlocks.WASTED_GRASS);
        addDrop(GallifreyModBlocks.WASTED_LEAVES);
        addDrop(GallifreyModBlocks.WASTED_LOG);
        addDrop(GallifreyModBlocks.WASTED_PLANKS);
        addDrop(GallifreyModBlocks.WASTED_SLAB, slabDrops(GallifreyModBlocks.WASTED_SLAB));
        addDrop(GallifreyModBlocks.DALEKANIUM_BLOCK);
        addDrop(GallifreyModBlocks.DALEKANIUM_ORE, oreDrops(GallifreyModBlocks.DALEKANIUM_ORE, GallifreyModItems.DALEKANIUM_INGOT));
        addDrop(GallifreyModBlocks.DEEPSLATE_DALEKANIUM_ORE, oreDrops(GallifreyModBlocks.DEEPSLATE_DALEKANIUM_ORE, GallifreyModItems.DALEKANIUM_INGOT));
    }
}