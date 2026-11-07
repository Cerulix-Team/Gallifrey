package com.cerulixteam.gallifrey.block.backport;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;
import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;
import com.terraformersmc.terraform.sign.block.TerraformHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallSignBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BedItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import static com.cerulixteam.gallifrey.block.backport.BackportBlocks.CUTOUT;
import static com.cerulixteam.gallifrey.block.backport.BackportBlocks.CUTOUT_MIPPED;
import static com.cerulixteam.gallifrey.block.backport.BackportBlocks.block;

/**
 * Blocks and items from Minecraft 26.1 to 26.3, backported like the rest of {@link BackportBlocks}:
 * <ul>
 *   <li>26.1: the Golden Dandelion (feed it to a baby animal to keep it a baby; again to let it grow)</li>
 *   <li>26.2: Sulfur and Cinnabar stone sets, Potent Sulfur (gas and geysers) and Sulfur Spikes</li>
 *   <li>26.3: the Poplar wood set with red, orange and yellow leaves, Shelf Mushrooms, the Red Shrub,
 *   wool and concrete slabs and stairs in all 16 colours, Cushions to sit on, and the single-use Straw Bed</li>
 * </ul>
 * Not backported: the Sulfur Cube (a mob), the Dappled Forest and Sulfur Caves biomes, fallen trees and shelves.
 */
public final class Backport26 {
    private Backport26() {}

    private static final String MOD_ID = "gallifrey";

    // =========================================================
    // 26.1
    // =========================================================

    public static final Block GOLDEN_DANDELION = block("golden_dandelion",
            new FlowerBlock(StatusEffects.SATURATION, 7, FabricBlockSettings.copyOf(Blocks.DANDELION).mapColor(MapColor.GOLD)), true, CUTOUT);
    public static final Block POTTED_GOLDEN_DANDELION = block("potted_golden_dandelion",
            new FlowerPotBlock(GOLDEN_DANDELION, FabricBlockSettings.copyOf(Blocks.POTTED_DANDELION)), false, CUTOUT);

    // =========================================================
    // 26.2: SULFUR AND CINNABAR
    // =========================================================

    public static final Block[] SULFUR = stoneSet("sulfur", MapColor.YELLOW, BlockSoundGroup.TUFF);
    public static final Block[] CINNABAR = stoneSet("cinnabar", MapColor.RED, BlockSoundGroup.DEEPSLATE);
    public static final Block POTENT_SULFUR = block("potent_sulfur", new PotentSulfurBlock(FabricBlockSettings.copyOf(SULFUR[0]).luminance(3)));
    public static final Block SULFUR_SPIKE = block("sulfur_spike", new SulfurSpikeBlock(FabricBlockSettings.copyOf(Blocks.POINTED_DRIPSTONE)
            .mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.POINTED_DRIPSTONE)), true, CUTOUT);

    // =========================================================
    // 26.3: POPLAR
    // =========================================================

    public static final Block POPLAR_LOG = block("poplar_log", new PillarBlock(poplar(Blocks.BIRCH_LOG)));
    public static final Block POPLAR_WOOD = block("poplar_wood", new PillarBlock(poplar(Blocks.BIRCH_WOOD)));
    public static final Block STRIPPED_POPLAR_LOG = block("stripped_poplar_log", new PillarBlock(poplar(Blocks.STRIPPED_BIRCH_LOG)));
    public static final Block STRIPPED_POPLAR_WOOD = block("stripped_poplar_wood", new PillarBlock(poplar(Blocks.STRIPPED_BIRCH_WOOD)));
    public static final Block POPLAR_PLANKS = block("poplar_planks", new Block(poplar(Blocks.BIRCH_PLANKS)));
    public static final Block POPLAR_STAIRS = block("poplar_stairs", new StairsBlock(POPLAR_PLANKS.getDefaultState(), poplar(Blocks.BIRCH_STAIRS)));
    public static final Block POPLAR_SLAB = block("poplar_slab", new SlabBlock(poplar(Blocks.BIRCH_SLAB)));
    public static final Block POPLAR_FENCE = block("poplar_fence", new FenceBlock(poplar(Blocks.BIRCH_FENCE)));
    public static final Block POPLAR_FENCE_GATE = block("poplar_fence_gate", new FenceGateBlock(poplar(Blocks.BIRCH_FENCE_GATE), WoodType.BIRCH));
    public static final Block POPLAR_DOOR = block("poplar_door", new DoorBlock(poplar(Blocks.BIRCH_DOOR), BlockSetType.BIRCH), true, CUTOUT);
    public static final Block POPLAR_TRAPDOOR = block("poplar_trapdoor", new TrapdoorBlock(poplar(Blocks.BIRCH_TRAPDOOR), BlockSetType.BIRCH), true, CUTOUT);
    public static final Block POPLAR_PRESSURE_PLATE = block("poplar_pressure_plate", new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, poplar(Blocks.BIRCH_PRESSURE_PLATE), BlockSetType.BIRCH));
    public static final Block POPLAR_BUTTON = block("poplar_button", new ButtonBlock(FabricBlockSettings.copyOf(Blocks.BIRCH_BUTTON), BlockSetType.BIRCH, 30, true));

    public static final Identifier POPLAR_SIGN_TEXTURE = new Identifier(MOD_ID, "entity/signs/poplar");
    public static final Identifier POPLAR_HANGING_SIGN_TEXTURE = new Identifier(MOD_ID, "entity/signs/hanging/poplar");
    public static final Identifier POPLAR_HANGING_GUI_SIGN_TEXTURE = new Identifier(MOD_ID, "textures/gui/hanging_signs/poplar");
    public static final Block POPLAR_SIGN = block("poplar_standing_sign",
            new TerraformSignBlock(POPLAR_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.BIRCH_SIGN)), false);
    public static final Block POPLAR_WALL_SIGN = block("poplar_wall_sign",
            new TerraformWallSignBlock(POPLAR_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.BIRCH_WALL_SIGN).dropsLike(POPLAR_SIGN)), false);
    public static final Block POPLAR_HANGING_SIGN = block("poplar_hanging_sign",
            new TerraformHangingSignBlock(POPLAR_HANGING_SIGN_TEXTURE, POPLAR_HANGING_GUI_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.BIRCH_HANGING_SIGN)), false);
    public static final Block POPLAR_WALL_HANGING_SIGN = block("poplar_wall_hanging_sign",
            new TerraformWallHangingSignBlock(POPLAR_HANGING_SIGN_TEXTURE, POPLAR_HANGING_GUI_SIGN_TEXTURE, FabricBlockSettings.copyOf(Blocks.BIRCH_WALL_HANGING_SIGN).dropsLike(POPLAR_HANGING_SIGN)), false);

    public static final Block RED_POPLAR_LEAVES = block("red_poplar_leaves", new BackportLeavesBlock(() -> BackportExtras.RED_POPLAR_LEAVES_PARTICLE, 12, FabricBlockSettings.copyOf(Blocks.BIRCH_LEAVES).mapColor(MapColor.RED)), true, CUTOUT_MIPPED);
    public static final Block ORANGE_POPLAR_LEAVES = block("orange_poplar_leaves", new BackportLeavesBlock(() -> BackportExtras.ORANGE_POPLAR_LEAVES_PARTICLE, 12, FabricBlockSettings.copyOf(Blocks.BIRCH_LEAVES).mapColor(MapColor.ORANGE)), true, CUTOUT_MIPPED);
    public static final Block YELLOW_POPLAR_LEAVES = block("yellow_poplar_leaves", new BackportLeavesBlock(() -> BackportExtras.YELLOW_POPLAR_LEAVES_PARTICLE, 12, FabricBlockSettings.copyOf(Blocks.BIRCH_LEAVES).mapColor(MapColor.YELLOW)), true, CUTOUT_MIPPED);
    public static final Block POPLAR_SAPLING = block("poplar_sapling", new SaplingBlock(new PoplarSaplingGenerator(), FabricBlockSettings.copyOf(Blocks.BIRCH_SAPLING)), true, CUTOUT);
    public static final Block POTTED_POPLAR_SAPLING = block("potted_poplar_sapling", new FlowerPotBlock(POPLAR_SAPLING, FabricBlockSettings.copyOf(Blocks.POTTED_BIRCH_SAPLING)), false, CUTOUT);

    public static final Block SHELF_MUSHROOM = block("shelf_mushroom", new ShelfMushroomBlock(FabricBlockSettings.create().mapColor(MapColor.BROWN)
            .strength(0.5F).sounds(BlockSoundGroup.WOOD).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)), true, CUTOUT);
    public static final Block RED_SHRUB = block("red_shrub", new RedShrubBlock(BackportPlantBlock.box(2, 0, 2, 14, 13, 14),
            BackportBlocks.plant(MapColor.RED).replaceable()), true, CUTOUT);

    // =========================================================
    // 26.3: WOOL AND CONCRETE SLABS AND STAIRS, CUSHIONS, STRAW BED
    // =========================================================

    public static final Block[] WOOL_SLABS = new Block[16];
    public static final Block[] WOOL_STAIRS = new Block[16];
    public static final Block[] CONCRETE_SLABS = new Block[16];
    public static final Block[] CONCRETE_STAIRS = new Block[16];
    public static final Item[] CUSHIONS = new Item[16];

    static {
        for (DyeColor color : DyeColor.values()) {
            int i = color.getId();
            Block wool = Registries.BLOCK.get(new Identifier(color.getName() + "_wool"));
            WOOL_SLABS[i] = block(color.getName() + "_wool_slab", new SlabBlock(FabricBlockSettings.copyOf(wool)));
            WOOL_STAIRS[i] = block(color.getName() + "_wool_stairs", new StairsBlock(wool.getDefaultState(), FabricBlockSettings.copyOf(wool)));
        }
        for (DyeColor color : DyeColor.values()) {
            int i = color.getId();
            Block concrete = Registries.BLOCK.get(new Identifier(color.getName() + "_concrete"));
            CONCRETE_SLABS[i] = block(color.getName() + "_concrete_slab", new SlabBlock(FabricBlockSettings.copyOf(concrete)));
            CONCRETE_STAIRS[i] = block(color.getName() + "_concrete_stairs", new StairsBlock(concrete.getDefaultState(), FabricBlockSettings.copyOf(concrete)));
        }
    }

    public static final Block STRAW_BED = block("straw_bed", new StrawBedBlock(FabricBlockSettings.copyOf(Blocks.HAY_BLOCK).strength(0.2F)
            .nonOpaque().pistonBehavior(PistonBehavior.DESTROY)), false, CUTOUT);
    public static final Item STRAW_BED_ITEM = item("straw_bed", new BedItem(STRAW_BED, new FabricItemSettings().maxCount(1)));

    public static final EntityType<CushionEntity> CUSHION_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier(MOD_ID, "cushion"),
            FabricEntityTypeBuilder.<CushionEntity>create(SpawnGroup.MISC, CushionEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0F, 0.25F)).trackRangeChunks(10).trackedUpdateRate(20).build());

    static {
        for (DyeColor color : DyeColor.values()) {
            CUSHIONS[color.getId()] = item(color.getName() + "_cushion", new CushionItem(color, new FabricItemSettings()));
        }
    }

    // =========================================================
    // 26.3: POPLAR SIGNS AND BOATS
    // =========================================================

    public static final Item POPLAR_SIGN_ITEM = item("poplar_sign", new SignItem(new FabricItemSettings().maxCount(16), POPLAR_SIGN, POPLAR_WALL_SIGN));
    public static final Item POPLAR_HANGING_SIGN_ITEM = item("poplar_hanging_sign", new HangingSignItem(POPLAR_HANGING_SIGN, POPLAR_WALL_HANGING_SIGN, new FabricItemSettings().maxCount(16)));
    public static final Identifier POPLAR_BOAT_ID = new Identifier(MOD_ID, "poplar_boat");
    public static final Identifier POPLAR_CHEST_BOAT_ID = new Identifier(MOD_ID, "poplar_chest_boat");
    public static final RegistryKey<TerraformBoatType> POPLAR_BOAT_KEY = TerraformBoatTypeRegistry.createKey(POPLAR_BOAT_ID);
    public static final Item POPLAR_BOAT = TerraformBoatItemHelper.registerBoatItem(POPLAR_BOAT_ID, POPLAR_BOAT_KEY, false);
    public static final Item POPLAR_CHEST_BOAT = TerraformBoatItemHelper.registerBoatItem(POPLAR_CHEST_BOAT_ID, POPLAR_BOAT_KEY, true);

    public static final TreeDecoratorType<ShelfMushroomTreeDecorator> SHELF_MUSHROOM_DECORATOR = Registry.register(Registries.TREE_DECORATOR_TYPE,
            new Identifier(MOD_ID, "shelf_mushroom"), new TreeDecoratorType<>(ShelfMushroomTreeDecorator.CODEC));

    // =========================================================
    // HELPERS
    // =========================================================

    /** A 26.2 stone set: the stone, polished, bricks, their slabs, stairs and walls, and chiseled. Same order as the ids below. */
    private static Block[] stoneSet(String name, MapColor color, BlockSoundGroup sounds) {
        String[] ids = {name, name + "_slab", name + "_stairs", name + "_wall",
                "polished_" + name, "polished_" + name + "_slab", "polished_" + name + "_stairs", "polished_" + name + "_wall",
                name + "_bricks", name + "_brick_slab", name + "_brick_stairs", name + "_brick_wall", "chiseled_" + name};
        Block[] out = new Block[ids.length];
        for (int i = 0; i < ids.length; i++) {
            FabricBlockSettings settings = FabricBlockSettings.create().mapColor(color).requiresTool().strength(1.5F, 6.0F).sounds(sounds);
            Block base = i < 4 ? out[0] : i < 8 ? out[4] : out[8];
            Block block = switch (i % 4 == 0 || i == 12 ? -1 : i % 4) {
                case 1 -> new SlabBlock(settings);
                case 2 -> new StairsBlock(base.getDefaultState(), settings);
                case 3 -> new WallBlock(settings.solid());
                default -> new Block(settings);
            };
            out[i] = block(ids[i], block);
        }
        return out;
    }

    private static FabricBlockSettings poplar(Block like) {
        return FabricBlockSettings.copyOf(like).mapColor(MapColor.PALE_YELLOW);
    }

    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
        BackportBlocks.TAB.add(item);
        return item;
    }

    static void registerBoat() {
        TerraformBoatType poplar = new TerraformBoatType.Builder()
                .item(POPLAR_BOAT).chestItem(POPLAR_CHEST_BOAT).planks(POPLAR_PLANKS.asItem()).build();
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, POPLAR_BOAT_KEY, poplar);
    }

    public static void register() {
    }
}
