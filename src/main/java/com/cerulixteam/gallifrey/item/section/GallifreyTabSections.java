package com.cerulixteam.gallifrey.item.section;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Every section of the Gallifrey creative tab.
 *
 * The order of {@link #ALL} is the order of the sidebar buttons AND the order the
 * items appear in when no section is selected.
 *
 * To add a section: declare it below, add it to ALL, and add a lang line
 * "creative_section.gallifrey.<id>". Six fit in the sidebar as-is.
 */
public final class GallifreyTabSections {

    public static final CreativeSection WOOD_TYPES = CreativeSection.of(
            "wood_types",
            () -> new ItemStack(GallifreyModBlocks.TARDIS_LOG),
            entries -> {
                // TARDIS wood set
                entries.add(GallifreyModBlocks.TARDIS_SAPLING);
                entries.add(GallifreyModBlocks.TARDIS_LEAVES);
                entries.add(GallifreyModBlocks.TARDIS_LOG);
                entries.add(GallifreyModBlocks.TARDIS_WOOD);
                entries.add(GallifreyModBlocks.STRIP_TARDIS_LOG);
                entries.add(GallifreyModBlocks.STRIP_TARDIS_WOOD);
                entries.add(GallifreyModBlocks.TARDIS_PLANKS);
                entries.add(GallifreyModBlocks.TARDIS_STAIRS);
                entries.add(GallifreyModBlocks.TARDIS_SLAB);
                entries.add(GallifreyModBlocks.TARDIS_FENCE);
                entries.add(GallifreyModBlocks.TARDIS_FENCE_GATE);
                entries.add(GallifreyModBlocks.TARDIS_WOOD_DOOR);
                entries.add(GallifreyModBlocks.TARDIS_TRAPDOOR);
                entries.add(GallifreyModBlocks.TARDIS_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.TARDIS_BUTTON);
                entries.add(GallifreyModItems.TARDIS_SIGN);
                entries.add(GallifreyModItems.HANGING_TARDIS_SIGN);
                entries.add(GallifreyModItems.TARDIS_BOAT);
                entries.add(GallifreyModItems.TARDIS_CHEST_BOAT);

                // Ulanda wood set
                entries.add(GallifreyModBlocks.ULANDA_SAPLING);
                entries.add(GallifreyModBlocks.ULANDA_LEAVES);
                entries.add(GallifreyModBlocks.ULANDA_LOG);
                entries.add(GallifreyModBlocks.ULANDA_WOOD);
                entries.add(GallifreyModBlocks.STRIP_ULANDA_LOG);
                entries.add(GallifreyModBlocks.STRIP_ULANDA_WOOD);
                entries.add(GallifreyModBlocks.ULANDA_PLANKS);
                entries.add(GallifreyModBlocks.ULANDA_STAIRS);
                entries.add(GallifreyModBlocks.ULANDA_SLAB);
                entries.add(GallifreyModBlocks.ULANDA_FENCE);
                entries.add(GallifreyModBlocks.ULANDA_FENCE_GATE);
                entries.add(GallifreyModBlocks.ULANDA_DOOR);
                entries.add(GallifreyModBlocks.ULANDA_TRAPDOOR);
                entries.add(GallifreyModBlocks.ULANDA_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.ULANDA_BUTTON);
                entries.add(GallifreyModItems.ULANDA_SIGN);
                entries.add(GallifreyModItems.HANGING_ULANDA_SIGN);
                entries.add(GallifreyModItems.ULANDA_BOAT);
                entries.add(GallifreyModItems.ULANDA_CHEST_BOAT);

                // Treeborg wood set
                entries.add(GallifreyModBlocks.TREEBORG_SAPLING);
                entries.add(GallifreyModBlocks.TREEBORG_LEAVES);
                entries.add(GallifreyModBlocks.TREEBORG_LOG);
                entries.add(GallifreyModBlocks.TREEBORG_WOOD);
                entries.add(GallifreyModBlocks.STRIP_TREEBORG_LOG);
                entries.add(GallifreyModBlocks.STRIP_TREEBORG_WOOD);
                entries.add(GallifreyModBlocks.TREEBORG_PLANKS);
                entries.add(GallifreyModBlocks.TREEBORG_STAIRS);
                entries.add(GallifreyModBlocks.TREEBORG_SLAB);
                entries.add(GallifreyModBlocks.TREEBORG_FENCE);
                entries.add(GallifreyModBlocks.TREEBORG_FENCE_GATE);
                entries.add(GallifreyModBlocks.TREEBORG_DOOR);
                entries.add(GallifreyModBlocks.TREEBORG_TRAPDOOR);
                entries.add(GallifreyModBlocks.TREEBORG_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.TREEBORG_BUTTON);
                entries.add(GallifreyModItems.TREEBORG_SIGN);
                entries.add(GallifreyModItems.HANGING_TREEBORG_SIGN);
                entries.add(GallifreyModItems.TREEBORG_BOAT);
                entries.add(GallifreyModItems.TREEBORG_CHEST_BOAT);

                // Ash wood set
                entries.add(GallifreyModBlocks.ASH_SAPLING);
                entries.add(GallifreyModBlocks.ASH_LEAVES);
                entries.add(GallifreyModBlocks.ASH_LOG);
                entries.add(GallifreyModBlocks.ASH_WOOD);
                entries.add(GallifreyModBlocks.STRIP_ASH_LOG);
                entries.add(GallifreyModBlocks.STRIP_ASH_WOOD);
                entries.add(GallifreyModBlocks.ASH_PLANKS);
                entries.add(GallifreyModBlocks.ASH_STAIRS);
                entries.add(GallifreyModBlocks.ASH_SLAB);
                entries.add(GallifreyModBlocks.ASH_FENCE);
                entries.add(GallifreyModBlocks.ASH_FENCE_GATE);
                entries.add(GallifreyModBlocks.ASH_DOOR);
                entries.add(GallifreyModBlocks.ASH_TRAPDOOR);
                entries.add(GallifreyModBlocks.ASH_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.ASH_BUTTON);
                entries.add(GallifreyModItems.ASH_SIGN);
                entries.add(GallifreyModItems.HANGING_ASH_SIGN);
                entries.add(GallifreyModItems.ASH_BOAT);
                entries.add(GallifreyModItems.ASH_CHEST_BOAT);

                // Maple wood set
                entries.add(GallifreyModBlocks.MAPLE_SAPLING);
                entries.add(GallifreyModBlocks.MAPLE_LEAVES);
                entries.add(GallifreyModBlocks.MAPLE_LOG);
                entries.add(GallifreyModBlocks.MAPLE_WOOD);
                entries.add(GallifreyModBlocks.STRIP_MAPLE_LOG);
                entries.add(GallifreyModBlocks.STRIP_MAPLE_WOOD);
                entries.add(GallifreyModBlocks.MAPLE_PLANKS);
                entries.add(GallifreyModBlocks.MAPLE_STAIRS);
                entries.add(GallifreyModBlocks.MAPLE_SLAB);
                entries.add(GallifreyModBlocks.MAPLE_FENCE);
                entries.add(GallifreyModBlocks.MAPLE_FENCE_GATE);
                entries.add(GallifreyModBlocks.MAPLE_DOOR);
                entries.add(GallifreyModBlocks.MAPLE_TRAPDOOR);
                entries.add(GallifreyModBlocks.MAPLE_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.MAPLE_BUTTON);
                entries.add(GallifreyModItems.MAPLE_SIGN);
                entries.add(GallifreyModItems.HANGING_MAPLE_SIGN);
                entries.add(GallifreyModItems.MAPLE_BOAT);
                entries.add(GallifreyModItems.MAPLE_CHEST_BOAT);

                // Moonpine wood set
                entries.add(GallifreyModBlocks.MOONPINE_SAPLING);
                entries.add(GallifreyModBlocks.MOONPINE_LEAVES);
                entries.add(GallifreyModBlocks.MOONPINE_LOG);
                entries.add(GallifreyModBlocks.MOONPINE_WOOD);
                entries.add(GallifreyModBlocks.STRIP_MOONPINE_LOG);
                entries.add(GallifreyModBlocks.STRIP_MOONPINE_WOOD);
                entries.add(GallifreyModBlocks.MOONPINE_PLANKS);
                entries.add(GallifreyModBlocks.MOONPINE_STAIRS);
                entries.add(GallifreyModBlocks.MOONPINE_SLAB);
                entries.add(GallifreyModBlocks.MOONPINE_FENCE);
                entries.add(GallifreyModBlocks.MOONPINE_FENCE_GATE);
                entries.add(GallifreyModBlocks.MOONPINE_DOOR);
                entries.add(GallifreyModBlocks.MOONPINE_TRAPDOOR);
                entries.add(GallifreyModBlocks.MOONPINE_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.MOONPINE_BUTTON);
                entries.add(GallifreyModItems.MOONPINE_SIGN);
                entries.add(GallifreyModItems.HANGING_MOONPINE_SIGN);
                entries.add(GallifreyModItems.MOONPINE_BOAT);
                entries.add(GallifreyModItems.MOONPINE_CHEST_BOAT);

                // Prehistoric wood set
            }
    );

    public static final CreativeSection PREHISTORIC = CreativeSection.of(
            "prehistoric",
            () -> new ItemStack(GallifreyModBlocks.PREHISTORIC_BLOCK),
            entries -> {
                entries.add(GallifreyModBlocks.PREHISTORIC_SAPLING);
                entries.add(GallifreyModBlocks.PREHISTORIC_VINE);
                entries.add(GallifreyModBlocks.PREHISTORIC_LEAVES);
                entries.add(GallifreyModBlocks.PREHISTORIC_LOG);
                entries.add(GallifreyModBlocks.PREHISTORIC_PLANKS);
                entries.add(GallifreyModBlocks.PREHISTORIC_STAIRS);
                entries.add(GallifreyModBlocks.PREHISTORIC_SLAB);
                entries.add(GallifreyModBlocks.PREHISTORIC_FENCE);
                entries.add(GallifreyModBlocks.PREHISTORIC_FENCE_GATE);
                entries.add(GallifreyModBlocks.PREHISTORIC_DOOR);
                entries.add(GallifreyModBlocks.PREHISTORIC_TRAPDOOR);
                entries.add(GallifreyModBlocks.PREHISTORIC_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.PREHISTORIC_BUTTON);
                entries.add(GallifreyModBlocks.PREHISTORIC_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE);
                entries.add(GallifreyModBlocks.PREHISTORIC_BLOCK);
                entries.add(GallifreyModItems.PREHISTORIC_INGOT);
                entries.add(GallifreyModItems.PREHISTORIC_UPGRADE_SMITHING_TEMPLATE);
                entries.add(GallifreyModItems.PREHISTORIC_SWORD);
                entries.add(GallifreyModItems.PREHISTORIC_PICKAXE);
                entries.add(GallifreyModItems.PREHISTORIC_AXE);
                entries.add(GallifreyModItems.PREHISTORIC_SHOVEL);
                entries.add(GallifreyModItems.PREHISTORIC_HOE);
                entries.add(GallifreyModItems.PREHISTORIC_HELMET);
                entries.add(GallifreyModItems.PREHISTORIC_CHESTPLATE);
                entries.add(GallifreyModItems.PREHISTORIC_LEGGINGS);
                entries.add(GallifreyModItems.PREHISTORIC_BOOTS);
                entries.add(GallifreyModItems.CAVEMAN_SPAWN_EGG);
            }
    );

    public static final CreativeSection MISC = CreativeSection.of(
            "misc",
            () -> new ItemStack(GallifreyModItems.SONIC_SCREWDRIVER),
            entries -> {
                // Vortex Manipulator and its parts
                entries.add(GallifreyModItems.VORTEX_MANIPULATOR);
                entries.add(GallifreyModBlocks.WHITE_POINT_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE);
                entries.add(GallifreyModBlocks.NETHER_WHITE_POINT_ORE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_RESIDUE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_UPGRADE_SMITHING_TEMPLATE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_SWORD);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_PICKAXE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_AXE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_SHOVEL);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_HOE);
                if (com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.WHITE_POINT_STAR_KNIFE != null) entries.add(com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.WHITE_POINT_STAR_KNIFE); // Farmer's Delight only
                entries.add(GallifreyModItems.WHITE_POINT_STAR_HELMET);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_CHESTPLATE);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_LEGGINGS);
                entries.add(GallifreyModItems.WHITE_POINT_STAR_BOOTS);

                // Atrium
                entries.add(GallifreyModBlocks.ATRIUM_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_ATRIUM_ORE);
                entries.add(GallifreyModBlocks.ATRIUM_BLOCK);
                entries.add(GallifreyModItems.ATRIUM);
                entries.add(GallifreyModItems.ATRIUM_FUEL);
                entries.add(GallifreyModItems.ENERGIZED_ATRIUM);
                entries.add(GallifreyModItems.ATRIUM_CORE);
                entries.add(GallifreyModItems.BLANK_CIRCUIT);
                entries.add(GallifreyModItems.LOCATION_CIRCUIT);
                entries.add(GallifreyModItems.INTERFACE_CIRCUIT);
                entries.add(GallifreyModItems.DIMENSION_CIRCUIT);
                entries.add(GallifreyModItems.DW_XIV_MUSIC_DISC);
                entries.add(GallifreyModItems.GALLIFREY_MUSIC_DISC);
                entries.add(GallifreyModItems.VALEDECEM_MUSIC_DISC);
                entries.add(GallifreyModItems.DUGGA_DOO_MUSIC_DISC);

                //gallifreyan workbench
                entries.add(GallifreyModBlocks.GALLIFREYAN_WORKBENCH);

                // Tapping
                entries.add(GallifreyModBlocks.TREE_TAPPER);
                entries.add(GallifreyModItems.SILICONE);
                entries.add(GallifreyModItems.TREEBORG_PASTE);
                entries.add(GallifreyModItems.MAPLE_SYRUP);
                entries.add(GallifreyModItems.HUON_PARTICLE);

                // Sonics
                entries.add(GallifreyModItems.SONIC_SCREWDRIVER);
                entries.add(GallifreyModItems.SONIC_SHADES);
                entries.add(GallifreyModBlocks.SONIC_WORKSHOP);
                entries.add(GallifreyModItems.RAW_SONIC_CRYSTAL);
                entries.add(GallifreyModItems.REFINED_SONIC_CRYSTAL);
                entries.add(GallifreyModBlocks.SONIC_CRYSTAL_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE);
                entries.add(GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE);

                // TARDIS
                entries.add(GallifreyModBlocks.TARDIS_EXTERIOR);

                //STEEL
                entries.add(GallifreyModItems.RAW_STEEL);
                entries.add(GallifreyModItems.STEEL_INGOT);
                entries.add(GallifreyModBlocks.RAW_STEEL_BLOCK);
                entries.add(GallifreyModBlocks.STEEL_BLOCK);
                entries.add(GallifreyModBlocks.REINFORCED_STEEL_BLOCK);
                entries.add(GallifreyModBlocks.GRATE_BLOCK);
                entries.add(GallifreyModBlocks.THICK_GRATE_BLOCK);
                entries.add(GallifreyModBlocks.RUSTY_GRATE_BLOCK);
                entries.add(GallifreyModBlocks.RUSTY_THICK_GRATE_BLOCK);
                entries.add(GallifreyModItems.STEEL_TEMPLATE);
                entries.add(GallifreyModItems.STEEL_SWORD);
                entries.add(GallifreyModItems.STEEL_PICKAXE);
                entries.add(GallifreyModItems.STEEL_AXE);
                entries.add(GallifreyModItems.STEEL_SHOVEL);
                entries.add(GallifreyModItems.STEEL_HOE);
                if (com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.STEEL_KNIFE != null) entries.add(com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.STEEL_KNIFE); // Farmer's Delight only
                entries.add(GallifreyModItems.STEEL_HELMET);
                entries.add(GallifreyModItems.STEEL_CHESTPLATE);
                entries.add(GallifreyModItems.STEEL_LEGGINGS);
                entries.add(GallifreyModItems.STEEL_BOOTS);

                // Emerald equipment (diamond-tier; accepts regular or classic emeralds)
                entries.add(GallifreyModItems.EMERALD_SWORD);
                entries.add(GallifreyModItems.EMERALD_PICKAXE);
                entries.add(GallifreyModItems.EMERALD_AXE);
                entries.add(GallifreyModItems.EMERALD_SHOVEL);
                entries.add(GallifreyModItems.EMERALD_HOE);
                entries.add(GallifreyModItems.EMERALD_HELMET);
                entries.add(GallifreyModItems.EMERALD_CHESTPLATE);
                entries.add(GallifreyModItems.EMERALD_LEGGINGS);
                entries.add(GallifreyModItems.EMERALD_BOOTS);

                // Ruby equipment and materials
                entries.add(GallifreyModItems.RUBY);
                entries.add(GallifreyModBlocks.RUBY_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_RUBY_ORE);
                entries.add(GallifreyModBlocks.RUBY_BLOCK);
                entries.add(GallifreyModItems.RUBY_SWORD);
                entries.add(GallifreyModItems.RUBY_PICKAXE);
                entries.add(GallifreyModItems.RUBY_AXE);
                entries.add(GallifreyModItems.RUBY_SHOVEL);
                entries.add(GallifreyModItems.RUBY_HOE);
                entries.add(GallifreyModItems.RUBY_HELMET);
                entries.add(GallifreyModItems.RUBY_CHESTPLATE);
                entries.add(GallifreyModItems.RUBY_LEGGINGS);
                entries.add(GallifreyModItems.RUBY_BOOTS);

                // Sapphire equipment follows ruby
                entries.add(GallifreyModItems.SAPPHIRE);
                entries.add(GallifreyModBlocks.SAPPHIRE_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_SAPPHIRE_ORE);
                entries.add(GallifreyModBlocks.SAPPHIRE_BLOCK);
                entries.add(GallifreyModItems.SAPPHIRE_SWORD);
                entries.add(GallifreyModItems.SAPPHIRE_PICKAXE);
                entries.add(GallifreyModItems.SAPPHIRE_AXE);
                entries.add(GallifreyModItems.SAPPHIRE_SHOVEL);
                entries.add(GallifreyModItems.SAPPHIRE_HOE);
                entries.add(GallifreyModItems.SAPPHIRE_HELMET);
                entries.add(GallifreyModItems.SAPPHIRE_CHESTPLATE);
                entries.add(GallifreyModItems.SAPPHIRE_LEGGINGS);
                entries.add(GallifreyModItems.SAPPHIRE_BOOTS);

                //Alt Shit
                entries.add(GallifreyModBlocks.HARTNELL_BLOCK);
                entries.add(GallifreyModBlocks.HARTNELL_WALL);
                entries.add(GallifreyModBlocks.GOOD_HEAVENS);
                entries.add(GallifreyModBlocks.EXQUISITE_CAT);
                entries.add(GallifreyModBlocks.LOST_DIRT);

                //Space suit
                entries.add(GallifreyModItems.SPACE_SUIT_HELMET);
                entries.add(GallifreyModItems.SPACE_SUIT_CHESTPLATE);
                entries.add(GallifreyModItems.SPACE_SUIT_LEGGINGS);
                entries.add(GallifreyModItems.SPACE_SUIT_BOOTS);

                // Clothing
                entries.add(GallifreyModItems.FEZ);
                entries.add(GallifreyModItems.FANCYFEZ);
                entries.add(GallifreyModItems.PURPLEFEZ);
                entries.add(GallifreyModItems.GREENFEZ);
                entries.add(GallifreyModItems.ORANGEFEZ);
                entries.add(GallifreyModItems.BLUEFEZ);
                entries.add(GallifreyModItems.DARKBLUEFEZ);
                entries.add(GallifreyModItems.PINKFEZ);
                entries.add(GallifreyModItems.GREYFEZ);
                entries.add(GallifreyModItems.YELLOWFEZ);
                entries.add(GallifreyModItems.TRUSTABLE_HAT);
                entries.add(GallifreyModItems.EYESTALK);
                entries.add(GallifreyModItems.OMEGA_HELMET);

                entries.add(GallifreyModItems.TIMELORD_ROBE_CHESTPLATE);
                entries.add(GallifreyModItems.TIMELORD_ROBE_LEGGINGS);
                entries.add(GallifreyModItems.TIMELORD_ROBE_BOOTS);
                entries.add(GallifreyModItems.LAB_COAT_CHESTPLATE);
                entries.add(GallifreyModItems.LAB_COAT_LEGGINGS);
                entries.add(GallifreyModItems.LAB_COAT_BOOTS);


                // Cyber spawn eggs appear immediately below the clothing items.
                entries.add(GallifreyModItems.MONDASIAN_CYBERMAN_SPAWN_EGG);
                entries.add(GallifreyModItems.PROTO_CYBERMAN_SPAWN_EGG);
                entries.add(GallifreyModItems.CYBERMAT_SPAWN_EGG);
            }
    );

    public static final CreativeSection ROUNDELS = CreativeSection.of(
            "roundels",
            () -> new ItemStack(GallifreyModBlocks.STRUCTURE_ROUNDEL),
            entries -> {
                entries.add(GallifreyModBlocks.MOSS_ROUNDEL);
                entries.add(GallifreyModBlocks.DIRT_ROUNDEL);
                entries.add(GallifreyModBlocks.END_STONE_BRICKS_ROUNDEL);
                entries.add(GallifreyModBlocks.SANDSTONE_ROUNDEL);
                entries.add(GallifreyModBlocks.HARTNELL_ROUNDEL);
                entries.add(GallifreyModBlocks.BASALT_ROUNDEL);
                entries.add(GallifreyModBlocks.BONE_ROUNDEL);
                entries.add(GallifreyModBlocks.STRUCTURE_ROUNDEL);
                entries.add(GallifreyModBlocks.LODESTONE_ROUNDEL);
                entries.add(GallifreyModBlocks.VANILLA_QUATZ_ROUNDEL);
                entries.add(GallifreyModBlocks.REACTOR_ROUNDEL);
                entries.add(GallifreyModBlocks.QUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.BLUEQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.CRIMQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.AMBQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.OBSIQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.VERDQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.VIOQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.CYQUARTZ_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_ACACIA_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_BIRCH_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_CHERRY_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_DARK_OAK_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_JUNGLE_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_MANGROVE_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_OAK_LOG_ROUNDEL);
                entries.add(GallifreyModBlocks.STRIPPED_SPRUCE_LOG_ROUNDEL);;
                entries.add(GallifreyModBlocks.COPPER_ROUNDEL);
                entries.add(GallifreyModBlocks.EXPOSED_COPPER_ROUNDEL);
                entries.add(GallifreyModBlocks.WEATHERED_COPPER_ROUNDEL);
                entries.add(GallifreyModBlocks.OXIDIZED_COPPER_ROUNDEL);
                //CONCRETE ROUNDELS
                entries.add(GallifreyModBlocks.GRAY_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.GREEN_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.LIGHT_BLUE_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.LIGHT_GRAY_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.LIME_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.MAGENTA_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.ORANGE_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.PINK_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.PURPLE_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.RED_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.WHITE_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.YELLOW_CONCRETE_ROUNDEL);
                entries.add(GallifreyModBlocks.POLISHED_ANDESITE_ROUNDEL);
                entries.add(GallifreyModBlocks.POLISHED_DEEPSLATE_ROUNDEL);
                entries.add(GallifreyModBlocks.POLISHED_DIORITE_ROUNDEL);
                entries.add(GallifreyModBlocks.POLISHED_GRANITE_ROUNDEL);
            }
    );

    public static final CreativeSection MARS = CreativeSection.of(
            "mars",
            () -> new ItemStack(GallifreyModBlocks.MARS_STONE),
            entries -> {
                entries.add(GallifreyModBlocks.MARS_SAND);
                entries.add(GallifreyModBlocks.MARS_SANDSTONE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_SANDSTONE_SLAB);
                entries.add(GallifreyModBlocks.MARS_STONE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_STONE_SLAB);
                entries.add(GallifreyModBlocks.MARS_COBBLESTONE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_COBBLESTONE_SLAB);
                entries.add(GallifreyModBlocks.MARS_ANDESITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_ANDESITE_SLAB);
                entries.add(GallifreyModBlocks.MARS_DIORITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_DIORITE_SLAB);
                entries.add(GallifreyModBlocks.MARS_GRANITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_GRANITE_SLAB);
                entries.add(GallifreyModBlocks.POLISHED_MARS_STONE_STAIRS);
                entries.add(GallifreyModBlocks.POLISHED_MARS_STONE_SLAB);
                entries.add(GallifreyModBlocks.MARS_POLISHED_ANDESITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_POLISHED_ANDESITE_SLAB);
                entries.add(GallifreyModBlocks.MARS_POLISHED_DIORITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_POLISHED_DIORITE_SLAB);
                entries.add(GallifreyModBlocks.MARS_POLISHED_GRANITE_STAIRS);
                entries.add(GallifreyModBlocks.MARS_POLISHED_GRANITE_SLAB);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS_STAIRS);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS_SLAB);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED_STAIRS);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED_SLAB);
                entries.add(GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS_STAIRS);
                entries.add(GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS_SLAB);
                entries.add(GallifreyModBlocks.MARS_SANDSTONE);
                entries.add(GallifreyModBlocks.MARS_STONE);
                entries.add(GallifreyModBlocks.MARS_COBBLESTONE);
                entries.add(GallifreyModBlocks.MARS_ANDESITE);
                entries.add(GallifreyModBlocks.MARS_DIORITE);
                entries.add(GallifreyModBlocks.MARS_GRANITE);
                entries.add(GallifreyModBlocks.POLISHED_MARS_STONE);
                entries.add(GallifreyModBlocks.MARS_POLISHED_ANDESITE);
                entries.add(GallifreyModBlocks.MARS_POLISHED_DIORITE);
                entries.add(GallifreyModBlocks.MARS_POLISHED_GRANITE);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS);
                entries.add(GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED);
                entries.add(GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS);
                entries.add(GallifreyModBlocks.MARS_IRON_ORE);
                entries.add(GallifreyModBlocks.PISS_CRYSTAL);
                entries.add(GallifreyModItems.MARS_STONE_SWORD);
                entries.add(GallifreyModItems.MARS_STONE_PICKAXE);
                entries.add(GallifreyModItems.MARS_STONE_AXE);
                entries.add(GallifreyModItems.MARS_STONE_SHOVEL);
                entries.add(GallifreyModItems.MARS_STONE_HOE);
            }
    );

    public static final CreativeSection CLASSIC = CreativeSection.of(
            "classic",
            () -> new ItemStack(GallifreyModBlocks.CLASSIC_GRASS),
            entries -> {
                // Natural blocks
                entries.add(GallifreyModBlocks.CLASSIC_GRASS);
                entries.add(GallifreyModBlocks.CLASSIC_DIRT);
                entries.add(GallifreyModBlocks.CLASSIC_STONE);
                entries.add(GallifreyModBlocks.CLASSIC_COBBLE);
                entries.add(GallifreyModBlocks.CLASSIC_MOSSY_COBBLESTONE);
                entries.add(GallifreyModBlocks.CLASSIC_SAND);
                entries.add(GallifreyModBlocks.CLASSIC_GRAVEL);
                entries.add(GallifreyModBlocks.CLASSIC_BEDROCK);
                entries.add(GallifreyModBlocks.CLASSIC_OBSIDIAN);

                // Plants & trees
                entries.add(GallifreyModBlocks.CLASSIC_LOG);
                entries.add(GallifreyModBlocks.CLASSIC_LEAVES);
                entries.add(GallifreyModBlocks.CLASSIC_SAPLING);
                entries.add(GallifreyModBlocks.CLASSIC_RED_FLOWER);
                entries.add(GallifreyModBlocks.CLASSIC_YELLOW_FLOWER);

                // Ores
                entries.add(GallifreyModBlocks.CLASSIC_COAL_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_IRON_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_GOLD_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_REDSTONE_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_LAPIS_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_DIAMOND_ORE);
                entries.add(GallifreyModBlocks.CLASSIC_EMERALD_ORE);

                // Building blocks
                entries.add(GallifreyModBlocks.CLASSIC_PLANKS);
                entries.add(GallifreyModBlocks.CLASSIC_STAIRS);
                entries.add(GallifreyModBlocks.CLASSIC_SLAB);
                entries.add(GallifreyModBlocks.CLASSIC_FENCE);
                entries.add(GallifreyModBlocks.CLASSIC_FENCE_GATE);
                entries.add(GallifreyModBlocks.CLASSIC_BRICKS);
                entries.add(GallifreyModBlocks.CLASSIC_GLASS);
                entries.add(GallifreyModBlocks.CLASSIC_GLASS_PANE);
                entries.add(GallifreyModBlocks.CLASSIC_BOOKSHELF);
                entries.add(GallifreyModBlocks.CLASSIC_SPONGE);
                entries.add(GallifreyModBlocks.CLASSIC_WOOL_WINDOWS);

                // Mineral blocks
                entries.add(GallifreyModBlocks.CLASSIC_IRON);
                entries.add(GallifreyModBlocks.CLASSIC_GOLD);
                entries.add(GallifreyModBlocks.CLASSIC_DIAMOND_BLOCK);
                entries.add(GallifreyModBlocks.CLASSIC_EMERALD_BLOCK);
                entries.add(GallifreyModBlocks.CLASSIC_LAPIS_BLOCK);
                entries.add(GallifreyModBlocks.CLASSIC_REDSTONE_BLOCK);
                entries.add(GallifreyModBlocks.CLASSIC_COAL_BLOCK);

                // Utility blocks
                entries.add(GallifreyModBlocks.CLASSIC_REDSTONE_LAMP);
                entries.add(GallifreyModBlocks.CLASSIC_WOODEN_BUTTON);
                entries.add(GallifreyModBlocks.CLASSIC_STONE_BUTTON);
                entries.add(GallifreyModBlocks.CLASSIC_WOODEN_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.CLASSIC_STONE_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.CLASSIC_CRAFTING_TABLE);
                entries.add(GallifreyModBlocks.CLASSIC_FURNACE);
                entries.add(GallifreyModBlocks.CLASSIC_TNT);
                entries.add(GallifreyModItems.DIAMOND_MINECART);

                // Classic Nether
                entries.add(GallifreyModBlocks.CLASSIC_NETHER_NETHERRACK);
                entries.add(GallifreyModBlocks.CLASSIC_NETHER_SOUL_SAND);
                entries.add(GallifreyModBlocks.CLASSIC_NETHER_GLOWSTONE);
                entries.add(GallifreyModBlocks.CLASSIC_NETHER_MAGMA);
                entries.add(GallifreyModBlocks.CLASSIC_NETHER_QUARTZ_ORE);

                // Buckets
                entries.add(GallifreyModItems.CLASSIC_WATER_BUCKET);
                entries.add(GallifreyModItems.CLASSIC_LAVA_BUCKET);

                // Materials
                entries.add(GallifreyModItems.CLASSIC_STICK);
                entries.add(GallifreyModItems.CLASSIC_COAL);
                entries.add(GallifreyModItems.CLASSIC_IRON_INGOT);
                entries.add(GallifreyModItems.CLASSIC_GOLD_INGOT);
                entries.add(GallifreyModItems.CLASSIC_GOLD_NUGGET);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND);
                entries.add(GallifreyModItems.CLASSIC_EMERALD);
                entries.add(GallifreyModItems.CLASSIC_FLINT);
                entries.add(GallifreyModItems.CLASSIC_LEATHER);

                // Food
                entries.add(GallifreyModItems.CLASSIC_APPLE);
                entries.add(GallifreyModItems.CLASSIC_APPLE_GOLDEN);
                entries.add(GallifreyModItems.CLASSIC_CARROT_GOLDEN);

                // Utility items
                entries.add(GallifreyModItems.CLASSIC_FLINT_AND_STEEL);
                entries.add(GallifreyModItems.CLASSIC_OAK_BOAT);
                entries.add(GallifreyModItems.CLASSIC_VILLAGER_SPAWN_EGG);

                // Wood tools
                entries.add(GallifreyModItems.CLASSIC_WOOD_SWORD);
                entries.add(GallifreyModItems.CLASSIC_WOOD_SHOVEL);
                entries.add(GallifreyModItems.CLASSIC_WOOD_PICKAXE);
                entries.add(GallifreyModItems.CLASSIC_WOOD_AXE);
                entries.add(GallifreyModItems.CLASSIC_WOOD_HOE);

                // Iron tools
                entries.add(GallifreyModItems.CLASSIC_IRON_SWORD);
                entries.add(GallifreyModItems.CLASSIC_IRON_SHOVEL);
                entries.add(GallifreyModItems.CLASSIC_IRON_PICKAXE);
                entries.add(GallifreyModItems.CLASSIC_IRON_AXE);
                entries.add(GallifreyModItems.CLASSIC_IRON_HOE);

                // Gold tools
                entries.add(GallifreyModItems.CLASSIC_GOLD_SWORD);
                entries.add(GallifreyModItems.CLASSIC_GOLD_SHOVEL);
                entries.add(GallifreyModItems.CLASSIC_GOLD_PICKAXE);
                entries.add(GallifreyModItems.CLASSIC_GOLD_AXE);
                entries.add(GallifreyModItems.CLASSIC_GOLD_HOE);

                // Diamond tools
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_SWORD);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_SHOVEL);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_PICKAXE);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_AXE);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_HOE);

                // Leather armour
                entries.add(GallifreyModItems.CLASSIC_LEATHER_HELMET);
                entries.add(GallifreyModItems.CLASSIC_LEATHER_CHESTPLATE);
                entries.add(GallifreyModItems.CLASSIC_LEATHER_LEGGINGS);
                entries.add(GallifreyModItems.CLASSIC_LEATHER_BOOTS);

                // Chainmail armour
                entries.add(GallifreyModItems.CLASSIC_CHAINMAIL_HELMET);
                entries.add(GallifreyModItems.CLASSIC_CHAINMAIL_CHESTPLATE);
                entries.add(GallifreyModItems.CLASSIC_CHAINMAIL_LEGGINGS);
                entries.add(GallifreyModItems.CLASSIC_CHAINMAIL_BOOTS);

                // Iron armour
                entries.add(GallifreyModItems.CLASSIC_IRON_HELMET);
                entries.add(GallifreyModItems.CLASSIC_IRON_CHESTPLATE);
                entries.add(GallifreyModItems.CLASSIC_IRON_LEGGINGS);
                entries.add(GallifreyModItems.CLASSIC_IRON_BOOTS);

                // Gold armour
                entries.add(GallifreyModItems.CLASSIC_GOLD_HELMET);
                entries.add(GallifreyModItems.CLASSIC_GOLD_CHESTPLATE);
                entries.add(GallifreyModItems.CLASSIC_GOLD_LEGGINGS);
                entries.add(GallifreyModItems.CLASSIC_GOLD_BOOTS);

                // Diamond armour
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_HELMET);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_CHESTPLATE);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_LEGGINGS);
                entries.add(GallifreyModItems.CLASSIC_DIAMOND_BOOTS);
            });

    public static final CreativeSection SKARO = CreativeSection.of(
            "skaro",
            () -> new ItemStack(GallifreyModBlocks.WASTED_GRASS),
            entries -> {
                entries.add(GallifreyModBlocks.WASTED_DIRT);
                entries.add(GallifreyModBlocks.WASTED_GRASS);
                // Wasted wood set
                entries.add(GallifreyModBlocks.WASTED_SAPLING);
                entries.add(GallifreyModBlocks.WASTED_LEAVES);
                entries.add(GallifreyModBlocks.WASTED_LOG);
                entries.add(GallifreyModBlocks.WASTED_WOOD);
                entries.add(GallifreyModBlocks.WASTED_PLANKS);
                entries.add(GallifreyModBlocks.WASTED_STAIRS);
                entries.add(GallifreyModBlocks.WASTED_SLAB);
                entries.add(GallifreyModBlocks.WASTED_FENCE);
                entries.add(GallifreyModBlocks.WASTED_FENCE_GATE);
                entries.add(GallifreyModBlocks.WASTED_DOOR);
                entries.add(GallifreyModBlocks.WASTED_TRAPDOOR);
                entries.add(GallifreyModBlocks.WASTED_PRESSURE_PLATE);
                entries.add(GallifreyModBlocks.WASTED_BUTTON);
                entries.add(GallifreyModBlocks.SKARO_STONE);
                entries.add(GallifreyModBlocks.SKARO_COBBLESTONE);
                entries.add(GallifreyModBlocks.SKARO_ANDESITE);
                entries.add(GallifreyModBlocks.SKARO_DIORITE);
                entries.add(GallifreyModBlocks.SKARO_GRANITE);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_ANDESITE);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_DIORITE);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_GRANITE);
                entries.add(GallifreyModBlocks.POLISHED_SKARO_STONE);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE);
                entries.add(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_TILES);
                entries.add(GallifreyModBlocks.SKARO_STONE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_STONE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_COBBLESTONE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_COBBLESTONE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_ANDESITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_ANDESITE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_DIORITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_DIORITE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_GRANITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_GRANITE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_DIORITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_GRANITE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB);
                entries.add(GallifreyModBlocks.POLISHED_SKARO_STONE_STAIRS);
                entries.add(GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_STAIRS);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB);
                entries.add(GallifreyModBlocks.SKARO_STONE_WALL);
                entries.add(GallifreyModBlocks.SKARO_COBBLESTONE_WALL);
                entries.add(GallifreyModBlocks.SKARO_ANDESITE_WALL);
                entries.add(GallifreyModBlocks.SKARO_DIORITE_WALL);
                entries.add(GallifreyModBlocks.SKARO_GRANITE_WALL);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL);
                entries.add(GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL);
                entries.add(GallifreyModBlocks.POLISHED_SKARO_STONE_WALL);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_WALL);
                entries.add(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL);
                entries.add(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL);
                entries.add(GallifreyModBlocks.KALETITE_BRICKS);
                entries.add(GallifreyModBlocks.DALEKANIUM_BLOCK);
                entries.add(GallifreyModBlocks.DALEKANIUM_ORE);
                entries.add(GallifreyModBlocks.DEEPSLATE_DALEKANIUM_ORE);
                entries.add(GallifreyModItems.DALEKANIUM_INGOT);
                entries.add(GallifreyModItems.LIQUID_DALEKANIUM);
                entries.add(GallifreyModItems.SKARO_STONE_SWORD);
                entries.add(GallifreyModItems.SKARO_STONE_PICKAXE);
                entries.add(GallifreyModItems.SKARO_STONE_AXE);
                entries.add(GallifreyModItems.SKARO_STONE_SHOVEL);
                entries.add(GallifreyModItems.SKARO_STONE_HOE);
                entries.add(GallifreyModItems.DALEKANIUM_SWORD);
                entries.add(GallifreyModItems.DALEKANIUM_PICKAXE);
                entries.add(GallifreyModItems.DALEKANIUM_AXE);
                entries.add(GallifreyModItems.DALEKANIUM_SHOVEL);
                entries.add(GallifreyModItems.DALEKANIUM_HOE);
                if (com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.DALEKANIUM_KNIFE != null) entries.add(com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.DALEKANIUM_KNIFE); // Farmer's Delight only
                entries.add(GallifreyModItems.DALEKANIUM_HELMET);
                entries.add(GallifreyModItems.DALEKANIUM_CHESTPLATE);
                entries.add(GallifreyModItems.DALEKANIUM_LEGGINGS);
                entries.add(GallifreyModItems.DALEKANIUM_BOOTS);
                // Metalertanium
                entries.add(GallifreyModItems.LIQUID_METALERTANIUM);
                entries.add(GallifreyModItems.METALERTANIUM_INGOT);
                entries.add(GallifreyModBlocks.METALERTANIUM_BLOCK);
                entries.add(GallifreyModItems.METALERTANIUM_SWORD);
                entries.add(GallifreyModItems.METALERTANIUM_PICKAXE);
                entries.add(GallifreyModItems.METALERTANIUM_AXE);
                entries.add(GallifreyModItems.METALERTANIUM_SHOVEL);
                entries.add(GallifreyModItems.METALERTANIUM_HOE);
                if (com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.METALERTANIUM_KNIFE != null) entries.add(com.cerulixteam.gallifrey.compat.farmersdelight.FarmersDelightCompat.METALERTANIUM_KNIFE); // Farmer's Delight only
                entries.add(GallifreyModItems.METALERTANIUM_HELMET);
                entries.add(GallifreyModItems.METALERTANIUM_CHESTPLATE);
                entries.add(GallifreyModItems.METALERTANIUM_LEGGINGS);
                entries.add(GallifreyModItems.METALERTANIUM_BOOTS);
                entries.add(GallifreyModItems.RADIATION_BUCKET);
            }
    );

    /** Sidebar order. */
    public static final List<CreativeSection> ALL = List.of(
            WOOD_TYPES,
            PREHISTORIC,
            MISC,
            ROUNDELS,
            MARS,
            CLASSIC,
            SKARO
    );

    private GallifreyTabSections() {}
}
