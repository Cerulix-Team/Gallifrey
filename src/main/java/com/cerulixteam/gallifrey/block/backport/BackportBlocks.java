package com.cerulixteam.gallifrey.block.backport;

import com.terraformersmc.terraform.sign.block.TerraformHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallSignBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Blocks from Minecraft 1.20.2 to 1.21.11, backported into Gallifrey so it doesn't need the
 * Copper &amp; Tuff Backport mod any more. They're in the "Backported" section of the Gallifrey tab.
 *
 * <ul>
 *   <li>Tuff: tuff, polished tuff and tuff brick slabs, stairs and walls, and the chiseled blocks (1.21)</li>
 *   <li>Copper: chiseled copper, grates, bulbs, doors and trapdoors (1.21), and bars, chains,
 *   lanterns and torches (1.21.9), each with every rust stage and waxed version</li>
 *   <li>The Pale Garden: the Pale Oak wood set, pale moss, hanging moss and eyeblossoms (1.21.4)</li>
 *   <li>Resin: resin clumps, the resin block and resin bricks (1.21.4)</li>
 *   <li>Plants: leaf litter, wildflowers, bushes, firefly bushes, cactus flowers and dry grass (1.21.5)</li>
 *   <li>The Heavy Core (1.21), as a decorative block</li>
 * </ul>
 *
 * The ids are the same as vanilla's, in the gallifrey namespace (gallifrey:tuff_bricks), so they match
 * Copper &amp; Tuff Backport's ids too. Models, blockstates and textures are vanilla 1.21.11's, under
 * models/block/backport and textures/block/backport. Tags live in the built-in "backported" data pack,
 * so they can't clash with the datagen tag files.
 */
public final class BackportBlocks {
    private BackportBlocks() {}

    private static final String MOD_ID = "gallifrey";

    /** Creative tab order for the Backported section. */
    public static final List<ItemConvertible> TAB = new ArrayList<>();
    /** Render layers and colours, read by the client. */
    public static final List<Block> CUTOUT = new ArrayList<>();
    public static final List<Block> CUTOUT_MIPPED = new ArrayList<>();
    public static final List<Block> GRASS_TINTED = new ArrayList<>();

    private static final OxidationLevel[] LEVELS = OxidationLevel.values();
    private static final String[] PREFIX = {"", "exposed_", "weathered_", "oxidized_"};
    private static final Block[] COPPER_BASE = {Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER};
    private static final int[] BULB_LIGHT = {15, 12, 8, 4};

    /** Copper doors and trapdoors open by hand, like in 1.21. (1.20.1 has no copper door sounds, so they use iron's.) */
    public static final BlockSetType COPPER_SET = new BlockSetTypeBuilder()
            .openableByHand(true)
            .soundGroup(BlockSoundGroup.COPPER)
            .doorOpenSound(SoundEvents.BLOCK_IRON_DOOR_OPEN)
            .doorCloseSound(SoundEvents.BLOCK_IRON_DOOR_CLOSE)
            .trapdoorOpenSound(SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN)
            .trapdoorCloseSound(SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE)
            .pressurePlateClickOnSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON)
            .pressurePlateClickOffSound(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF)
            .buttonClickOnSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON)
            .buttonClickOffSound(SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF)
            .register(new Identifier(MOD_ID, "copper"));

    // =========================================================
    // TUFF (1.21)
    // =========================================================

    public static final Block TUFF_SLAB = block("tuff_slab", new SlabBlock(tuff()));
    public static final Block TUFF_STAIRS = block("tuff_stairs", new StairsBlock(Blocks.TUFF.getDefaultState(), tuff()));
    public static final Block TUFF_WALL = block("tuff_wall", new WallBlock(tuff().solid()));
    public static final Block CHISELED_TUFF = block("chiseled_tuff", new Block(tuff()));
    public static final Block POLISHED_TUFF = block("polished_tuff", new Block(tuff()));
    public static final Block POLISHED_TUFF_SLAB = block("polished_tuff_slab", new SlabBlock(tuff()));
    public static final Block POLISHED_TUFF_STAIRS = block("polished_tuff_stairs", new StairsBlock(POLISHED_TUFF.getDefaultState(), tuff()));
    public static final Block POLISHED_TUFF_WALL = block("polished_tuff_wall", new WallBlock(tuff().solid()));
    public static final Block TUFF_BRICKS = block("tuff_bricks", new Block(tuff()));
    public static final Block TUFF_BRICK_SLAB = block("tuff_brick_slab", new SlabBlock(tuff()));
    public static final Block TUFF_BRICK_STAIRS = block("tuff_brick_stairs", new StairsBlock(TUFF_BRICKS.getDefaultState(), tuff()));
    public static final Block TUFF_BRICK_WALL = block("tuff_brick_wall", new WallBlock(tuff().solid()));
    public static final Block CHISELED_TUFF_BRICKS = block("chiseled_tuff_bricks", new Block(tuff()));

    // =========================================================
    // COPPER (1.21 and 1.21.9). Each array is: copper, exposed, weathered, oxidized,
    // then the waxed versions in the same order.
    // =========================================================

    public static final Block[] CHISELED_COPPER = copper("chiseled_copper",
            OxidizableBlock::new, Block::new,
            level -> FabricBlockSettings.copyOf(COPPER_BASE[level.ordinal()]));

    public static final Block[] COPPER_GRATE = copper("copper_grate",
            OxidizableCopperGrateBlock::new, CopperGrateBlock::new,
            level -> FabricBlockSettings.copyOf(COPPER_BASE[level.ordinal()]).sounds(BlockSoundGroup.COPPER).nonOpaque()
                    .allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never));

    public static final Block[] COPPER_BULB = copper("copper_bulb",
            OxidizableCopperBulbBlock::new, CopperBulbBlock::new,
            level -> FabricBlockSettings.copyOf(COPPER_BASE[level.ordinal()]).solidBlock(Blocks::never)
                    .luminance(state -> state.get(Properties.LIT) ? BULB_LIGHT[level.ordinal()] : 0));

    public static final Block[] COPPER_DOOR = copper("copper_door",
            (level, settings) -> new OxidizableCopperDoorBlock(level, settings, COPPER_SET),
            settings -> new CopperDoorBlock(settings, COPPER_SET),
            level -> FabricBlockSettings.copyOf(COPPER_BASE[level.ordinal()]).strength(3.0F, 6.0F).nonOpaque()
                    .pistonBehavior(PistonBehavior.DESTROY));

    public static final Block[] COPPER_TRAPDOOR = copper("copper_trapdoor",
            (level, settings) -> new OxidizableTrapdoorBlock(level, settings, COPPER_SET),
            settings -> new TrapdoorBlock(settings, COPPER_SET),
            level -> FabricBlockSettings.copyOf(COPPER_BASE[level.ordinal()]).strength(3.0F, 6.0F).nonOpaque()
                    .allowsSpawning(Blocks::never));

    public static final Block[] COPPER_BARS = copper("copper_bars",
            OxidizablePaneBlock::new, PaneBlock::new,
            level -> FabricBlockSettings.copyOf(Blocks.IRON_BARS).sounds(BlockSoundGroup.COPPER)
                    .mapColor(COPPER_BASE[level.ordinal()].getDefaultMapColor()));

    public static final Block[] COPPER_CHAIN = copper("copper_chain",
            OxidizableChainBlock::new, ChainBlock::new,
            level -> FabricBlockSettings.copyOf(Blocks.CHAIN).mapColor(COPPER_BASE[level.ordinal()].getDefaultMapColor()));

    public static final Block[] COPPER_LANTERN = copper("copper_lantern",
            OxidizableLanternBlock::new, LanternBlock::new,
            level -> FabricBlockSettings.copyOf(Blocks.LANTERN).mapColor(COPPER_BASE[level.ordinal()].getDefaultMapColor()));

    /** The torch's item is in {@link BackportItems}, since one item places both torches. */
    public static final Block COPPER_TORCH = block("copper_torch",
            new TorchBlock(FabricBlockSettings.copyOf(Blocks.TORCH), ParticleTypes.FLAME), false);
    public static final Block COPPER_WALL_TORCH = block("copper_wall_torch",
            new WallTorchBlock(FabricBlockSettings.copyOf(Blocks.WALL_TORCH).dropsLike(COPPER_TORCH), ParticleTypes.FLAME), false);

    // =========================================================
    // PALE GARDEN (1.21.4)
    // =========================================================

    public static final Block PALE_OAK_LOG = block("pale_oak_log", new PillarBlock(paleOak(Blocks.DARK_OAK_LOG)));
    public static final Block PALE_OAK_WOOD = block("pale_oak_wood", new PillarBlock(paleOak(Blocks.DARK_OAK_WOOD)));
    public static final Block STRIPPED_PALE_OAK_LOG = block("stripped_pale_oak_log", new PillarBlock(paleOak(Blocks.STRIPPED_DARK_OAK_LOG)));
    public static final Block STRIPPED_PALE_OAK_WOOD = block("stripped_pale_oak_wood", new PillarBlock(paleOak(Blocks.STRIPPED_DARK_OAK_WOOD)));
    public static final Block PALE_OAK_PLANKS = block("pale_oak_planks", new Block(paleOak(Blocks.DARK_OAK_PLANKS)));
    public static final Block PALE_OAK_STAIRS = block("pale_oak_stairs", new StairsBlock(PALE_OAK_PLANKS.getDefaultState(), paleOak(Blocks.DARK_OAK_STAIRS)));
    public static final Block PALE_OAK_SLAB = block("pale_oak_slab", new SlabBlock(paleOak(Blocks.DARK_OAK_SLAB)));
    public static final Block PALE_OAK_FENCE = block("pale_oak_fence", new FenceBlock(paleOak(Blocks.DARK_OAK_FENCE)));
    public static final Block PALE_OAK_FENCE_GATE = block("pale_oak_fence_gate", new FenceGateBlock(paleOak(Blocks.DARK_OAK_FENCE_GATE), WoodType.DARK_OAK));
    public static final Block PALE_OAK_DOOR = block("pale_oak_door", new DoorBlock(paleOak(Blocks.DARK_OAK_DOOR), BlockSetType.DARK_OAK), true, CUTOUT);
    public static final Block PALE_OAK_TRAPDOOR = block("pale_oak_trapdoor", new TrapdoorBlock(paleOak(Blocks.DARK_OAK_TRAPDOOR), BlockSetType.DARK_OAK), true, CUTOUT);
    public static final Block PALE_OAK_PRESSURE_PLATE = block("pale_oak_pressure_plate", new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, paleOak(Blocks.DARK_OAK_PRESSURE_PLATE), BlockSetType.DARK_OAK));
    public static final Block PALE_OAK_BUTTON = block("pale_oak_button", new ButtonBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_BUTTON), BlockSetType.DARK_OAK, 30, true));

    public static final Identifier PALE_OAK_SIGN_TEXTURE = new Identifier(MOD_ID, "entity/signs/pale_oak");
    public static final Identifier PALE_OAK_HANGING_SIGN_TEXTURE = new Identifier(MOD_ID, "entity/signs/hanging/pale_oak");
    public static final Identifier PALE_OAK_HANGING_GUI_SIGN_TEXTURE = new Identifier(MOD_ID, "textures/gui/hanging_signs/pale_oak");
    /** The sign items, and the boats, are in {@link BackportItems}. */
    public static final Block PALE_OAK_SIGN = block("pale_oak_standing_sign",
            new TerraformSignBlock(PALE_OAK_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_SIGN).mapColor(MapColor.OFF_WHITE)), false);
    public static final Block PALE_OAK_WALL_SIGN = block("pale_oak_wall_sign",
            new TerraformWallSignBlock(PALE_OAK_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_WALL_SIGN).mapColor(MapColor.OFF_WHITE).dropsLike(PALE_OAK_SIGN)), false);
    public static final Block PALE_OAK_HANGING_SIGN = block("pale_oak_hanging_sign",
            new TerraformHangingSignBlock(PALE_OAK_HANGING_SIGN_TEXTURE, PALE_OAK_HANGING_GUI_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_HANGING_SIGN).mapColor(MapColor.OFF_WHITE)), false);
    public static final Block PALE_OAK_WALL_HANGING_SIGN = block("pale_oak_wall_hanging_sign",
            new TerraformWallHangingSignBlock(PALE_OAK_HANGING_SIGN_TEXTURE, PALE_OAK_HANGING_GUI_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.DARK_OAK_WALL_HANGING_SIGN).mapColor(MapColor.OFF_WHITE).dropsLike(PALE_OAK_HANGING_SIGN)), false);

    public static final Block PALE_OAK_LEAVES = block("pale_oak_leaves", new LeavesBlock(FabricBlockSettings.copyOf(Blocks.DARK_OAK_LEAVES).mapColor(MapColor.PALE_GREEN)), true, CUTOUT_MIPPED);
    public static final Block PALE_OAK_SAPLING = block("pale_oak_sapling", new SaplingBlock(new PaleOakSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.DARK_OAK_SAPLING).mapColor(MapColor.OFF_WHITE)), true, CUTOUT);
    public static final Block POTTED_PALE_OAK_SAPLING = block("potted_pale_oak_sapling", new FlowerPotBlock(PALE_OAK_SAPLING, FabricBlockSettings.copyOf(Blocks.POTTED_DARK_OAK_SAPLING)), false, CUTOUT);

    public static final Block PALE_MOSS_BLOCK = block("pale_moss_block", new Block(FabricBlockSettings.copyOf(Blocks.MOSS_BLOCK).mapColor(MapColor.LIGHT_GRAY)));
    public static final Block PALE_MOSS_CARPET = block("pale_moss_carpet", new CarpetBlock(FabricBlockSettings.copyOf(Blocks.MOSS_CARPET).mapColor(MapColor.LIGHT_GRAY)));
    public static final Block PALE_HANGING_MOSS = block("pale_hanging_moss", new PaleHangingMossBlock(FabricBlockSettings.copyOf(Blocks.HANGING_ROOTS).offset(AbstractBlock.OffsetType.NONE).sounds(BlockSoundGroup.MOSS_CARPET).mapColor(MapColor.LIGHT_GRAY).ticksRandomly()), true, CUTOUT);

    public static final Block OPEN_EYEBLOSSOM = block("open_eyeblossom", new EyeblossomBlock(true, () -> BackportBlocks.CLOSED_EYEBLOSSOM, StatusEffects.BLINDNESS, 11,
            FabricBlockSettings.copyOf(Blocks.POPPY).ticksRandomly()), true, CUTOUT);
    public static final Block CLOSED_EYEBLOSSOM = block("closed_eyeblossom", new EyeblossomBlock(false, () -> BackportBlocks.OPEN_EYEBLOSSOM, StatusEffects.NAUSEA, 7,
            FabricBlockSettings.copyOf(Blocks.POPPY).ticksRandomly()), true, CUTOUT);
    public static final Block POTTED_OPEN_EYEBLOSSOM = block("potted_open_eyeblossom", new FlowerPotBlock(OPEN_EYEBLOSSOM, FabricBlockSettings.copyOf(Blocks.POTTED_POPPY)), false, CUTOUT);
    public static final Block POTTED_CLOSED_EYEBLOSSOM = block("potted_closed_eyeblossom", new FlowerPotBlock(CLOSED_EYEBLOSSOM, FabricBlockSettings.copyOf(Blocks.POTTED_POPPY)), false, CUTOUT);

    // =========================================================
    // RESIN (1.21.4)
    // =========================================================

    public static final Block RESIN_CLUMP = block("resin_clump", new ResinClumpBlock(FabricBlockSettings.copyOf(Blocks.GLOW_LICHEN).luminance(0).mapColor(MapColor.TERRACOTTA_ORANGE).sounds(BlockSoundGroup.HONEY)), true, CUTOUT);
    public static final Block RESIN_BLOCK = block("resin_block", new Block(FabricBlockSettings.create().mapColor(MapColor.TERRACOTTA_ORANGE).breakInstantly().sounds(BlockSoundGroup.MUD)));
    public static final Block RESIN_BRICKS = block("resin_bricks", new Block(resinBricks()));
    public static final Block RESIN_BRICK_SLAB = block("resin_brick_slab", new SlabBlock(resinBricks()));
    public static final Block RESIN_BRICK_STAIRS = block("resin_brick_stairs", new StairsBlock(RESIN_BRICKS.getDefaultState(), resinBricks()));
    public static final Block RESIN_BRICK_WALL = block("resin_brick_wall", new WallBlock(resinBricks().solid()));
    public static final Block CHISELED_RESIN_BRICKS = block("chiseled_resin_bricks", new Block(resinBricks()));

    // =========================================================
    // PLANTS (1.21.5)
    // =========================================================

    public static final Block LEAF_LITTER = block("leaf_litter", new LeafLitterBlock(FabricBlockSettings.copyOf(Blocks.PINK_PETALS).mapColor(MapColor.BROWN).sounds(BlockSoundGroup.AZALEA_LEAVES)), true, CUTOUT);
    public static final Block WILDFLOWERS = block("wildflowers", new FlowerbedBlock(FabricBlockSettings.copyOf(Blocks.PINK_PETALS).mapColor(MapColor.YELLOW)), true, CUTOUT);
    public static final Block BUSH = block("bush", new BackportPlantBlock(BackportPlantBlock.Soil.DIRT, BackportPlantBlock.box(1, 0, 1, 15, 13, 15),
            plant(MapColor.DARK_GREEN).replaceable()), true, CUTOUT);
    public static final Block FIREFLY_BUSH = block("firefly_bush", new BackportPlantBlock(BackportPlantBlock.Soil.DIRT, BackportPlantBlock.box(1, 0, 1, 15, 13, 15),
            plant(MapColor.DARK_GREEN).luminance(2)), true, CUTOUT);
    public static final Block CACTUS_FLOWER = block("cactus_flower", new BackportPlantBlock(BackportPlantBlock.Soil.CACTUS, BackportPlantBlock.box(1, 0, 1, 15, 12, 15),
            FabricBlockSettings.copyOf(Blocks.POPPY).mapColor(MapColor.PINK).sounds(BlockSoundGroup.WET_GRASS)), true, CUTOUT);
    public static final Block SHORT_DRY_GRASS = block("short_dry_grass", new BackportPlantBlock(BackportPlantBlock.Soil.DRY, BackportPlantBlock.box(2, 0, 2, 14, 10, 14),
            FabricBlockSettings.copyOf(Blocks.DEAD_BUSH).mapColor(MapColor.YELLOW).replaceable()), true, CUTOUT);
    public static final Block TALL_DRY_GRASS = block("tall_dry_grass", new BackportPlantBlock(BackportPlantBlock.Soil.DRY, BackportPlantBlock.box(1, 0, 1, 15, 16, 15),
            FabricBlockSettings.copyOf(Blocks.DEAD_BUSH).mapColor(MapColor.YELLOW).replaceable()), true, CUTOUT);

    // =========================================================
    // MISC
    // =========================================================

    public static final Block HEAVY_CORE = block("heavy_core", new HeavyCoreBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY)
            .strength(10.0F, 1200.0F).sounds(BlockSoundGroup.NETHERITE).nonOpaque().pistonBehavior(PistonBehavior.NORMAL)));

    static {
        GRASS_TINTED.add(BUSH);
        GRASS_TINTED.add(WILDFLOWERS);
        for (Block[] family : new Block[][]{COPPER_DOOR, COPPER_TRAPDOOR, COPPER_GRATE, COPPER_BARS, COPPER_CHAIN, COPPER_LANTERN}) {
            for (Block block : family) CUTOUT.add(block);
        }
        CUTOUT.add(COPPER_TORCH);
        CUTOUT.add(COPPER_WALL_TORCH);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static FabricBlockSettings tuff() {
        return FabricBlockSettings.copyOf(Blocks.TUFF);
    }

    private static FabricBlockSettings resinBricks() {
        return FabricBlockSettings.create().mapColor(MapColor.TERRACOTTA_ORANGE).requiresTool().strength(1.5F, 6.0F).sounds(BlockSoundGroup.MUD_BRICKS);
    }

    static FabricBlockSettings plant(MapColor color) {
        return FabricBlockSettings.create().mapColor(color).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS)
                .offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY);
    }

    private static FabricBlockSettings paleOak(Block like) {
        return FabricBlockSettings.copyOf(like).mapColor(MapColor.OFF_WHITE);
    }

    static Block block(String name, Block block) {
        return block(name, block, true);
    }

    static Block block(String name, Block block, boolean item) {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, name), block);
        if (item) {
            Item blockItem = Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), new BlockItem(block, new FabricItemSettings()));
            TAB.add(blockItem);
        }
        return block;
    }

    static Block block(String name, Block block, boolean item, List<Block> layer) {
        layer.add(block);
        return block(name, block, item);
    }

    /**
     * Registers the four rust stages and four waxed versions of one copper block, and tells Fabric
     * how they rust, scrape and wax into each other.
     */
    static Block[] copper(String base,
                                  BiFunction<OxidationLevel, AbstractBlock.Settings, Block> rusting,
                                  Function<AbstractBlock.Settings, Block> waxed,
                                  Function<OxidationLevel, FabricBlockSettings> settings) {
        Block[] out = new Block[8];
        for (int i = 0; i < 4; i++) {
            out[i] = block(PREFIX[i] + base, rusting.apply(LEVELS[i], settings.apply(LEVELS[i])));
        }
        for (int i = 0; i < 4; i++) {
            out[4 + i] = block("waxed_" + PREFIX[i] + base, waxed.apply(settings.apply(LEVELS[i])));
        }
        for (int i = 0; i < 3; i++) {
            OxidizableBlocksRegistry.registerOxidizableBlockPair(out[i], out[i + 1]);
        }
        for (int i = 0; i < 4; i++) {
            OxidizableBlocksRegistry.registerWaxableBlockPair(out[i], out[4 + i]);
        }
        return out;
    }

    /** Loads the class (and so registers everything). Called from GallifreyMod. */
    public static void register() {
    }
}
