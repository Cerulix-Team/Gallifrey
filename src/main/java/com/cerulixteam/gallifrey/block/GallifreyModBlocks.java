package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.block.custom.SpreadingGrassBlock;
import com.cerulixteam.gallifrey.block.custom.ClassicTntBlock;
import com.cerulixteam.gallifrey.item.custom.SonicWorkshopItem;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.GallifreySounds;
import com.terraformersmc.terraform.sign.block.TerraformWallSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformHangingSignBlock;
import com.cerulixteam.gallifrey.block.custom.SonicSignalBlock;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import com.cerulixteam.gallifrey.block.entity.RoundelBlock;
import com.cerulixteam.gallifrey.world.tree.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import com.cerulixteam.gallifrey.block.custom.SonicWorkshopBlock;
import net.minecraft.util.math.intprovider.UniformIntProvider;


/**
 * All the blocks for the mod get registered here.
 *
 * Adding a new one? do this:
 *  1. Make the block class
 *  2. Register it below
 *  3. Block model json in assets/gallifrey/models/block/
 *  4. Texture in assets/gallifrey/textures/block/
 *  5. Item model json in assets/gallifrey/models/item/
 *  6. Add it to the creative tab
 */
public class GallifreyModBlocks {

    // ============================================================
    // CLASSIC BLOCKS
    // ============================================================

    public static final Block GALLIFREYAN_WORKBENCH = registerBlock(
            "gallifreyan_workbench",
            new com.cerulixteam.gallifrey.block.custom.GallifreyanWorkbenchBlock(
                    FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE).nonOpaque()
            )
    );

    // Classic-era blocks and their custom 2009-era sound groups.
    private static final BlockSoundGroup CLASSIC_STONE_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, GallifreySounds.CLASSIC_STONE_STEP_0, GallifreySounds.CLASSIC_STONE_STEP_1, GallifreySounds.CLASSIC_STONE_STEP_2, GallifreySounds.CLASSIC_STONE_STEP_3, GallifreySounds.CLASSIC_STONE_STEP_3);
    private static final BlockSoundGroup CLASSIC_GRASS_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, GallifreySounds.CLASSIC_GRASS_STEP_0, GallifreySounds.CLASSIC_GRASS_STEP_1, GallifreySounds.CLASSIC_GRASS_STEP_2, GallifreySounds.CLASSIC_GRASS_STEP_3, GallifreySounds.CLASSIC_GRASS_STEP_3);
    private static final BlockSoundGroup CLASSIC_WOOD_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, GallifreySounds.CLASSIC_WOOD_STEP_0, GallifreySounds.CLASSIC_WOOD_STEP_1, GallifreySounds.CLASSIC_WOOD_STEP_2, GallifreySounds.CLASSIC_WOOD_STEP_3, GallifreySounds.CLASSIC_WOOD_STEP_3);
    private static final BlockSoundGroup CLASSIC_GRAVEL_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, GallifreySounds.CLASSIC_GRAVEL_STEP_0, GallifreySounds.CLASSIC_GRAVEL_STEP_1, GallifreySounds.CLASSIC_GRAVEL_STEP_2, GallifreySounds.CLASSIC_GRAVEL_STEP_3, GallifreySounds.CLASSIC_GRAVEL_STEP_3);

    public static final Block CLASSIC_STONE = registerBlock("classic_stone", new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_GRASS = registerBlock("classic_grass", new SpreadingGrassBlock(() -> GallifreyModBlocks.CLASSIC_DIRT, () -> java.util.List.of(GallifreyModBlocks.CLASSIC_RED_FLOWER, GallifreyModBlocks.CLASSIC_YELLOW_FLOWER), FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK).sounds(CLASSIC_GRASS_SOUNDS)));
    public static final Block CLASSIC_DIRT = registerBlock("classic_dirt", new Block(FabricBlockSettings.copyOf(Blocks.DIRT).sounds(CLASSIC_GRASS_SOUNDS)));
    public static final Block CLASSIC_COBBLE = registerBlock("classic_cobble", new Block(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_PLANKS = registerBlock("classic_planks", new Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_STAIRS = registerBlock("classic_stairs", new StairsBlock(CLASSIC_PLANKS.getDefaultState(), FabricBlockSettings.copyOf(Blocks.OAK_WOOD).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_SLAB = registerBlock("classic_slab", new SlabBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_FENCE = registerBlock("classic_fence", new FenceBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_FENCE_GATE = registerBlock("classic_fence_gate", new FenceGateBlock(FabricBlockSettings.copyOf(Blocks.OAK_WOOD).sounds(CLASSIC_WOOD_SOUNDS), WoodType.OAK));
    public static final Block CLASSIC_LOG = registerBlock("classic_log", new PillarBlock(FabricBlockSettings.copyOf(Blocks.OAK_LOG).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_SAPLING = registerBlock("classic_sapling", new SaplingBlock(new ClassicSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.OAK_SAPLING).sounds(CLASSIC_GRASS_SOUNDS)));
    public static final Block CLASSIC_LEAVES = registerBlock("classic_leaves", new LeavesBlock(FabricBlockSettings.copyOf(Blocks.OAK_LEAVES).sounds(CLASSIC_GRASS_SOUNDS)));
    public static final Block CLASSIC_SAND = registerBlock("classic_sand", new FallingBlock(FabricBlockSettings.copyOf(Blocks.SAND).sounds(CLASSIC_GRAVEL_SOUNDS)));
    public static final Block CLASSIC_GRAVEL = registerBlock("classic_gravel", new FallingBlock(FabricBlockSettings.copyOf(Blocks.GRAVEL).sounds(CLASSIC_GRAVEL_SOUNDS)));
    // Classic textures that were present in the asset pack but had no registered block.
    public static final Block CLASSIC_MOSSY_COBBLESTONE = registerBlock("classic_mossy_cobblestone", new Block(FabricBlockSettings.copyOf(Blocks.MOSSY_COBBLESTONE).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_BOOKSHELF = registerBlock("classic_bookshelf", new Block(FabricBlockSettings.copyOf(Blocks.BOOKSHELF).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_CRAFTING_TABLE = registerBlock("classic_crafting_table", new com.cerulixteam.gallifrey.block.custom.ClassicCraftingTableBlock(FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE).sounds(CLASSIC_WOOD_SOUNDS)));
    public static final Block CLASSIC_FURNACE = registerBlock("classic_furnace", new FurnaceBlock(FabricBlockSettings.copyOf(Blocks.FURNACE).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_WOOL_WINDOWS = registerBlock("classic_wool_windows", new Block(FabricBlockSettings.copyOf(Blocks.WHITE_WOOL)));

    public static final Block CLASSIC_GOLD = registerBlock("classic_gold", new Block(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_IRON = registerBlock("classic_iron", new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_GLASS = registerBlock("classic_glass", new GlassBlock(FabricBlockSettings.copyOf(Blocks.GLASS).sounds(BlockSoundGroup.GLASS).nonOpaque()));
    public static final Block CLASSIC_SPONGE = registerBlock("classic_sponge", new Block(FabricBlockSettings.copyOf(Blocks.SPONGE).sounds(CLASSIC_GRASS_SOUNDS)));
    public static final Block CLASSIC_BRICKS = registerBlock("classic_bricks", new Block(FabricBlockSettings.copyOf(Blocks.BRICKS).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_TNT = registerBlock("classic_tnt", new ClassicTntBlock(FabricBlockSettings.copyOf(Blocks.TNT).sounds(BlockSoundGroup.GRASS)));
    public static final Block CLASSIC_RED_FLOWER = registerBlock("classic_red_flower", new FlowerBlock(StatusEffects.SATURATION, 1, FabricBlockSettings.copyOf(Blocks.RED_TULIP)));
    public static final Block CLASSIC_YELLOW_FLOWER = registerBlock("classic_yellow_flower", new FlowerBlock(StatusEffects.SATURATION, 1, FabricBlockSettings.copyOf(Blocks.DANDELION)));
    public static final Block CLASSIC_BEDROCK = registerBlock("classic_bedrock", new Block(FabricBlockSettings.copyOf(Blocks.BEDROCK).strength(-1.0F, 3600000.0F).dropsNothing().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_DIAMOND_BLOCK = registerBlock("classic_diamond_block", new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_EMERALD_BLOCK = registerBlock("classic_emerald_block", new Block(FabricBlockSettings.copyOf(Blocks.EMERALD_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_LAPIS_BLOCK = registerBlock("classic_lapis_block", new Block(FabricBlockSettings.copyOf(Blocks.LAPIS_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_REDSTONE_BLOCK = registerBlock("classic_redstone_block", new Block(FabricBlockSettings.copyOf(Blocks.REDSTONE_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));

    public static final Block CLASSIC_OBSIDIAN = registerBlock("classic_obsidian",
            new Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).sounds(CLASSIC_STONE_SOUNDS)));

    public static final Block CLASSIC_COAL_ORE = registerBlock("classic_coal_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.COAL_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_IRON_ORE = registerBlock("classic_iron_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_GOLD_ORE = registerBlock("classic_gold_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.GOLD_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_REDSTONE_ORE = registerBlock("classic_redstone_ore",
            new Block(FabricBlockSettings.copyOf(CLASSIC_STONE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_LAPIS_ORE = registerBlock("classic_lapis_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.LAPIS_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_DIAMOND_ORE = registerBlock("classic_diamond_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block CLASSIC_EMERALD_ORE = registerBlock("classic_emerald_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.EMERALD_ORE).strength(3.0F, 3.0F).requiresTool().sounds(CLASSIC_STONE_SOUNDS)));
    public static final Block RUBY_BLOCK = registerBlock(
            "ruby_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(3.0F, 3.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block RUBY_ORE = registerBlock(
            "ruby_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.EMERALD_ORE)
                            .strength(3.0F, 3.0F)
                            .requiresTool()
                            .sounds(CLASSIC_STONE_SOUNDS),
                    UniformIntProvider.create(3, 7)));

    public static final Block DEEPSLATE_RUBY_ORE = registerBlock(
            "deepslate_ruby_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.DEEPSLATE_EMERALD_ORE)
                            .strength(4.5F, 3.0F)
                            .requiresTool()
                            .sounds(CLASSIC_STONE_SOUNDS),
                    UniformIntProvider.create(3, 7)));

    public static final Block SAPPHIRE_BLOCK = registerBlock(
            "sapphire_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(3.0F, 3.0F)
                    .requiresTool()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block SAPPHIRE_ORE = registerBlock(
            "sapphire_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.EMERALD_ORE)
                            .strength(3.0F, 3.0F)
                            .requiresTool()
                            .sounds(CLASSIC_STONE_SOUNDS),
                    UniformIntProvider.create(3, 7)));

    public static final Block DEEPSLATE_SAPPHIRE_ORE = registerBlock(
            "deepslate_sapphire_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.DEEPSLATE_EMERALD_ORE)
                            .strength(4.5F, 3.0F)
                            .requiresTool()
                            .sounds(CLASSIC_STONE_SOUNDS),
                    UniformIntProvider.create(3, 7)));

    public static final Block CLASSIC_REDSTONE_LAMP = registerBlock("classic_redstone_lamp",
            new RedstoneLampBlock(FabricBlockSettings.copyOf(Blocks.REDSTONE_LAMP).sounds(BlockSoundGroup.GLASS)));

    // Classic buttons, pressure plates and glass pane. Their blockstates and models come from
    // datagen (ModModelProvider), so there are no hand-written files for them in resources.
    public static final Block CLASSIC_WOODEN_BUTTON = registerBlock("classic_wooden_button",
            new ButtonBlock(FabricBlockSettings.copyOf(Blocks.OAK_BUTTON).sounds(CLASSIC_WOOD_SOUNDS), BlockSetType.OAK, 30, true));
    public static final Block CLASSIC_STONE_BUTTON = registerBlock("classic_stone_button",
            new ButtonBlock(FabricBlockSettings.copyOf(Blocks.STONE_BUTTON).sounds(CLASSIC_STONE_SOUNDS), BlockSetType.STONE, 20, false));
    public static final Block CLASSIC_WOODEN_PRESSURE_PLATE = registerBlock("classic_wooden_pressure_plate",
            new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE).sounds(CLASSIC_WOOD_SOUNDS), BlockSetType.OAK));
    public static final Block CLASSIC_STONE_PRESSURE_PLATE = registerBlock("classic_stone_pressure_plate",
            new PressurePlateBlock(PressurePlateBlock.ActivationRule.MOBS,
                    FabricBlockSettings.copyOf(Blocks.STONE_PRESSURE_PLATE).sounds(CLASSIC_STONE_SOUNDS), BlockSetType.STONE));
    public static final Block CLASSIC_GLASS_PANE = registerBlock("classic_glass_pane",
            new PaneBlock(FabricBlockSettings.copyOf(Blocks.GLASS_PANE)));


    // Classic Nether block set based on the Alpha 1.2.6 assets. These are separate blocks so the
    // old terrain atlas can be imported without replacing modern Minecraft textures.
    public static final Block CLASSIC_NETHER_NETHERRACK = registerBlock("classic_nether_netherrack",
            new Block(FabricBlockSettings.copyOf(Blocks.NETHERRACK)));
    public static final Block CLASSIC_NETHER_SOUL_SAND = registerBlock("classic_nether_soul_sand",
            new Block(FabricBlockSettings.copyOf(Blocks.SOUL_SAND)));
    public static final Block CLASSIC_NETHER_GLOWSTONE = registerBlock("classic_nether_glowstone",
            new Block(FabricBlockSettings.copyOf(Blocks.GLOWSTONE)));
    public static final Block CLASSIC_NETHER_MAGMA = registerBlock("classic_nether_magma",
            new Block(FabricBlockSettings.copyOf(Blocks.MAGMA_BLOCK)));
    public static final Block CLASSIC_NETHER_QUARTZ_ORE = registerBlock("classic_nether_quartz_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.NETHER_QUARTZ_ORE).strength(3.0F, 3.0F).requiresTool()));
    public static final Block CLASSIC_COAL_BLOCK = registerBlock("classic_coal_block", new Block(FabricBlockSettings.copyOf(Blocks.COAL_BLOCK).sounds(CLASSIC_STONE_SOUNDS)));
    // ============================================================
    // MARS BLOCKS
    // ============================================================
    /** Falling Mars sand, matching vanilla sand behaviour. */
    public static final Block MARS_SAND = registerBlock("mars_sand",
            new FallingBlock(FabricBlockSettings.copyOf(Blocks.SAND)));

    /** Mars sandstone with separate top, bottom and side textures. */
    public static final Block MARS_SANDSTONE = registerBlock("mars_sandstone",
            new Block(FabricBlockSettings.copyOf(Blocks.SANDSTONE)));

    public static final Block MARS_STONE = registerBlock("mars_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).strength(1.5f, 6.0f)));
    public static final Block MARS_COBBLESTONE = registerBlock("mars_cobblestone",
            new Block(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)));
    public static final Block MARS_ANDESITE = registerBlock("mars_andesite",
            new Block(FabricBlockSettings.copyOf(Blocks.ANDESITE)));
    public static final Block MARS_DIORITE = registerBlock("mars_diorite",
            new Block(FabricBlockSettings.copyOf(Blocks.DIORITE)));
    public static final Block MARS_GRANITE = registerBlock("mars_granite",
            new Block(FabricBlockSettings.copyOf(Blocks.GRANITE)));
    public static final Block POLISHED_MARS_STONE = registerBlock("polished_mars_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_GRANITE)));
    public static final Block MARS_POLISHED_ANDESITE = registerBlock("mars_polished_andesite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_ANDESITE)));
    public static final Block MARS_POLISHED_DIORITE = registerBlock("mars_polished_diorite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_DIORITE)));
    public static final Block MARS_POLISHED_GRANITE = registerBlock("mars_polished_granite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_GRANITE)));
    public static final Block MARS_STONE_BRICKS = registerBlock("mars_stone_bricks",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE_BRICKS)));
    public static final Block MARS_STONE_BRICKS_CRACKED = registerBlock("mars_stone_bricks_cracked",
            new Block(FabricBlockSettings.copyOf(Blocks.CRACKED_STONE_BRICKS)));
    public static final Block MARS_CHISELED_STONE_BRICKS = registerBlock("mars_chizelled_stone_bricks",
            new Block(FabricBlockSettings.copyOf(Blocks.CHISELED_STONE_BRICKS)));
    // Mars building variants (crystals and ores intentionally excluded).
    public static final Block MARS_SANDSTONE_STAIRS = registerBlock("mars_sandstone_stairs", new StairsBlock(MARS_SANDSTONE.getDefaultState(), FabricBlockSettings.copyOf(MARS_SANDSTONE)));
    public static final Block MARS_SANDSTONE_SLAB = registerBlock("mars_sandstone_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_SANDSTONE)));
    public static final Block MARS_STONE_STAIRS = registerBlock("mars_stone_stairs", new StairsBlock(MARS_STONE.getDefaultState(), FabricBlockSettings.copyOf(MARS_STONE)));
    public static final Block MARS_STONE_SLAB = registerBlock("mars_stone_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_STONE)));
    public static final Block MARS_COBBLESTONE_STAIRS = registerBlock("mars_cobblestone_stairs", new StairsBlock(MARS_COBBLESTONE.getDefaultState(), FabricBlockSettings.copyOf(MARS_COBBLESTONE)));
    public static final Block MARS_COBBLESTONE_SLAB = registerBlock("mars_cobblestone_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_COBBLESTONE)));
    public static final Block MARS_ANDESITE_STAIRS = registerBlock("mars_andesite_stairs", new StairsBlock(MARS_ANDESITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_ANDESITE)));
    public static final Block MARS_ANDESITE_SLAB = registerBlock("mars_andesite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_ANDESITE)));
    public static final Block MARS_DIORITE_STAIRS = registerBlock("mars_diorite_stairs", new StairsBlock(MARS_DIORITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_DIORITE)));
    public static final Block MARS_DIORITE_SLAB = registerBlock("mars_diorite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_DIORITE)));
    public static final Block MARS_GRANITE_STAIRS = registerBlock("mars_granite_stairs", new StairsBlock(MARS_GRANITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_GRANITE)));
    public static final Block MARS_GRANITE_SLAB = registerBlock("mars_granite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_GRANITE)));
    public static final Block POLISHED_MARS_STONE_STAIRS = registerBlock("polished_mars_stone_stairs", new StairsBlock(POLISHED_MARS_STONE.getDefaultState(), FabricBlockSettings.copyOf(POLISHED_MARS_STONE)));
    public static final Block POLISHED_MARS_STONE_SLAB = registerBlock("polished_mars_stone_slab", new SlabBlock(FabricBlockSettings.copyOf(POLISHED_MARS_STONE)));
    public static final Block MARS_POLISHED_ANDESITE_STAIRS = registerBlock("mars_polished_andesite_stairs", new StairsBlock(MARS_POLISHED_ANDESITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_POLISHED_ANDESITE)));
    public static final Block MARS_POLISHED_ANDESITE_SLAB = registerBlock("mars_polished_andesite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_POLISHED_ANDESITE)));
    public static final Block MARS_POLISHED_DIORITE_STAIRS = registerBlock("mars_polished_diorite_stairs", new StairsBlock(MARS_POLISHED_DIORITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_POLISHED_DIORITE)));
    public static final Block MARS_POLISHED_DIORITE_SLAB = registerBlock("mars_polished_diorite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_POLISHED_DIORITE)));
    public static final Block MARS_POLISHED_GRANITE_STAIRS = registerBlock("mars_polished_granite_stairs", new StairsBlock(MARS_POLISHED_GRANITE.getDefaultState(), FabricBlockSettings.copyOf(MARS_POLISHED_GRANITE)));
    public static final Block MARS_POLISHED_GRANITE_SLAB = registerBlock("mars_polished_granite_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_POLISHED_GRANITE)));
    public static final Block MARS_STONE_BRICKS_STAIRS = registerBlock("mars_stone_bricks_stairs", new StairsBlock(MARS_STONE_BRICKS.getDefaultState(), FabricBlockSettings.copyOf(MARS_STONE_BRICKS)));
    public static final Block MARS_STONE_BRICKS_SLAB = registerBlock("mars_stone_bricks_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_STONE_BRICKS)));
    public static final Block MARS_STONE_BRICKS_CRACKED_STAIRS = registerBlock("mars_stone_bricks_cracked_stairs", new StairsBlock(MARS_STONE_BRICKS_CRACKED.getDefaultState(), FabricBlockSettings.copyOf(MARS_STONE_BRICKS_CRACKED)));
    public static final Block MARS_STONE_BRICKS_CRACKED_SLAB = registerBlock("mars_stone_bricks_cracked_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_STONE_BRICKS_CRACKED)));
    public static final Block MARS_CHISELED_STONE_BRICKS_STAIRS = registerBlock("mars_chizelled_stone_bricks_stairs", new StairsBlock(MARS_CHISELED_STONE_BRICKS.getDefaultState(), FabricBlockSettings.copyOf(MARS_CHISELED_STONE_BRICKS)));
    public static final Block MARS_CHISELED_STONE_BRICKS_SLAB = registerBlock("mars_chizelled_stone_bricks_slab", new SlabBlock(FabricBlockSettings.copyOf(MARS_CHISELED_STONE_BRICKS)));

    public static final Block MARS_IRON_ORE = registerBlock("mars_iron_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_ORE)));
    public static final Block PISS_CRYSTAL = registerBlock("piss_crystal",
            new AmethystClusterBlock(7, 3, FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER)
                    .luminance(state -> 8)
                    .sounds(BlockSoundGroup.AMETHYST_CLUSTER)));

    // ============================================================
    // MISC BLOCKS
    // ============================================================

    public static final Block RAW_STEEL_BLOCK = registerBlock(
            "raw_steel_block",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.RAW_IRON_BLOCK)
            )
    );

    public static final Block HARTNELL_BLOCK = registerBlock(
            "hartnell_block",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.STONE)
            )
    );

    public static final Block STEEL_BLOCK = registerBlock(
            "steel_block",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
            )
    );

    public static final Block REINFORCED_STEEL_BLOCK = registerBlock(
            "reinforced_steel_block",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OBSIDIAN)
                            .sounds(BlockSoundGroup.METAL)
            )
    );

    // Steel grates
    public static final Block GRATE_BLOCK = registerBlock(
            "grate_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .nonOpaque())
    );

    public static final Block THICK_GRATE_BLOCK = registerBlock(
            "thick_grate_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .nonOpaque())
    );

    public static final Block RUSTY_GRATE_BLOCK = registerBlock(
            "rusty_grate_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .nonOpaque())
    );

    public static final Block RUSTY_THICK_GRATE_BLOCK = registerBlock(
            "rusty_thick_grate_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .nonOpaque())
    );

    // Storage block for Metalertanium Ingots. Netherite-grade, like the gear.
    public static final Block METALERTANIUM_BLOCK = registerBlock(
            "metalertanium_block",
            new Block(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK))
    );

    public static final Block TREE_TAPPER = registerBlock(
            "tapper",
            new TapperBlock(
                    FabricBlockSettings.create()
                            .strength(1.0f)
                            .sounds(BlockSoundGroup.LANTERN)
                            .nonOpaque()
            )
    );

    // ============================================================
    // Ores
    // ============================================================

    public static final Block SONIC_CRYSTAL_ORE = registerBlock("sonic_crystal_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(2f), UniformIntProvider.create(2, 5)));

    public static final Block DEEPSLATE_SONIC_CRYSTAL_ORE = registerBlock("deepslate_sonic_crystal_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE).strength(4f), UniformIntProvider.create(2, 5)));

    public static final Block NETHER_SONIC_CRYSTAL_ORE = registerBlock("nether_sonic_crystal_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.NETHERRACK).strength(1.5f), UniformIntProvider.create(2, 5)));

    public static final Block WHITE_POINT_ORE = registerBlock("white_point_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(2f), UniformIntProvider.create(2, 5)));

    public static final Block DEEPSLATE_WHITE_POINT_ORE = registerBlock("deepslate_white_point_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE).strength(4f), UniformIntProvider.create(2, 5)));

    public static final Block NETHER_WHITE_POINT_ORE = registerBlock("nether_white_point_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.NETHERRACK).strength(1.5f), UniformIntProvider.create(2, 5)));

    public static final Block PREHISTORIC_ORE = registerBlock("prehistoric_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.STONE).strength(3.0f).requiresTool(), UniformIntProvider.create(2, 5)));

    public static final Block DEEPSLATE_PREHISTORIC_ORE = registerBlock("deepslate_prehistoric_ore",
            new ExperienceDroppingBlock(FabricBlockSettings.copyOf(Blocks.DEEPSLATE).strength(4.5f).requiresTool(), UniformIntProvider.create(2, 5)));


    // ============================================================
    // ROUNDELS
    // ============================================================

    public static final Block BASALT_ROUNDEL = registerBlock(
            "basalt_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.COBBLESTONE)
                            .sounds(BlockSoundGroup.BASALT)
            )
    );

    public static final Block BONE_ROUNDEL = registerBlock(
            "bone_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.COBBLESTONE)
                            .sounds(BlockSoundGroup.BONE)
            )
    );

    public static final Block STRUCTURE_ROUNDEL = registerBlock(
            "structure_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.COBBLESTONE)
                            .sounds(BlockSoundGroup.STONE)
            )
    );

    public static final Block LODESTONE_ROUNDEL = registerBlock(
            "lodestone_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.COBBLESTONE)
                            .sounds(BlockSoundGroup.STONE)
            )
    );

    public static final Block QUARTZ_ROUNDEL = registerBlock(
            "quartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block VANILLA_QUATZ_ROUNDEL = registerBlock(
            "vanilla_quartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block REACTOR_ROUNDEL = registerBlock(
            "reactor_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.NETHERRACK)
            )
    );

    public static final Block AMBQUARTZ_ROUNDEL = registerBlock(
            "ambquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block BLUEQUARTZ_ROUNDEL = registerBlock(
            "bluequartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block BLACK_CONCRETE_ROUNDEL = registerBlock(
            "black_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block BLUE_CONCRETE_ROUNDEL = registerBlock(
            "blue_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block BROWN_CONCRETE_ROUNDEL = registerBlock(
            "brown_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block COPPER_ROUNDEL = registerBlock(
            "copper_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK)
            )
    );

    public static final Block CRIMQUARTZ_ROUNDEL = registerBlock(
            "crimquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block OBSIQUARTZ_ROUNDEL = registerBlock(
            "obsiquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block VERDQUARTZ_ROUNDEL = registerBlock(
            "verdquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block VIOQUARTZ_ROUNDEL = registerBlock(
            "vioquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
                            .sounds(BlockSoundGroup.STONE)
            )
    );

    public static final Block CYQUARTZ_ROUNDEL = registerBlock(
            "cyquartz_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.QUARTZ_BLOCK)
            )
    );

    public static final Block DIRT_ROUNDEL = registerBlock(
            "dirt_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.DIRT)
            )
    );

    public static final Block END_STONE_BRICKS_ROUNDEL = registerBlock(
            "end_stone_bricks_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.END_STONE_BRICKS)
            )
    );

    public static final Block EXPOSED_COPPER_ROUNDEL = registerBlock(
            "exposed_copper_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.EXPOSED_COPPER)
            )
    );

    public static final Block GRAY_CONCRETE_ROUNDEL = registerBlock(
            "gray_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block GREEN_CONCRETE_ROUNDEL = registerBlock(
            "green_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block LIGHT_BLUE_CONCRETE_ROUNDEL = registerBlock(
            "light_blue_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block LIGHT_GRAY_CONCRETE_ROUNDEL = registerBlock(
            "light_gray_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block LIME_CONCRETE_ROUNDEL = registerBlock(
            "lime_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block MAGENTA_CONCRETE_ROUNDEL = registerBlock(
            "magenta_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block MOSS_ROUNDEL = registerBlock(
            "moss_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK)
            )
    );

    public static final Block ORANGE_CONCRETE_ROUNDEL = registerBlock(
            "orange_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block OXIDIZED_COPPER_ROUNDEL = registerBlock(
            "oxidized_copper_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block POLISHED_DIORITE_ROUNDEL = registerBlock(
            "polished_diorite_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.POLISHED_DIORITE)
            )
    );

    public static final Block POLISHED_ANDESITE_ROUNDEL = registerBlock(
            "polished_andesite_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.POLISHED_ANDESITE)
            )
    );

    public static final Block POLISHED_DEEPSLATE_ROUNDEL = registerBlock(
            "polished_deepslate_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.POLISHED_DEEPSLATE)
            )
    );

    public static final Block PINK_CONCRETE_ROUNDEL = registerBlock(
            "pink_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block POLISHED_GRANITE_ROUNDEL = registerBlock(
            "polished_granite_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.POLISHED_GRANITE)
            )
    );

    public static final Block PURPLE_CONCRETE_ROUNDEL = registerBlock(
            "purple_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block RED_CONCRETE_ROUNDEL = registerBlock(
            "red_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block SANDSTONE_ROUNDEL = registerBlock(
            "sandstone_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.SANDSTONE)
            )
    );

    public static final Block STRIPPED_ACACIA_LOG_ROUNDEL = registerBlock(
            "stripped_acacia_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_ACACIA_LOG)
                            .mapColor(Blocks.STRIPPED_ACACIA_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_BIRCH_LOG_ROUNDEL = registerBlock(
            "stripped_birch_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_BIRCH_LOG)
                            .mapColor(Blocks.STRIPPED_BIRCH_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_CHERRY_LOG_ROUNDEL = registerBlock(
            "stripped_cherry_log_roundel",
            new RoundelBlock(

                    FabricBlockSettings.copyOf(Blocks.STRIPPED_CHERRY_LOG)
                            .mapColor(Blocks.STRIPPED_CHERRY_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_DARK_OAK_LOG_ROUNDEL = registerBlock(
            "stripped_dark_oak_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_DARK_OAK_LOG)
                            .mapColor(Blocks.STRIPPED_DARK_OAK_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_JUNGLE_LOG_ROUNDEL = registerBlock(
            "stripped_jungle_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_JUNGLE_LOG)
                            .mapColor(Blocks.STRIPPED_JUNGLE_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_MANGROVE_LOG_ROUNDEL = registerBlock(
            "stripped_mangrove_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_MANGROVE_LOG)
                            .mapColor(Blocks.STRIPPED_MANGROVE_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_OAK_LOG_ROUNDEL = registerBlock(
            "stripped_oak_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_OAK_LOG)
                            .mapColor(Blocks.STRIPPED_OAK_LOG.getDefaultMapColor())
            )
    );

    public static final Block STRIPPED_SPRUCE_LOG_ROUNDEL = registerBlock(
            "stripped_spruce_log_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STRIPPED_SPRUCE_LOG)
                            .mapColor(Blocks.STRIPPED_SPRUCE_LOG.getDefaultMapColor())
            )
    );

    public static final Block WEATHERED_COPPER_ROUNDEL = registerBlock(
            "weathered_copper_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.WEATHERED_COPPER)
            )
    );

    public static final Block WHITE_CONCRETE_ROUNDEL = registerBlock(
            "white_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block YELLOW_CONCRETE_ROUNDEL = registerBlock(
            "yellow_concrete_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.BLUE_CONCRETE)
            )
    );

    public static final Block HARTNELL_ROUNDEL = registerBlock(
            "hartnell_roundel",
            new RoundelBlock(
                    FabricBlockSettings.copyOf(Blocks.STONE)
            )
    );

    public static final Block HARTNELL_WALL = registerBlock(
            "hartnell_wall",
            new WallBlock(
                    FabricBlockSettings.copyOf(Blocks.STONE)
            )
    );

        // ============================================================
        // SONIC SCREWDRIVER
        // ============================================================

        public static final Block SONIC_SIGNAL = registerBlock(
                "sonic_signal",
                new SonicSignalBlock(
                        FabricBlockSettings.create()
                                .strength(-1.0f, 3600000.0f)
                                .nonOpaque()
                                .noCollision()
                                .dropsNothing()
                                .luminance(state -> 0)
                )
        );


    public static final Block SONIC_WORKSHOP = registerBlock(
            "sonic_workshop",
            new SonicWorkshopBlock(
                    FabricBlockSettings.copyOf(Blocks.SMOOTH_STONE_SLAB)
                            .strength(3.0f)
                            .sounds(BlockSoundGroup.METAL)
                            .nonOpaque()
            ),
            true
    );




    // ============================================================
    // TARDIS WOOD SET
    // ============================================================

    public static final Block TARDIS_LEAVES = registerBlock(
            "tardis_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block TARDIS_WOOD = registerBlock(
            "tardis_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_LOG = registerBlock(
            "tardis_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_TARDIS_LOG = registerBlock(
            "stripped_tardis_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_TARDIS_WOOD = registerBlock(
            "stripped_tardis_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_PLANKS = registerBlock(
            "tardis_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_SAPLING = registerBlock(
            "tardis_sapling",
            new SaplingBlock(
                    new TardisSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_TARDIS_SAPLING = registerBlock(
            "potted_tardis_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.TARDIS_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block TARDIS_STAIRS = registerBlock(
            "tardis_stairs",
            new StairsBlock(
                    GallifreyModBlocks.TARDIS_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_SLAB = registerBlock(
            "tardis_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_BUTTON = registerBlock(
            "tardis_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block TARDIS_PRESSURE_PLATE = registerBlock(
            "tardis_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block TARDIS_FENCE = registerBlock(
            "tardis_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TARDIS_FENCE_GATE = registerBlock(
            "tardis_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block TARDIS_WOOD_DOOR = registerBlock(
            "tardis_wood_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_DOOR),
                    BlockSetType.OAK
            )
    );

    public static final Block TARDIS_TRAPDOOR = registerBlock(
            "tardis_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_PLANKS),
                    BlockSetType.OAK
            )
    );


    // ============================================================
    // TARDIS SIGNS
    // ============================================================

    public static final Identifier TARDIS_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/tardis");

    public static final Identifier TARDIS_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/tardis");

    public static final Identifier TARDIS_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/tardis"
            );

    public static final Block STANDING_TARDIS_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "tardis_standing_sign"
                    ),
                    new TerraformSignBlock(
                            TARDIS_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_SIGN)
                    )
            );

    public static final Block WALL_TARDIS_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "tardis_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            TARDIS_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_SIGN).dropsLike(STANDING_TARDIS_SIGN)
                    )
            );

    public static final Block HANGING_TARDIS_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "tardis_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            TARDIS_HANGING_SIGN_TEXTURE,
                            TARDIS_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_TARDIS_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "tardis_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            TARDIS_HANGING_SIGN_TEXTURE,
                            TARDIS_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING_TARDIS_SIGN)
                    )
            );


    public static final BlockFamily TARDIS_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.TARDIS_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_TARDIS_SIGN,
                            GallifreyModBlocks.WALL_TARDIS_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();


    // ============================================================
    // ULANDA WOOD SET
    // ============================================================

    public static final Block ULANDA_LEAVES = registerBlock(
            "ulanda_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block ULANDA_WOOD = registerBlock(
            "ulanda_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_LOG = registerBlock(
            "ulanda_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_ULANDA_LOG = registerBlock(
            "stripped_ulanda_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_ULANDA_WOOD = registerBlock(
            "stripped_ulanda_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_PLANKS = registerBlock(
            "ulanda_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_SAPLING = registerBlock(
            "ulanda_sapling",
            new SaplingBlock(
                    new UlandaSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_ULANDA_SAPLING = registerBlock(
            "potted_ulanda_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.ULANDA_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block ULANDA_STAIRS = registerBlock(
            "ulanda_stairs",
            new StairsBlock(
                    GallifreyModBlocks.ULANDA_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_SLAB = registerBlock(
            "ulanda_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_BUTTON = registerBlock(
            "ulanda_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block ULANDA_PRESSURE_PLATE = registerBlock(
            "ulanda_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block ULANDA_FENCE = registerBlock(
            "ulanda_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block ULANDA_FENCE_GATE = registerBlock(
            "ulanda_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block ULANDA_DOOR = registerBlock(
            "ulanda_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.ACACIA_DOOR),
                    BlockSetType.ACACIA
            )
    );

    public static final Block ULANDA_TRAPDOOR = registerBlock(
            "ulanda_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.ACACIA_DOOR),
                    BlockSetType.ACACIA
            )
    );


    // ============================================================
    // ULANDA SIGNS
    // ============================================================

    public static final Identifier ULANDA_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/ulanda");

    public static final Identifier ULANDA_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/ulanda");

    public static final Identifier ULANDA_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/ulanda"
            );

    public static final Block STANDING_ULANDA_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ulanda_standing_sign"
                    ),
                    new TerraformSignBlock(
                            ULANDA_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.ACACIA_SIGN)
                    )
            );

    public static final Block WALL_ULANDA_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ulanda_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            ULANDA_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.ACACIA_WALL_SIGN).dropsLike(STANDING_ULANDA_SIGN)
                    )
            );

    public static final Block HANGING_ULANDA_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ulanda_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            ULANDA_HANGING_SIGN_TEXTURE,
                            ULANDA_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.ACACIA_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_ULANDA_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ulanda_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            ULANDA_HANGING_SIGN_TEXTURE,
                            ULANDA_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.ACACIA_WALL_HANGING_SIGN).dropsLike(HANGING_ULANDA_SIGN)
                    )
            );

    public static final BlockFamily ULANDA_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.ULANDA_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_ULANDA_SIGN,
                            GallifreyModBlocks.WALL_ULANDA_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();

    // ============================================================
    // TREEBORG WOOD SET
    // ============================================================

    public static final Block TREEBORG_LEAVES = registerBlock(
            "treeborg_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block TREEBORG_WOOD = registerBlock(
            "treeborg_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_LOG = registerBlock(
            "treeborg_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_TREEBORG_LOG = registerBlock(
            "stripped_treeborg_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_TREEBORG_WOOD = registerBlock(
            "stripped_treeborg_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_PLANKS = registerBlock(
            "treeborg_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_SAPLING = registerBlock(
            "treeborg_sapling",
            new SaplingBlock(
                    new TreeborgSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_TREEBORG_SAPLING = registerBlock(
            "potted_treeborg_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.TREEBORG_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block TREEBORG_STAIRS = registerBlock(
            "treeborg_stairs",
            new StairsBlock(
                    GallifreyModBlocks.TARDIS_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_SLAB = registerBlock(
            "treeborg_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_BUTTON = registerBlock(
            "treeborg_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block TREEBORG_PRESSURE_PLATE = registerBlock(
            "treeborg_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block TREEBORG_FENCE = registerBlock(
            "treeborg_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block TREEBORG_FENCE_GATE = registerBlock(
            "treeborg_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block TREEBORG_DOOR = registerBlock(
            "treeborg_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_DOOR),
                    BlockSetType.OAK
            )
    );

    public static final Block TREEBORG_TRAPDOOR = registerBlock(
            "treeborg_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_TRAPDOOR),
                    BlockSetType.JUNGLE
            )
    );


    // ============================================================
    // TREEBORG SIGNS
    // ============================================================

    public static final Identifier TREEBORG_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/treeborg");

    public static final Identifier TREEBORG_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/treeborg");

    public static final Identifier TREEBORG_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/treeborg"
            );

    public static final Block STANDING_TREEBORG_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "treeborg_standing_sign"
                    ),
                    new TerraformSignBlock(
                            TREEBORG_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_SIGN)
                    )
            );

    public static final Block WALL_TREEBORG_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "treeborg_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            TREEBORG_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_SIGN).dropsLike(STANDING_TREEBORG_SIGN)
                    )
            );

    public static final Block HANGING_TREEBORG_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "treeborg_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            TREEBORG_HANGING_SIGN_TEXTURE,
                            TREEBORG_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_TREEBORG_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "treeborg_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            TREEBORG_HANGING_SIGN_TEXTURE,
                            TREEBORG_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING_TREEBORG_SIGN)
                    )
            );


    public static final BlockFamily TREEBORG_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.TREEBORG_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_TREEBORG_SIGN,
                            GallifreyModBlocks.WALL_TREEBORG_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();

    // ============================================================
    // ASH WOOD SET
    // ============================================================

    public static final Block ASH_LEAVES = registerBlock(
            "ash_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_LEAVES)
            )
    );

    public static final Block ASH_WOOD = registerBlock(
            "ash_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_LOG = registerBlock(
            "ash_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_LOG)
            )
    );

    public static final Block STRIP_ASH_LOG = registerBlock(
            "stripped_ash_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_LOG)
            )
    );

    public static final Block STRIP_ASH_WOOD = registerBlock(
            "stripped_ash_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_PLANKS = registerBlock(
            "ash_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_SAPLING = registerBlock(
            "ash_sapling",
            new SaplingBlock(
                    new AshSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_ASH_SAPLING = registerBlock(
            "potted_ash_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.ASH_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block ASH_STAIRS = registerBlock(
            "ash_stairs",
            new StairsBlock(
                    GallifreyModBlocks.ASH_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_SLAB = registerBlock(
            "ash_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_BUTTON = registerBlock(
            "ash_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD),
                    BlockSetType.MANGROVE,
                    15,
                    true
            )
    );

    public static final Block ASH_PRESSURE_PLATE = registerBlock(
            "ash_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD),
                    BlockSetType.MANGROVE
            )
    );

    public static final Block ASH_FENCE = registerBlock(
            "ash_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.MANGROVE_WOOD)
            )
    );

    public static final Block ASH_FENCE_GATE = registerBlock(
            "ash_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.MANGROVE
            )
    );

    public static final Block ASH_DOOR = registerBlock(
            "ash_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.ACACIA_DOOR),
                    BlockSetType.MANGROVE
            )
    );

    public static final Block ASH_TRAPDOOR = registerBlock(
            "ash_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_TRAPDOOR),
                    BlockSetType.MANGROVE
            )
    );


    // ============================================================
    // ASH SIGNS
    // ============================================================

    public static final Identifier ASH_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/ash");

    public static final Identifier ASH_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/ash");

    public static final Identifier ASH_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/ash"
            );

    public static final Block STANDING_ASH_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ash_standing_sign"
                    ),
                    new TerraformSignBlock(
                            ASH_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_SIGN)
                    )
            );

    public static final Block WALL_ASH_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ash_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            ASH_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_SIGN).dropsLike(STANDING_ASH_SIGN)
                    )
            );

    public static final Block HANGING_ASH_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ash_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            ASH_HANGING_SIGN_TEXTURE,
                            ASH_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_ASH_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "ash_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            ASH_HANGING_SIGN_TEXTURE,
                            ASH_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING_ASH_SIGN)
                    )
            );


    public static final BlockFamily ASH_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.ASH_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_ASH_SIGN,
                            GallifreyModBlocks.WALL_ASH_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();

    // ============================================================
    // MAPLE WOOD SET
    // ============================================================

    public static final Block MAPLE_LEAVES = registerBlock(
            "maple_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block MAPLE_WOOD = registerBlock(
            "maple_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_LOG = registerBlock(
            "maple_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_MAPLE_LOG = registerBlock(
            "stripped_maple_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_MAPLE_WOOD = registerBlock(
            "stripped_maple_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_PLANKS = registerBlock(
            "maple_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_SAPLING = registerBlock(
            "maple_sapling",
            new SaplingBlock(
                    new MapleSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_MAPLE_SAPLING = registerBlock(
            "potted_maple_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.MAPLE_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block MAPLE_STAIRS = registerBlock(
            "maple_stairs",
            new StairsBlock(
                    GallifreyModBlocks.MAPLE_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_SLAB = registerBlock(
            "maple_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_BUTTON = registerBlock(
            "maple_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block MAPLE_PRESSURE_PLATE = registerBlock(
            "maple_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block MAPLE_FENCE = registerBlock(
            "maple_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MAPLE_FENCE_GATE = registerBlock(
            "maple_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block MAPLE_DOOR = registerBlock(
            "maple_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.DARK_OAK_DOOR),
                    BlockSetType.ACACIA
            )
    );

    public static final Block MAPLE_TRAPDOOR = registerBlock(
            "maple_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.DARK_OAK_TRAPDOOR),
                    BlockSetType.ACACIA
            )
    );


    // ============================================================
    // MAPLE SIGNS
    // ============================================================

    public static final Identifier MAPLE_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/maple");

    public static final Identifier MAPLE_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/maple");

    public static final Identifier MAPLE_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/maple"
            );

    public static final Block STANDING_MAPLE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "maple_standing_sign"
                    ),
                    new TerraformSignBlock(
                            MAPLE_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_SIGN)
                    )
            );

    public static final Block WALL_MAPLE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "maple_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            MAPLE_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_SIGN).dropsLike(STANDING_MAPLE_SIGN)
                    )
            );

    public static final Block HANGING_MAPLE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "maple_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            MAPLE_HANGING_SIGN_TEXTURE,
                            MAPLE_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_MAPLE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "maple_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            MAPLE_HANGING_SIGN_TEXTURE,
                            MAPLE_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING_MAPLE_SIGN)
                    )
            );

    public static final BlockFamily MAPLE_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.MAPLE_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_MAPLE_SIGN,
                            GallifreyModBlocks.WALL_MAPLE_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();

    // ============================================================
    // MOONPINE WOOD SET
    // ============================================================

    public static final Block MOONPINE_LEAVES = registerBlock(
            "moonpine_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block MOONPINE_WOOD = registerBlock(
            "moonpine_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_LOG = registerBlock(
            "moonpine_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_MOONPINE_LOG = registerBlock(
            "stripped_moonpine_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block STRIP_MOONPINE_WOOD = registerBlock(
            "stripped_moonpine_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_PLANKS = registerBlock(
            "moonpine_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_SAPLING = registerBlock(
            "moonpine_sapling",
            new SaplingBlock(
                    new MoonpineSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_MOONPINE_SAPLING = registerBlock(
            "potted_moonpine_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.MOONPINE_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block MOONPINE_STAIRS = registerBlock(
            "moonpine_stairs",
            new StairsBlock(
                    GallifreyModBlocks.MOONPINE_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_SLAB = registerBlock(
            "moonpine_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_BUTTON = registerBlock(
            "moonpine_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block MOONPINE_PRESSURE_PLATE = registerBlock(
            "moonpine_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block MOONPINE_FENCE = registerBlock(
            "moonpine_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block MOONPINE_FENCE_GATE = registerBlock(
            "moonpine_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block MOONPINE_DOOR = registerBlock(
            "moonpine_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.DARK_OAK_DOOR),
                    BlockSetType.ACACIA
            )
    );

    public static final Block MOONPINE_TRAPDOOR = registerBlock(
            "moonpine_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.DARK_OAK_TRAPDOOR),
                    BlockSetType.ACACIA
            )
    );


    // ============================================================
    // MOONPINE SIGNS
    // ============================================================

    public static final Identifier MOONPINE_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/moonpine");

    public static final Identifier MOONPINE_HANGING_SIGN_TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "entity/signs/hanging/moonpine");

    public static final Identifier MOONPINE_HANGING_GUI_SIGN_TEXTURE =
            new Identifier(
                    GallifreyMod.MOD_ID,
                    "textures/gui/hanging_signs/moonpine"
            );

    public static final Block STANDING_MOONPINE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "moonpine_standing_sign"
                    ),
                    new TerraformSignBlock(
                            MOONPINE_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_SIGN)
                    )
            );

    public static final Block WALL_MOONPINE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "moonpine_wall_sign"
                    ),
                    new TerraformWallSignBlock(
                            MOONPINE_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_SIGN).dropsLike(STANDING_MOONPINE_SIGN)
                    )
            );

    public static final Block HANGING_MOONPINE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "moonpine_hanging_sign"
                    ),
                    new TerraformHangingSignBlock(
                            MOONPINE_HANGING_SIGN_TEXTURE,
                            MOONPINE_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_HANGING_SIGN)
                    )
            );

    public static final Block WALL_HANGING_MOONPINE_SIGN =
            Registry.register(
                    Registries.BLOCK,
                    new Identifier(
                            GallifreyMod.MOD_ID,
                            "moonpine_wall_hanging_sign"
                    ),
                    new TerraformWallHangingSignBlock(
                            MOONPINE_HANGING_SIGN_TEXTURE,
                            MOONPINE_HANGING_GUI_SIGN_TEXTURE,
                            FabricBlockSettings.copyOf(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(HANGING_MOONPINE_SIGN)
                    )
            );

    public static final BlockFamily MOONPINE_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.MOONPINE_PLANKS)
                    .sign(
                            GallifreyModBlocks.STANDING_MOONPINE_SIGN,
                            GallifreyModBlocks.WALL_MOONPINE_SIGN
                    )
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();

    // ============================================================
    // WASTED WOOD SET
    // ============================================================

    public static final Block WASTED_LEAVES = registerBlock(
            "wasted_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LEAVES)
            )
    );

    public static final Block WASTED_WOOD = registerBlock(
            "wasted_wood",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block WASTED_LOG = registerBlock(
            "wasted_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            )
    );

    public static final Block WASTED_PLANKS = registerBlock(
            "wasted_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block WASTED_SAPLING = registerBlock(
            "wasted_sapling",
            new SaplingBlock(
                    new WastedSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.OAK_SAPLING)
            )
    );

    public static final Block POTTED_WASTED_SAPLING = registerBlock(
            "potted_wasted_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.PREHISTORIC_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_OAK_SAPLING)
            )
    );

    public static final Block WASTED_STAIRS = registerBlock(
            "wasted_stairs",
            new StairsBlock(
                    GallifreyModBlocks.WASTED_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block WASTED_SLAB = registerBlock(
            "wasted_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block WASTED_BUTTON = registerBlock(
            "wasted_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK,
                    15,
                    true
            )
    );

    public static final Block WASTED_PRESSURE_PLATE = registerBlock(
            "wasted_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    BlockSetType.OAK
            )
    );

    public static final Block WASTED_FENCE = registerBlock(
            "wasted_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD)
            )
    );

    public static final Block WASTED_FENCE_GATE = registerBlock(
            "wasted_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_WOOD),
                    WoodType.OAK
            )
    );

    public static final Block WASTED_DOOR = registerBlock(
            "wasted_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_DOOR),
                    BlockSetType.OAK
            )
    );

    public static final Block WASTED_TRAPDOOR = registerBlock(
            "wasted_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.DARK_OAK_TRAPDOOR),
                    BlockSetType.ACACIA
            )
    );


    // ============================================================
    // PREHISTORIC WOOD SET
    // ============================================================

    public static final Block PREHISTORIC_LEAVES = registerBlock(
            "prehistoric_leaves",
            new LeavesBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_LEAVES)
            )
    );

    public static final Block PREHISTORIC_VINE = registerBlock(
            "prehistoric_vine",
            new VineBlock(
                    FabricBlockSettings.copyOf(Blocks.VINE)
            )
    );


    public static final Block PREHISTORIC_LOG = registerBlock(
            "prehistoric_log",
            new PillarBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_LOG)
            )
    );

    public static final Block PREHISTORIC_PLANKS = registerBlock(
            "prehistoric_planks",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD)
            )
    );

    public static final Block PREHISTORIC_SAPLING = registerBlock(
            "prehistoric_sapling",
            new SaplingBlock(
                    new PrehistoricSaplingGenerator(),
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_SAPLING)
            )
    );

    public static final Block POTTED_PREHISTORIC_SAPLING = registerBlock(
            "potted_prehistoric_sapling",
            new FlowerPotBlock(
                    GallifreyModBlocks.PREHISTORIC_SAPLING,
                    FabricBlockSettings.copyOf(Blocks.POTTED_JUNGLE_SAPLING)
            )
    );

    public static final Block PREHISTORIC_STAIRS = registerBlock(
            "prehistoric_stairs",
            new StairsBlock(
                    GallifreyModBlocks.PREHISTORIC_PLANKS.getDefaultState(),
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD)
            )
    );

    public static final Block PREHISTORIC_SLAB = registerBlock(
            "prehistoric_slab",
            new SlabBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD)
            )
    );

    public static final Block PREHISTORIC_BUTTON = registerBlock(
            "prehistoric_button",
            new ButtonBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD),
                    BlockSetType.JUNGLE,
                    15,
                    true
            )
    );

    public static final Block PREHISTORIC_PRESSURE_PLATE = registerBlock(
            "prehistoric_pressure_plate",
            new PressurePlateBlock(
                    PressurePlateBlock.ActivationRule.EVERYTHING,
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD),
                    BlockSetType.JUNGLE
            )
    );

    public static final Block PREHISTORIC_FENCE = registerBlock(
            "prehistoric_fence",
            new FenceBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD)
            )
    );

    public static final Block PREHISTORIC_FENCE_GATE = registerBlock(
            "prehistoric_fence_gate",
            new FenceGateBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_WOOD),
                    WoodType.JUNGLE
            )
    );

    public static final Block PREHISTORIC_DOOR = registerBlock(
            "prehistoric_door",
            new DoorBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_DOOR),
                    BlockSetType.JUNGLE
            )
    );

    public static final Block PREHISTORIC_TRAPDOOR = registerBlock(
            "prehistoric_trapdoor",
            new TrapdoorBlock(
                    FabricBlockSettings.copyOf(Blocks.JUNGLE_TRAPDOOR),
                    BlockSetType.JUNGLE
            )
    );


    public static final BlockFamily PREHISTORIC_FAMILY =
            BlockFamilies.register(GallifreyModBlocks.PREHISTORIC_PLANKS)
                    .group("wooden")
                    .unlockCriterionName("has_planks")
                    .build();



    // Storage block for Prehistoric Ingots (9 ingots -> 1 block).
    public static final Block PREHISTORIC_BLOCK = registerBlock(
            "prehistoric_block",
            new Block(
                    FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
            )
    );

    // ============================================================
    // ATRIUM
    // ============================================================

    public static final Block ATRIUM_BLOCK = registerBlock("atrium_block",
            new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()
                    .strength(5.0F, 6.0F)));

    public static final Block ATRIUM_ORE = registerBlock("atrium_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.IRON_ORE)
                            .sounds(BlockSoundGroup.STONE)
                            .requiresTool()
                            .strength(3.0F, 3.0F),
                    UniformIntProvider.create(2, 5)));

    public static final Block DEEPSLATE_ATRIUM_ORE = registerBlock("deepslate_atrium_ore",
            new ExperienceDroppingBlock(
                    FabricBlockSettings.copyOf(Blocks.DEEPSLATE_IRON_ORE)
                            .sounds(BlockSoundGroup.DEEPSLATE)
                            .requiresTool()
                            .strength(4.5F, 3.0F),
                    UniformIntProvider.create(2, 5)));

    // ============================================================
    // TARDIS EXTERIOR
    // ============================================================

    public static final Block TARDIS_EXTERIOR = registerBlock(
            "tardis_exterior",
            new TardisExteriorBlock(
                    FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)
                            .nonOpaque()
                            .strength(2.0f)
            )
    );


    // ============================================================
    // BLOCK REGISTRATION
    // ============================================================

    private static Block registerBlock(String name, Block block) {
        return registerBlock(name, block, false);
    }

    private static Block registerBlock(String name, Block block, boolean sonicWorkshopItem) {
        registerBlockItem(name, block, sonicWorkshopItem);

        return Registry.register(
                Registries.BLOCK,
                new Identifier(GallifreyMod.MOD_ID, name),
                block
        );
    }

    private static Item registerBlockItem(String name, Block block) {
        return registerBlockItem(name, block, false);
    }

    private static Item registerBlockItem(String name, Block block, boolean sonicWorkshopItem) {
        Item item = sonicWorkshopItem
                ? new SonicWorkshopItem(block, new FabricItemSettings())
                : new BlockItem(block, new FabricItemSettings());

        return Registry.register(
                Registries.ITEM,
                new Identifier(GallifreyMod.MOD_ID, name),
                item
        );
    }

    // ============================================================
    // SKARO BLOCKS (ported from Adventures With TARDISes)
    // ============================================================

    public static final Block DALEKANIUM_BLOCK = registerBlock("dalekanium_block",
            new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_BLOCK)
                    .sounds(BlockSoundGroup.DEEPSLATE).requiresTool().strength(1.5F, 3.0F)));

    public static final Block DALEKANIUM_ORE = registerBlock("dalekanium_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.COPPER_ORE)
                    .sounds(BlockSoundGroup.STONE).requiresTool().strength(1.5F, 3.0F)));

    public static final Block DEEPSLATE_DALEKANIUM_ORE = registerBlock("deepslate_dalekanium_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.COPPER_ORE)
                    .sounds(BlockSoundGroup.STONE).requiresTool().strength(1.5F, 3.0F)));

    public static final Block EXQUISITE_CAT = registerBlock("exquisite_cat",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    public static final Block GOOD_HEAVENS = registerBlock("good_heavens",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE)));

    // Skaro stone set.  These deliberately mirror the Overworld stone family,
    // but use Skaro textures so the dimension has its own identity.
    public static final Block SKARO_STONE = registerBlock("skaro_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).requiresTool()));
    public static final Block SKARO_COBBLESTONE = registerBlock("skaro_cobblestone",
            new Block(FabricBlockSettings.copyOf(Blocks.COBBLESTONE).requiresTool()));
    public static final Block SKARO_ANDESITE = registerBlock("skaro_andesite",
            new Block(FabricBlockSettings.copyOf(Blocks.ANDESITE).requiresTool()));
    public static final Block SKARO_DIORITE = registerBlock("skaro_diorite",
            new Block(FabricBlockSettings.copyOf(Blocks.DIORITE).requiresTool()));
    public static final Block SKARO_GRANITE = registerBlock("skaro_granite",
            new Block(FabricBlockSettings.copyOf(Blocks.GRANITE).requiresTool()));
    public static final Block SKARO_POLISHED_ANDESITE = registerBlock("skaro_polished_andesite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_ANDESITE).requiresTool()));
    public static final Block SKARO_POLISHED_DIORITE = registerBlock("skaro_polished_diorite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_DIORITE).requiresTool()));
    public static final Block SKARO_POLISHED_GRANITE = registerBlock("skaro_polished_granite",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_GRANITE).requiresTool()));
    public static final Block POLISHED_SKARO_STONE = registerBlock("polished_skaro_stone",
            new Block(FabricBlockSettings.copyOf(Blocks.POLISHED_GRANITE).requiresTool()));
    public static final Block SKARO_DEEPSLATE = registerBlock("skaro_deepslate",
            new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE).requiresTool()));
    public static final Block SKARO_COBBLED_DEEPSLATE = registerBlock("skaro_cobbled_deepslate",
            new Block(FabricBlockSettings.copyOf(Blocks.COBBLED_DEEPSLATE).requiresTool()));
    public static final Block SKARO_DEEPSLATE_TILES = registerBlock("skaro_deepslate_tiles",
            new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_TILES).requiresTool()));

    public static final Block SKARO_STONE_STAIRS = registerBlock("skaro_stone_stairs",
            new StairsBlock(SKARO_STONE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_STONE)));
    public static final Block SKARO_STONE_SLAB = registerBlock("skaro_stone_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_STONE)));
    public static final Block SKARO_COBBLESTONE_STAIRS = registerBlock("skaro_cobblestone_stairs",
            new StairsBlock(SKARO_COBBLESTONE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_COBBLESTONE)));
    public static final Block SKARO_COBBLESTONE_SLAB = registerBlock("skaro_cobblestone_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_COBBLESTONE)));
    public static final Block SKARO_ANDESITE_STAIRS = registerBlock("skaro_andesite_stairs",
            new StairsBlock(SKARO_ANDESITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_ANDESITE)));
    public static final Block SKARO_ANDESITE_SLAB = registerBlock("skaro_andesite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_ANDESITE)));
    public static final Block SKARO_DIORITE_STAIRS = registerBlock("skaro_diorite_stairs",
            new StairsBlock(SKARO_DIORITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_DIORITE)));
    public static final Block SKARO_DIORITE_SLAB = registerBlock("skaro_diorite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_DIORITE)));
    public static final Block SKARO_GRANITE_STAIRS = registerBlock("skaro_granite_stairs",
            new StairsBlock(SKARO_GRANITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_GRANITE)));
    public static final Block SKARO_GRANITE_SLAB = registerBlock("skaro_granite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_GRANITE)));
    public static final Block SKARO_POLISHED_ANDESITE_STAIRS = registerBlock("skaro_polished_andesite_stairs",
            new StairsBlock(SKARO_POLISHED_ANDESITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_POLISHED_ANDESITE)));
    public static final Block SKARO_POLISHED_ANDESITE_SLAB = registerBlock("skaro_polished_andesite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_ANDESITE)));
    public static final Block SKARO_POLISHED_DIORITE_STAIRS = registerBlock("skaro_polished_diorite_stairs",
            new StairsBlock(SKARO_POLISHED_DIORITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_POLISHED_DIORITE)));
    public static final Block SKARO_POLISHED_DIORITE_SLAB = registerBlock("skaro_polished_diorite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_DIORITE)));
    public static final Block SKARO_POLISHED_GRANITE_STAIRS = registerBlock("skaro_polished_granite_stairs",
            new StairsBlock(SKARO_POLISHED_GRANITE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_POLISHED_GRANITE)));
    public static final Block SKARO_POLISHED_GRANITE_SLAB = registerBlock("skaro_polished_granite_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_GRANITE)));
    public static final Block POLISHED_SKARO_STONE_STAIRS = registerBlock("polished_skaro_stone_stairs",
            new StairsBlock(POLISHED_SKARO_STONE.getDefaultState(), FabricBlockSettings.copyOf(POLISHED_SKARO_STONE)));
    public static final Block POLISHED_SKARO_STONE_SLAB = registerBlock("polished_skaro_stone_slab",
            new SlabBlock(FabricBlockSettings.copyOf(POLISHED_SKARO_STONE)));
    public static final Block SKARO_DEEPSLATE_STAIRS = registerBlock("skaro_deepslate_stairs",
            new StairsBlock(SKARO_DEEPSLATE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_DEEPSLATE)));
    public static final Block SKARO_DEEPSLATE_SLAB = registerBlock("skaro_deepslate_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_DEEPSLATE)));
    public static final Block SKARO_COBBLED_DEEPSLATE_STAIRS = registerBlock("skaro_cobbled_deepslate_stairs",
            new StairsBlock(SKARO_COBBLED_DEEPSLATE.getDefaultState(), FabricBlockSettings.copyOf(SKARO_COBBLED_DEEPSLATE)));
    public static final Block SKARO_COBBLED_DEEPSLATE_SLAB = registerBlock("skaro_cobbled_deepslate_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_COBBLED_DEEPSLATE)));
    public static final Block SKARO_DEEPSLATE_TILES_STAIRS = registerBlock("skaro_deepslate_tiles_stairs",
            new StairsBlock(SKARO_DEEPSLATE_TILES.getDefaultState(), FabricBlockSettings.copyOf(SKARO_DEEPSLATE_TILES)));
    public static final Block SKARO_DEEPSLATE_TILES_SLAB = registerBlock("skaro_deepslate_tiles_slab",
            new SlabBlock(FabricBlockSettings.copyOf(SKARO_DEEPSLATE_TILES)));

    // Skaro walls, one for every Skaro stone type.
    public static final Block SKARO_STONE_WALL = registerBlock("skaro_stone_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_STONE)));
    public static final Block SKARO_COBBLESTONE_WALL = registerBlock("skaro_cobblestone_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_COBBLESTONE)));
    public static final Block SKARO_ANDESITE_WALL = registerBlock("skaro_andesite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_ANDESITE)));
    public static final Block SKARO_DIORITE_WALL = registerBlock("skaro_diorite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_DIORITE)));
    public static final Block SKARO_GRANITE_WALL = registerBlock("skaro_granite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_GRANITE)));
    public static final Block SKARO_POLISHED_ANDESITE_WALL = registerBlock("skaro_polished_andesite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_ANDESITE)));
    public static final Block SKARO_POLISHED_DIORITE_WALL = registerBlock("skaro_polished_diorite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_DIORITE)));
    public static final Block SKARO_POLISHED_GRANITE_WALL = registerBlock("skaro_polished_granite_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_POLISHED_GRANITE)));
    public static final Block POLISHED_SKARO_STONE_WALL = registerBlock("polished_skaro_stone_wall",
            new WallBlock(FabricBlockSettings.copyOf(POLISHED_SKARO_STONE)));
    public static final Block SKARO_DEEPSLATE_WALL = registerBlock("skaro_deepslate_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_DEEPSLATE)));
    public static final Block SKARO_COBBLED_DEEPSLATE_WALL = registerBlock("skaro_cobbled_deepslate_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_COBBLED_DEEPSLATE)));
    public static final Block SKARO_DEEPSLATE_TILES_WALL = registerBlock("skaro_deepslate_tiles_wall",
            new WallBlock(FabricBlockSettings.copyOf(SKARO_DEEPSLATE_TILES)));

    // Kept for existing Skaro architecture; the old Kaletite stone/cobble blocks are removed.
    public static final Block KALETITE_BRICKS = registerBlock("kaletite_bricks",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE).sounds(BlockSoundGroup.STONE).requiresTool()));

    public static final Block WASTED_DIRT = registerBlock("wasted_dirt",
            new Block(FabricBlockSettings.copyOf(Blocks.DIRT).sounds(BlockSoundGroup.ROOTED_DIRT)));

    public static final Block WASTED_GRASS = registerBlock("wastedgrass",
            new SpreadingGrassBlock(() -> GallifreyModBlocks.WASTED_DIRT, () -> java.util.List.of(Blocks.GRASS),
                    FabricBlockSettings.copyOf(Blocks.GRASS_BLOCK).sounds(BlockSoundGroup.ROOTED_DIRT)));

    // ============================================================
    // REALITY BLOCKS (Lost Reality)
    // ============================================================
    /** Placeholder block so the lost_dirt texture has something to live on. Appears in patches in Lost Reality. */
    public static final Block LOST_DIRT = registerBlock("lost_dirt",
            new Block(FabricBlockSettings.copyOf(Blocks.DIRT)));

    public static void register() {
        GallifreyMod.LOGGER.info(
                "Registering ModBlocks for " + GallifreyMod.MOD_ID
        );

        registerFlammable();
    }


    // ============================================================
    // FLAMMABLE BLOCKS
    // ============================================================

    private static void registerFlammable() {

        FlammableBlockRegistry flammable =
                FlammableBlockRegistry.getDefaultInstance();


        // Classic wood set
        flammable.add(CLASSIC_LOG, 5, 5);
        flammable.add(CLASSIC_PLANKS, 5, 20);
        flammable.add(CLASSIC_STAIRS, 5, 20);
        flammable.add(CLASSIC_SLAB, 5, 20);
        flammable.add(CLASSIC_FENCE, 5, 20);
        flammable.add(CLASSIC_FENCE_GATE, 5, 20);
        flammable.add(CLASSIC_LEAVES, 30, 60);

        // TARDIS wood set
        flammable.add(TARDIS_LOG, 5, 5);
        flammable.add(STRIP_TARDIS_LOG, 5, 5);
        flammable.add(TARDIS_WOOD, 5, 5);
        flammable.add(STRIP_TARDIS_WOOD, 5, 5);
        flammable.add(TARDIS_PLANKS, 5, 20);
        flammable.add(TARDIS_LEAVES, 30, 60);
        flammable.add(TARDIS_STAIRS, 5, 20);
        flammable.add(TARDIS_SLAB, 5, 20);
        flammable.add(TARDIS_FENCE, 5, 20);
        flammable.add(TARDIS_FENCE_GATE, 5, 20);

        // Ulanda wood set
        flammable.add(ULANDA_LOG, 5, 5);
        flammable.add(STRIP_ULANDA_LOG, 5, 5);
        flammable.add(ULANDA_WOOD, 5, 5);
        flammable.add(STRIP_ULANDA_WOOD, 5, 5);
        flammable.add(ULANDA_PLANKS, 5, 20);
        flammable.add(ULANDA_LEAVES, 30, 60);
        flammable.add(ULANDA_STAIRS, 5, 20);
        flammable.add(ULANDA_SLAB, 5, 20);
        flammable.add(ULANDA_FENCE, 5, 20);
        flammable.add(ULANDA_FENCE_GATE, 5, 20);

        // Tree-borg wood set
        flammable.add(TREEBORG_LOG, 5, 5);
        flammable.add(STRIP_TREEBORG_LOG, 5, 5);
        flammable.add(TREEBORG_WOOD, 5, 5);
        flammable.add(STRIP_TREEBORG_WOOD, 5, 5);
        flammable.add(TREEBORG_PLANKS, 5, 20);
        flammable.add(TREEBORG_LEAVES, 30, 60);
        flammable.add(TREEBORG_STAIRS, 5, 20);
        flammable.add(TREEBORG_SLAB, 5, 20);
        flammable.add(TREEBORG_FENCE, 5, 20);
        flammable.add(TREEBORG_FENCE_GATE, 5, 20);

        // Skaro wasted wood set
        flammable.add(WASTED_LOG, 5, 5);
        flammable.add(WASTED_WOOD, 5, 5);
        flammable.add(WASTED_PLANKS, 5, 20);
        flammable.add(WASTED_LEAVES, 30, 60);
        flammable.add(WASTED_STAIRS, 5, 20);
        flammable.add(WASTED_SLAB, 5, 20);
        flammable.add(WASTED_FENCE, 5, 20);
        flammable.add(WASTED_FENCE_GATE, 5, 20);

        // Ash wood set
        flammable.add(ASH_LOG, 5, 5);
        flammable.add(STRIP_ASH_LOG, 5, 5);
        flammable.add(ASH_WOOD, 5, 5);
        flammable.add(STRIP_ASH_WOOD, 5, 5);
        flammable.add(ASH_PLANKS, 5, 20);
        flammable.add(ASH_LEAVES, 30, 60);
        flammable.add(ASH_STAIRS, 5, 20);
        flammable.add(ASH_SLAB, 5, 20);
        flammable.add(ASH_FENCE, 5, 20);
        flammable.add(ASH_FENCE_GATE, 5, 20);

        // Maple wood set
        flammable.add(MAPLE_LOG, 5, 5);
        flammable.add(STRIP_MAPLE_LOG, 5, 5);
        flammable.add(MAPLE_WOOD, 5, 5);
        flammable.add(STRIP_MAPLE_WOOD, 5, 5);
        flammable.add(MAPLE_PLANKS, 5, 20);
        flammable.add(MAPLE_LEAVES, 30, 60);
        flammable.add(MAPLE_STAIRS, 5, 20);
        flammable.add(MAPLE_SLAB, 5, 20);
        flammable.add(MAPLE_FENCE, 5, 20);
        flammable.add(MAPLE_FENCE_GATE, 5, 20);

        // Prehistoric wood set
        flammable.add(PREHISTORIC_LOG, 5, 5);
        flammable.add(PREHISTORIC_PLANKS, 5, 20);
        flammable.add(PREHISTORIC_LEAVES, 30, 60);
        flammable.add(PREHISTORIC_VINE, 15, 100);
        flammable.add(PREHISTORIC_STAIRS, 5, 20);
        flammable.add(PREHISTORIC_SLAB, 5, 20);
        flammable.add(PREHISTORIC_FENCE, 5, 20);
        flammable.add(PREHISTORIC_FENCE_GATE, 5, 20);

        // Moonpine wood set
        flammable.add(MOONPINE_LOG, 5, 5);
        flammable.add(STRIP_MOONPINE_LOG, 5, 5);
        flammable.add(MOONPINE_WOOD, 5, 5);
        flammable.add(STRIP_MOONPINE_WOOD, 5, 5);
        flammable.add(MOONPINE_PLANKS, 5, 20);
        flammable.add(MOONPINE_LEAVES, 30, 60);
        flammable.add(MOONPINE_STAIRS, 5, 20);
        flammable.add(MOONPINE_SLAB, 5, 20);
        flammable.add(MOONPINE_FENCE, 5, 20);
        flammable.add(MOONPINE_FENCE_GATE, 5, 20);

    }
}