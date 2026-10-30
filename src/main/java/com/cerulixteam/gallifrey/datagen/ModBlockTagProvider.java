package com.cerulixteam.gallifrey.datagen;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        // Classic buttons and pressure plates: the vanilla tags make them behave (and mine) like
        // the vanilla ones. Wooden ones are axe-mineable through these tags already.
        getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add(GallifreyModBlocks.CLASSIC_WOODEN_BUTTON);
        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(GallifreyModBlocks.CLASSIC_WOODEN_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.STONE_BUTTONS).add(GallifreyModBlocks.CLASSIC_STONE_BUTTON);
        getOrCreateTagBuilder(BlockTags.STONE_PRESSURE_PLATES).add(GallifreyModBlocks.CLASSIC_STONE_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_STONE_BUTTON, GallifreyModBlocks.CLASSIC_STONE_PRESSURE_PLATE);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_LOG, GallifreyModBlocks.CLASSIC_PLANKS, GallifreyModBlocks.CLASSIC_STAIRS, GallifreyModBlocks.CLASSIC_SLAB, GallifreyModBlocks.CLASSIC_FENCE, GallifreyModBlocks.CLASSIC_FENCE_GATE);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
                .add(GallifreyModBlocks.CLASSIC_LOG);
        getOrCreateTagBuilder(BlockTags.LEAVES)
                .add(GallifreyModBlocks.CLASSIC_LEAVES);
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(GallifreyModBlocks.GALLIFREYAN_WORKBENCH);
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_GRASS, GallifreyModBlocks.CLASSIC_DIRT, GallifreyModBlocks.CLASSIC_SAND, GallifreyModBlocks.CLASSIC_GRAVEL);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_STONE, GallifreyModBlocks.CLASSIC_COBBLE, GallifreyModBlocks.CLASSIC_GOLD,
                        GallifreyModBlocks.CLASSIC_IRON, GallifreyModBlocks.CLASSIC_BRICKS, GallifreyModBlocks.CLASSIC_SPONGE,
                        GallifreyModBlocks.CLASSIC_GLASS, GallifreyModBlocks.CLASSIC_TNT);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(GallifreyModBlocks.ULANDA_LOG, GallifreyModBlocks.ULANDA_WOOD,
                        GallifreyModBlocks.STRIP_ULANDA_LOG, GallifreyModBlocks.STRIP_ULANDA_WOOD)
                .add(GallifreyModBlocks.TREEBORG_LOG, GallifreyModBlocks.TREEBORG_WOOD,
                        GallifreyModBlocks.STRIP_TREEBORG_LOG, GallifreyModBlocks.STRIP_TREEBORG_WOOD)
                .add(GallifreyModBlocks.TARDIS_LOG, GallifreyModBlocks.TARDIS_WOOD,
                        GallifreyModBlocks.STRIP_TARDIS_LOG, GallifreyModBlocks.STRIP_TARDIS_WOOD)
                .add(GallifreyModBlocks.ASH_LOG, GallifreyModBlocks.ASH_WOOD,
                        GallifreyModBlocks.STRIP_ASH_LOG, GallifreyModBlocks.STRIP_ASH_WOOD)
                .add(GallifreyModBlocks.MAPLE_LOG, GallifreyModBlocks.MAPLE_WOOD,
                        GallifreyModBlocks.STRIP_MAPLE_LOG, GallifreyModBlocks.STRIP_MAPLE_WOOD)
                .add(GallifreyModBlocks.WASTED_DIRT,
                        GallifreyModBlocks.WASTED_GRASS, GallifreyModBlocks.WASTED_LEAVES,
                        GallifreyModBlocks.WASTED_LOG, GallifreyModBlocks.WASTED_PLANKS,
                        GallifreyModBlocks.WASTED_SLAB)

        .add(GallifreyModBlocks.MOONPINE_LOG, GallifreyModBlocks.MOONPINE_WOOD,
                GallifreyModBlocks.STRIP_MOONPINE_LOG, GallifreyModBlocks.STRIP_MOONPINE_WOOD)
        .add(GallifreyModBlocks.WASTED_WOOD,
                GallifreyModBlocks.PREHISTORIC_LOG,
                GallifreyModBlocks.PREHISTORIC_PLANKS);

        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(GallifreyModBlocks.MARS_SAND, GallifreyModBlocks.LOST_DIRT);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.MARS_SANDSTONE,
                        GallifreyModBlocks.MARS_STONE, GallifreyModBlocks.MARS_COBBLESTONE,
                        GallifreyModBlocks.MARS_ANDESITE, GallifreyModBlocks.MARS_DIORITE,
                        GallifreyModBlocks.MARS_GRANITE, GallifreyModBlocks.POLISHED_MARS_STONE,
                        GallifreyModBlocks.MARS_POLISHED_ANDESITE, GallifreyModBlocks.MARS_POLISHED_DIORITE,
                        GallifreyModBlocks.MARS_POLISHED_GRANITE, GallifreyModBlocks.MARS_STONE_BRICKS,
                        GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED, GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS,
                        GallifreyModBlocks.MARS_IRON_ORE, GallifreyModBlocks.PISS_CRYSTAL)
                .add(GallifreyModBlocks.DALEKANIUM_BLOCK, GallifreyModBlocks.DALEKANIUM_ORE,
                        GallifreyModBlocks.DEEPSLATE_DALEKANIUM_ORE,
                        GallifreyModBlocks.EXQUISITE_CAT, GallifreyModBlocks.GOOD_HEAVENS,
                        GallifreyModBlocks.SKARO_STONE, GallifreyModBlocks.SKARO_COBBLESTONE,
                        GallifreyModBlocks.SKARO_ANDESITE, GallifreyModBlocks.SKARO_DIORITE,
                        GallifreyModBlocks.SKARO_GRANITE, GallifreyModBlocks.SKARO_POLISHED_ANDESITE,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE, GallifreyModBlocks.SKARO_POLISHED_GRANITE,
                        GallifreyModBlocks.POLISHED_SKARO_STONE, GallifreyModBlocks.SKARO_DEEPSLATE,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE, GallifreyModBlocks.SKARO_DEEPSLATE_TILES,
                        GallifreyModBlocks.SKARO_STONE_STAIRS, GallifreyModBlocks.SKARO_STONE_SLAB,
                        GallifreyModBlocks.SKARO_COBBLESTONE_STAIRS, GallifreyModBlocks.SKARO_COBBLESTONE_SLAB,
                        GallifreyModBlocks.SKARO_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_ANDESITE_SLAB,
                        GallifreyModBlocks.SKARO_DIORITE_STAIRS, GallifreyModBlocks.SKARO_DIORITE_SLAB,
                        GallifreyModBlocks.SKARO_GRANITE_STAIRS, GallifreyModBlocks.SKARO_GRANITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_GRANITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB,
                        GallifreyModBlocks.POLISHED_SKARO_STONE_STAIRS, GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB,
                        GallifreyModBlocks.SKARO_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_SLAB,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB,
                        GallifreyModBlocks.SKARO_DEEPSLATE_TILES_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB,
                        GallifreyModBlocks.KALETITE_BRICKS)
                .add(GallifreyModBlocks.SONIC_CRYSTAL_ORE, GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE,
                        GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE)
                .add(GallifreyModBlocks.CLASSIC_COAL_ORE,   GallifreyModBlocks.CLASSIC_IRON_ORE, GallifreyModBlocks.CLASSIC_DIAMOND_ORE, GallifreyModBlocks.CLASSIC_GOLD_ORE, GallifreyModBlocks.CLASSIC_LAPIS_ORE, GallifreyModBlocks.CLASSIC_REDSTONE_ORE, GallifreyModBlocks.CLASSIC_NETHER_NETHERRACK, GallifreyModBlocks.CLASSIC_NETHER_QUARTZ_ORE, GallifreyModBlocks.CLASSIC_STONE, GallifreyModBlocks.CLASSIC_COBBLE, GallifreyModBlocks.CLASSIC_GOLD, GallifreyModBlocks.CLASSIC_IRON, GallifreyModBlocks.CLASSIC_BRICKS)
                .add(GallifreyModBlocks.WHITE_POINT_ORE, GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE,
                        GallifreyModBlocks.NETHER_WHITE_POINT_ORE);

        // Mars blocks and ordinary Skaro stone blocks can be harvested with a stone pickaxe.
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GallifreyModBlocks.MARS_SANDSTONE,
                        GallifreyModBlocks.MARS_STONE, GallifreyModBlocks.MARS_COBBLESTONE,
                        GallifreyModBlocks.MARS_ANDESITE, GallifreyModBlocks.MARS_DIORITE,
                        GallifreyModBlocks.MARS_GRANITE, GallifreyModBlocks.POLISHED_MARS_STONE,
                        GallifreyModBlocks.MARS_POLISHED_ANDESITE, GallifreyModBlocks.MARS_POLISHED_DIORITE,
                        GallifreyModBlocks.MARS_POLISHED_GRANITE, GallifreyModBlocks.MARS_STONE_BRICKS,
                        GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED, GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS,
                        GallifreyModBlocks.MARS_IRON_ORE, GallifreyModBlocks.PISS_CRYSTAL,
                        GallifreyModBlocks.EXQUISITE_CAT, GallifreyModBlocks.GOOD_HEAVENS,
                        GallifreyModBlocks.SKARO_STONE, GallifreyModBlocks.SKARO_COBBLESTONE,
                        GallifreyModBlocks.SKARO_ANDESITE, GallifreyModBlocks.SKARO_DIORITE,
                        GallifreyModBlocks.SKARO_GRANITE, GallifreyModBlocks.SKARO_POLISHED_ANDESITE,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE, GallifreyModBlocks.SKARO_POLISHED_GRANITE,
                        GallifreyModBlocks.POLISHED_SKARO_STONE, GallifreyModBlocks.SKARO_DEEPSLATE,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE, GallifreyModBlocks.SKARO_DEEPSLATE_TILES,
                        GallifreyModBlocks.SKARO_STONE_STAIRS, GallifreyModBlocks.SKARO_STONE_SLAB,
                        GallifreyModBlocks.SKARO_COBBLESTONE_STAIRS, GallifreyModBlocks.SKARO_COBBLESTONE_SLAB,
                        GallifreyModBlocks.SKARO_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_ANDESITE_SLAB,
                        GallifreyModBlocks.SKARO_DIORITE_STAIRS, GallifreyModBlocks.SKARO_DIORITE_SLAB,
                        GallifreyModBlocks.SKARO_GRANITE_STAIRS, GallifreyModBlocks.SKARO_GRANITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB,
                        GallifreyModBlocks.SKARO_POLISHED_GRANITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB,
                        GallifreyModBlocks.POLISHED_SKARO_STONE_STAIRS, GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB,
                        GallifreyModBlocks.SKARO_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_SLAB,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB,
                        GallifreyModBlocks.SKARO_DEEPSLATE_TILES_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB,
                        GallifreyModBlocks.KALETITE_BRICKS, GallifreyModBlocks.HARTNELL_WALL);

        // Sonic crystal and Dalekanium ores require an iron pickaxe.
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(GallifreyModBlocks.SONIC_CRYSTAL_ORE, GallifreyModBlocks.NETHER_SONIC_CRYSTAL_ORE,
                        GallifreyModBlocks.DEEPSLATE_SONIC_CRYSTAL_ORE,
                        GallifreyModBlocks.DALEKANIUM_ORE, GallifreyModBlocks.DEEPSLATE_DALEKANIUM_ORE,
                        GallifreyModBlocks.DALEKANIUM_BLOCK);

        // White Point Star ores require a diamond pickaxe.
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(GallifreyModBlocks.REINFORCED_STEEL_BLOCK,
                        GallifreyModBlocks.WHITE_POINT_ORE, GallifreyModBlocks.DEEPSLATE_WHITE_POINT_ORE,
                        GallifreyModBlocks.NETHER_WHITE_POINT_ORE,
                        GallifreyModBlocks.PREHISTORIC_ORE, GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.PREHISTORIC_ORE, GallifreyModBlocks.DEEPSLATE_PREHISTORIC_ORE);

        // Steel grates and the Metalertanium block.
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.GRATE_BLOCK, GallifreyModBlocks.THICK_GRATE_BLOCK,
                        GallifreyModBlocks.RUSTY_GRATE_BLOCK, GallifreyModBlocks.RUSTY_THICK_GRATE_BLOCK,
                        GallifreyModBlocks.METALERTANIUM_BLOCK);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GallifreyModBlocks.GRATE_BLOCK, GallifreyModBlocks.THICK_GRATE_BLOCK,
                        GallifreyModBlocks.RUSTY_GRATE_BLOCK, GallifreyModBlocks.RUSTY_THICK_GRATE_BLOCK);
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(GallifreyModBlocks.METALERTANIUM_BLOCK);

        // Roundels: mined with the same tool as the vanilla block each one is made from.
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(
                        GallifreyModBlocks.STRIPPED_ACACIA_LOG_ROUNDEL, GallifreyModBlocks.STRIPPED_BIRCH_LOG_ROUNDEL,
                        GallifreyModBlocks.STRIPPED_CHERRY_LOG_ROUNDEL, GallifreyModBlocks.STRIPPED_DARK_OAK_LOG_ROUNDEL,
                        GallifreyModBlocks.STRIPPED_JUNGLE_LOG_ROUNDEL, GallifreyModBlocks.STRIPPED_MANGROVE_LOG_ROUNDEL,
                        GallifreyModBlocks.STRIPPED_OAK_LOG_ROUNDEL, GallifreyModBlocks.STRIPPED_SPRUCE_LOG_ROUNDEL);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(
                        GallifreyModBlocks.BASALT_ROUNDEL, GallifreyModBlocks.BONE_ROUNDEL,
                        GallifreyModBlocks.VANILLA_QUATZ_ROUNDEL,
                        GallifreyModBlocks.STRUCTURE_ROUNDEL, GallifreyModBlocks.LODESTONE_ROUNDEL,
                        GallifreyModBlocks.QUARTZ_ROUNDEL, GallifreyModBlocks.AMBQUARTZ_ROUNDEL,
                        GallifreyModBlocks.BLUEQUARTZ_ROUNDEL, GallifreyModBlocks.BLACK_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.BLUE_CONCRETE_ROUNDEL, GallifreyModBlocks.BROWN_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.COPPER_ROUNDEL, GallifreyModBlocks.CRIMQUARTZ_ROUNDEL,
                        GallifreyModBlocks.OBSIQUARTZ_ROUNDEL, GallifreyModBlocks.VERDQUARTZ_ROUNDEL,
                        GallifreyModBlocks.VIOQUARTZ_ROUNDEL, GallifreyModBlocks.CYQUARTZ_ROUNDEL,
                        GallifreyModBlocks.END_STONE_BRICKS_ROUNDEL, GallifreyModBlocks.EXPOSED_COPPER_ROUNDEL,
                        GallifreyModBlocks.GRAY_CONCRETE_ROUNDEL, GallifreyModBlocks.GREEN_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.LIGHT_BLUE_CONCRETE_ROUNDEL, GallifreyModBlocks.LIGHT_GRAY_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.LIME_CONCRETE_ROUNDEL, GallifreyModBlocks.MAGENTA_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.ORANGE_CONCRETE_ROUNDEL, GallifreyModBlocks.OXIDIZED_COPPER_ROUNDEL,
                        GallifreyModBlocks.POLISHED_DIORITE_ROUNDEL, GallifreyModBlocks.POLISHED_ANDESITE_ROUNDEL,
                        GallifreyModBlocks.POLISHED_DEEPSLATE_ROUNDEL, GallifreyModBlocks.PINK_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.POLISHED_GRANITE_ROUNDEL, GallifreyModBlocks.PURPLE_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.RED_CONCRETE_ROUNDEL, GallifreyModBlocks.SANDSTONE_ROUNDEL,
                        GallifreyModBlocks.WEATHERED_COPPER_ROUNDEL, GallifreyModBlocks.WHITE_CONCRETE_ROUNDEL,
                        GallifreyModBlocks.YELLOW_CONCRETE_ROUNDEL, GallifreyModBlocks.HARTNELL_ROUNDEL,
                        GallifreyModBlocks.HARTNELL_WALL);

        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(
                        GallifreyModBlocks.DIRT_ROUNDEL);

        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE)
                .add(
                        GallifreyModBlocks.MOSS_ROUNDEL);

        getOrCreateTagBuilder(BlockTags.FENCES)
                .add(GallifreyModBlocks.TARDIS_FENCE, GallifreyModBlocks.TREEBORG_FENCE,
                        GallifreyModBlocks.ASH_FENCE, GallifreyModBlocks.MAPLE_FENCE,
                        GallifreyModBlocks.MOONPINE_FENCE, GallifreyModBlocks.ULANDA_FENCE,
                        GallifreyModBlocks.WASTED_FENCE, GallifreyModBlocks.PREHISTORIC_FENCE);

        getOrCreateTagBuilder(BlockTags.FENCE_GATES)
                .add(GallifreyModBlocks.TARDIS_FENCE_GATE, GallifreyModBlocks.TREEBORG_FENCE_GATE,
                        GallifreyModBlocks.ASH_FENCE_GATE, GallifreyModBlocks.MAPLE_FENCE_GATE,
                        GallifreyModBlocks.MOONPINE_FENCE_GATE,GallifreyModBlocks.ULANDA_FENCE_GATE,
                        GallifreyModBlocks.WASTED_FENCE_GATE, GallifreyModBlocks.PREHISTORIC_FENCE_GATE);

        // Walls only connect to each other (and look right) when they are in this tag.
        getOrCreateTagBuilder(BlockTags.WALLS)
                .add(GallifreyModBlocks.HARTNELL_WALL)
                .add(GallifreyModBlocks.SKARO_STONE_WALL, GallifreyModBlocks.SKARO_COBBLESTONE_WALL,
                        GallifreyModBlocks.SKARO_ANDESITE_WALL, GallifreyModBlocks.SKARO_DIORITE_WALL,
                        GallifreyModBlocks.SKARO_GRANITE_WALL, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL, GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL,
                        GallifreyModBlocks.POLISHED_SKARO_STONE_WALL, GallifreyModBlocks.SKARO_DEEPSLATE_WALL,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL);

        // Skaro walls are mined with a pickaxe, like the rest of the Skaro stone set.
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.SKARO_STONE_WALL, GallifreyModBlocks.SKARO_COBBLESTONE_WALL,
                        GallifreyModBlocks.SKARO_ANDESITE_WALL, GallifreyModBlocks.SKARO_DIORITE_WALL,
                        GallifreyModBlocks.SKARO_GRANITE_WALL, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL,
                        GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL, GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL,
                        GallifreyModBlocks.POLISHED_SKARO_STONE_WALL, GallifreyModBlocks.SKARO_DEEPSLATE_WALL,
                        GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL);

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GallifreyModBlocks.SKARO_STONE_WALL, GallifreyModBlocks.SKARO_COBBLESTONE_WALL);

        // Logs tag: leaves only stay alive next to blocks in this tag,
        // so every tree's logs must be here or its leaves decay.
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
                .add(GallifreyModBlocks.ULANDA_LOG, GallifreyModBlocks.ULANDA_WOOD,
                        GallifreyModBlocks.STRIP_ULANDA_LOG, GallifreyModBlocks.STRIP_ULANDA_WOOD)
                .add(GallifreyModBlocks.TREEBORG_LOG, GallifreyModBlocks.TREEBORG_WOOD,
                        GallifreyModBlocks.STRIP_TREEBORG_LOG, GallifreyModBlocks.STRIP_TREEBORG_WOOD)
                .add(GallifreyModBlocks.TARDIS_LOG, GallifreyModBlocks.TARDIS_WOOD,
                        GallifreyModBlocks.STRIP_TARDIS_LOG, GallifreyModBlocks.STRIP_TARDIS_WOOD)
                .add(GallifreyModBlocks.ASH_LOG, GallifreyModBlocks.ASH_WOOD,
                        GallifreyModBlocks.STRIP_ASH_LOG, GallifreyModBlocks.STRIP_ASH_WOOD)
                .add(GallifreyModBlocks.MAPLE_LOG, GallifreyModBlocks.MAPLE_WOOD,
                        GallifreyModBlocks.STRIP_MAPLE_LOG, GallifreyModBlocks.STRIP_MAPLE_WOOD)
                .add(GallifreyModBlocks.MOONPINE_LOG, GallifreyModBlocks.MOONPINE_WOOD,
                        GallifreyModBlocks.STRIP_MOONPINE_LOG, GallifreyModBlocks.STRIP_MOONPINE_WOOD)
                .add(GallifreyModBlocks.WASTED_LOG, GallifreyModBlocks.WASTED_WOOD,
                GallifreyModBlocks.PREHISTORIC_LOG);

        // CLASSIC BLOCK TAGS
        // Keep Classic trees compatible with vanilla leaf-decay rules and tool tags.
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
                .add(GallifreyModBlocks.CLASSIC_LOG);

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_STONE, GallifreyModBlocks.CLASSIC_COBBLE,
                        GallifreyModBlocks.CLASSIC_GOLD, GallifreyModBlocks.CLASSIC_IRON,
                        GallifreyModBlocks.CLASSIC_GLASS, GallifreyModBlocks.CLASSIC_SPONGE,
                        GallifreyModBlocks.CLASSIC_BRICKS, GallifreyModBlocks.CLASSIC_TNT);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_PLANKS, GallifreyModBlocks.CLASSIC_LOG,
                        GallifreyModBlocks.CLASSIC_LEAVES);

        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_DIRT, GallifreyModBlocks.CLASSIC_GRASS,
                        GallifreyModBlocks.CLASSIC_SAND, GallifreyModBlocks.CLASSIC_GRAVEL);

        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_LEAVES, GallifreyModBlocks.CLASSIC_GRASS);

        // Classic blocks that copy a vanilla block which needs the right tool to drop.
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_FURNACE, GallifreyModBlocks.CLASSIC_OBSIDIAN,
                        GallifreyModBlocks.CLASSIC_MOSSY_COBBLESTONE, GallifreyModBlocks.CLASSIC_DIAMOND_BLOCK,
                        GallifreyModBlocks.CLASSIC_EMERALD_BLOCK, GallifreyModBlocks.CLASSIC_LAPIS_BLOCK,
                        GallifreyModBlocks.CLASSIC_REDSTONE_BLOCK, GallifreyModBlocks.CLASSIC_EMERALD_ORE,
                        GallifreyModBlocks.CLASSIC_NETHER_GLOWSTONE, GallifreyModBlocks.CLASSIC_NETHER_MAGMA);
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_CRAFTING_TABLE, GallifreyModBlocks.CLASSIC_BOOKSHELF);
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE)
                .add(GallifreyModBlocks.CLASSIC_NETHER_SOUL_SAND);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GallifreyModBlocks.CLASSIC_IRON, GallifreyModBlocks.CLASSIC_LAPIS_BLOCK);
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(GallifreyModBlocks.CLASSIC_GOLD, GallifreyModBlocks.CLASSIC_DIAMOND_BLOCK,
                        GallifreyModBlocks.CLASSIC_EMERALD_BLOCK, GallifreyModBlocks.CLASSIC_EMERALD_ORE);
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(GallifreyModBlocks.CLASSIC_OBSIDIAN);

        // ------------------------------------------------------------------
        // Entries that used to live in hand-written tag files under
        // src/main/resources/data/minecraft/tags/blocks/. Those files had the
        // same names as the generated ones, so only one of each pair was ever
        // loaded. Everything is here now; do not re-create those files.
        // ------------------------------------------------------------------
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .addOptionalTag(new Identifier("gallifrey", "prehistoric_ores"))
                .addOptionalTag(new Identifier("gallifrey", "prehistoric_blocks"))
                .add(GallifreyModBlocks.ATRIUM_ORE, GallifreyModBlocks.DEEPSLATE_ATRIUM_ORE,
                        GallifreyModBlocks.ATRIUM_BLOCK)
                .add(
                        GallifreyModBlocks.MARS_SANDSTONE_STAIRS, GallifreyModBlocks.MARS_SANDSTONE_SLAB,

                        GallifreyModBlocks.MARS_STONE_STAIRS, GallifreyModBlocks.MARS_STONE_SLAB,

                        GallifreyModBlocks.MARS_COBBLESTONE_STAIRS, GallifreyModBlocks.MARS_COBBLESTONE_SLAB,

                        GallifreyModBlocks.MARS_ANDESITE_STAIRS, GallifreyModBlocks.MARS_ANDESITE_SLAB,

                        GallifreyModBlocks.MARS_DIORITE_STAIRS, GallifreyModBlocks.MARS_DIORITE_SLAB,

                        GallifreyModBlocks.MARS_GRANITE_STAIRS, GallifreyModBlocks.MARS_GRANITE_SLAB,

                        GallifreyModBlocks.POLISHED_MARS_STONE_STAIRS, GallifreyModBlocks.POLISHED_MARS_STONE_SLAB,

                        GallifreyModBlocks.MARS_POLISHED_ANDESITE_STAIRS, GallifreyModBlocks.MARS_POLISHED_ANDESITE_SLAB,

                        GallifreyModBlocks.MARS_POLISHED_DIORITE_STAIRS, GallifreyModBlocks.MARS_POLISHED_DIORITE_SLAB,

                        GallifreyModBlocks.MARS_POLISHED_GRANITE_STAIRS, GallifreyModBlocks.MARS_POLISHED_GRANITE_SLAB,

                        GallifreyModBlocks.MARS_STONE_BRICKS_STAIRS, GallifreyModBlocks.MARS_STONE_BRICKS_SLAB,

                        GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED_STAIRS, GallifreyModBlocks.MARS_STONE_BRICKS_CRACKED_SLAB,

                        GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS_STAIRS, GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS_SLAB);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(GallifreyModBlocks.ATRIUM_ORE, GallifreyModBlocks.DEEPSLATE_ATRIUM_ORE,
                        GallifreyModBlocks.ATRIUM_BLOCK);

        // Ruby and sapphire: pickaxe-mineable, iron pickaxe or better to get drops.
        // (These blocks use requiresTool(), so without these tags they mine
        // at 1/100 speed and drop nothing.)
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(GallifreyModBlocks.RUBY_ORE, GallifreyModBlocks.DEEPSLATE_RUBY_ORE,
                        GallifreyModBlocks.RUBY_BLOCK,
                        GallifreyModBlocks.SAPPHIRE_ORE, GallifreyModBlocks.DEEPSLATE_SAPPHIRE_ORE,
                        GallifreyModBlocks.SAPPHIRE_BLOCK);
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(GallifreyModBlocks.RUBY_ORE, GallifreyModBlocks.DEEPSLATE_RUBY_ORE,
                        GallifreyModBlocks.RUBY_BLOCK,
                        GallifreyModBlocks.SAPPHIRE_ORE, GallifreyModBlocks.DEEPSLATE_SAPPHIRE_ORE,
                        GallifreyModBlocks.SAPPHIRE_BLOCK);

        // Classic ore tiers that were only in the (now deleted) hand-written tag files.
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GallifreyModBlocks.CLASSIC_IRON_ORE, GallifreyModBlocks.CLASSIC_LAPIS_ORE,
                        GallifreyModBlocks.CLASSIC_NETHER_QUARTZ_ORE);
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(GallifreyModBlocks.CLASSIC_GOLD_ORE, GallifreyModBlocks.CLASSIC_DIAMOND_ORE,
                        GallifreyModBlocks.CLASSIC_REDSTONE_ORE);
    }
}
