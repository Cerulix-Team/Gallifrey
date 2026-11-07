package com.cerulixteam.gallifrey.block.backport;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Oxidizable.OxidationLevel;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The rest of the 1.21 to 26.3 backports that aren't blocks of a set: trims, banner patterns, pottery sherds,
 * paintings, the four new potion effects, Copper Horse Armor, the Ominous Bottle, bundles, shelves, copper
 * chests, the Crafter, the Dried Ghast and Creaking Heart blocks, and falling-leaf and firefly particles.
 */
public final class BackportExtras {
    private BackportExtras() {}

    private static final String MOD_ID = "gallifrey";

    private static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, id(name), item);
        BackportBlocks.TAB.add(item);
        return item;
    }

    // =========================================================
    // PARTICLES
    // =========================================================

    public static final DefaultParticleType PALE_OAK_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("pale_oak_leaves"), FabricParticleTypes.simple());
    public static final DefaultParticleType RED_POPLAR_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("red_poplar_leaves"), FabricParticleTypes.simple());
    public static final DefaultParticleType ORANGE_POPLAR_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("orange_poplar_leaves"), FabricParticleTypes.simple());
    public static final DefaultParticleType YELLOW_POPLAR_LEAVES_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("yellow_poplar_leaves"), FabricParticleTypes.simple());
    public static final DefaultParticleType FIREFLY_PARTICLE = Registry.register(Registries.PARTICLE_TYPE, id("firefly"), FabricParticleTypes.simple());

    // =========================================================
    // TRIMS, BANNER PATTERNS, SHERDS (1.21)
    // =========================================================

    public static final Item FLOW_TRIM = item("flow_armor_trim_smithing_template", SmithingTemplateItem.of(RegistryKey.of(RegistryKeys.TRIM_PATTERN, id("flow"))));
    public static final Item BOLT_TRIM = item("bolt_armor_trim_smithing_template", SmithingTemplateItem.of(RegistryKey.of(RegistryKeys.TRIM_PATTERN, id("bolt"))));

    /** Banner patterns. The short ids go in banner NBT, so they're prefixed to stay clear of other mods. */
    public static final RegistryKey<BannerPattern> FLOW_PATTERN = bannerPattern("flow", "gallifrey_flo");
    public static final RegistryKey<BannerPattern> GUSTER_PATTERN = bannerPattern("guster", "gallifrey_gus");
    public static final Item FLOW_BANNER_PATTERN = item("flow_banner_pattern", new BannerPatternItem(TagKey.of(RegistryKeys.BANNER_PATTERN, id("pattern_item/flow")), new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item GUSTER_BANNER_PATTERN = item("guster_banner_pattern", new BannerPatternItem(TagKey.of(RegistryKeys.BANNER_PATTERN, id("pattern_item/guster")), new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)));

    /** Pottery sherds, and the pot patterns they make (see DecoratedPotPatternsMixin). */
    public static final Map<Item, RegistryKey<String>> SHERDS = new LinkedHashMap<>();
    public static final Item FLOW_SHERD = sherd("flow");
    public static final Item GUSTER_SHERD = sherd("guster");
    public static final Item SCRAPE_SHERD = sherd("scrape");

    private static RegistryKey<BannerPattern> bannerPattern(String name, String shortId) {
        RegistryKey<BannerPattern> key = RegistryKey.of(RegistryKeys.BANNER_PATTERN, id(name));
        Registry.register(Registries.BANNER_PATTERN, key, new BannerPattern(shortId));
        return key;
    }

    private static Item sherd(String name) {
        Item sherd = item(name + "_pottery_sherd", new Item(new FabricItemSettings()));
        RegistryKey<String> pattern = RegistryKey.of(RegistryKeys.DECORATED_POT_PATTERN, id(name + "_pottery_pattern"));
        Registry.register(Registries.DECORATED_POT_PATTERN, pattern, name + "_pottery_pattern");
        SHERDS.put(sherd, pattern);
        return sherd;
    }

    // =========================================================
    // PAINTINGS (1.21): name, width and height in blocks
    // =========================================================

    private static final Object[][] PAINTINGS = {
            {"backyard", 3, 4}, {"baroque", 2, 2}, {"bouquet", 3, 3}, {"cavebird", 3, 3}, {"changing", 4, 2}, {"cotan", 3, 3},
            {"dennis", 3, 3}, {"endboss", 3, 3}, {"fern", 3, 3}, {"finding", 4, 2}, {"humble", 2, 2}, {"lowmist", 4, 2},
            {"meditative", 1, 1}, {"orb", 4, 4}, {"owlemons", 3, 3}, {"passage", 4, 2}, {"pond", 3, 4}, {"prairie_ride", 1, 2},
            {"sunflowers", 3, 3}, {"tides", 3, 3}, {"unpacked", 4, 4}};

    static {
        for (Object[] p : PAINTINGS) {
            Registry.register(Registries.PAINTING_VARIANT, id((String) p[0]), new PaintingVariant((Integer) p[1] * 16, (Integer) p[2] * 16));
        }
    }

    // =========================================================
    // EFFECTS AND POTIONS (1.21)
    // =========================================================

    public static final StatusEffect INFESTED = Registry.register(Registries.STATUS_EFFECT, id("infested"), new BackportEffect(StatusEffectCategory.HARMFUL, 0x8C9B8C));
    public static final StatusEffect OOZING = Registry.register(Registries.STATUS_EFFECT, id("oozing"), new BackportEffect(StatusEffectCategory.HARMFUL, 0x99FFA3));
    public static final StatusEffect WEAVING = Registry.register(Registries.STATUS_EFFECT, id("weaving"), new BackportEffect(StatusEffectCategory.HARMFUL, 0x78695A));
    public static final StatusEffect WIND_CHARGED = Registry.register(Registries.STATUS_EFFECT, id("wind_charged"), new BackportEffect(StatusEffectCategory.HARMFUL, 0xBDC9FF));

    public static final Potion INFESTED_POTION = Registry.register(Registries.POTION, id("infested"), new Potion("infested", new StatusEffectInstance(INFESTED, 3600)));
    public static final Potion OOZING_POTION = Registry.register(Registries.POTION, id("oozing"), new Potion("oozing", new StatusEffectInstance(OOZING, 3600)));
    public static final Potion WEAVING_POTION = Registry.register(Registries.POTION, id("weaving"), new Potion("weaving", new StatusEffectInstance(WEAVING, 3600)));
    public static final Potion WIND_CHARGED_POTION = Registry.register(Registries.POTION, id("wind_charged"), new Potion("wind_charged", new StatusEffectInstance(WIND_CHARGED, 3600)));

    // =========================================================
    // ITEMS
    // =========================================================

    /** Copper Horse Armor. Its texture is minecraft:textures/entity/horse/armor/horse_armor_gallifrey_copper.png. */
    public static final Item COPPER_HORSE_ARMOR = item("copper_horse_armor", new HorseArmorItem(4, "gallifrey_copper", new FabricItemSettings().maxCount(1)));
    public static final Item OMINOUS_BOTTLE = item("ominous_bottle", new OminousBottleItem(new FabricItemSettings().rarity(Rarity.UNCOMMON)));

    /** Bundles (finished in 1.21.2): a plain one and one of every colour. */
    public static final Item BUNDLE = item("bundle", new BundleItem(new FabricItemSettings().maxCount(1)));
    public static final Item[] DYED_BUNDLES = new Item[16];

    static {
        for (DyeColor color : DyeColor.values()) {
            DYED_BUNDLES[color.getId()] = item(color.getName() + "_bundle", new BundleItem(new FabricItemSettings().maxCount(1)));
        }
    }

    // =========================================================
    // BLOCKS
    // =========================================================

    /** Shelves (1.21.9, and 26.3's poplar): one for every vanilla wood, plus pale oak and poplar. */
    public static final List<Block> SHELVES = new ArrayList<>();

    static {
        String[] woods = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped"};
        for (String wood : woods) {
            Block planks = Registries.BLOCK.get(new Identifier(wood + "_planks"));
            SHELVES.add(BackportBlocks.block(wood + "_shelf", new ShelfBlock(FabricBlockSettings.copyOf(planks).nonOpaque().strength(2.0F, 3.0F)),
                    true, BackportBlocks.CUTOUT));
        }
        SHELVES.add(BackportBlocks.block("pale_oak_shelf", new ShelfBlock(FabricBlockSettings.copyOf(BackportBlocks.PALE_OAK_PLANKS).nonOpaque().strength(2.0F, 3.0F)), true, BackportBlocks.CUTOUT));
        SHELVES.add(BackportBlocks.block("poplar_shelf", new ShelfBlock(FabricBlockSettings.copyOf(Backport26.POPLAR_PLANKS).nonOpaque().strength(2.0F, 3.0F)), true, BackportBlocks.CUTOUT));
    }

    public static final BlockEntityType<ShelfBlockEntity> SHELF_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("shelf"),
            FabricBlockEntityTypeBuilder.create(ShelfBlockEntity::new, SHELVES.toArray(new Block[0])).build());

    /** Copper chests: copper, exposed, weathered, oxidized, then the waxed ones. */
    public static final Block[] COPPER_CHESTS = new Block[8];

    static {
        String[] prefix = {"", "exposed_", "weathered_", "oxidized_"};
        Block[] base = {Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER};
        for (int waxed = 0; waxed < 2; waxed++) {
            for (int i = 0; i < 4; i++) {
                COPPER_CHESTS[waxed * 4 + i] = BackportBlocks.block((waxed == 1 ? "waxed_" : "") + prefix[i] + "copper_chest",
                        new CopperChestBlock(OxidationLevel.values()[i], waxed == 0, FabricBlockSettings.copyOf(base[i]).strength(3.0F, 6.0F)
                                .sounds(BlockSoundGroup.COPPER).nonOpaque()));
            }
        }
        for (int i = 0; i < 3; i++) OxidizableBlocksRegistry.registerOxidizableBlockPair(COPPER_CHESTS[i], COPPER_CHESTS[i + 1]);
        for (int i = 0; i < 4; i++) OxidizableBlocksRegistry.registerWaxableBlockPair(COPPER_CHESTS[i], COPPER_CHESTS[4 + i]);
    }

    public static final BlockEntityType<CopperChestBlockEntity> COPPER_CHEST_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("copper_chest"),
            FabricBlockEntityTypeBuilder.create(CopperChestBlockEntity::new, COPPER_CHESTS).build());

    public static final Block CRAFTER = BackportBlocks.block("crafter", new CrafterBlock(FabricBlockSettings.copyOf(Blocks.DISPENSER).strength(1.5F, 3.5F)));
    public static final BlockEntityType<CrafterBlockEntity> CRAFTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id("crafter"),
            FabricBlockEntityTypeBuilder.create(CrafterBlockEntity::new, CRAFTER).build());
    public static final ScreenHandlerType<CrafterScreenHandler> CRAFTER_SCREEN = Registry.register(Registries.SCREEN_HANDLER, id("crafter"),
            new ScreenHandlerType<>(CrafterScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final Block DRIED_GHAST = BackportBlocks.block("dried_ghast", new DriedGhastBlock(FabricBlockSettings.create().mapColor(MapColor.GRAY)
            .breakInstantly().sounds(BlockSoundGroup.SAND).nonOpaque().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)), true, BackportBlocks.CUTOUT);
    public static final Block CREAKING_HEART = BackportBlocks.block("creaking_heart", new CreakingHeartBlock(FabricBlockSettings.copyOf(BackportBlocks.PALE_OAK_LOG)
            .mapColor(MapColor.ORANGE).strength(10.0F).sounds(BlockSoundGroup.WOOD).ticksRandomly()));

    // =========================================================
    // SETUP
    // =========================================================

    private static final Identifier SIMPLE_DUNGEON = new Identifier("chests/simple_dungeon");
    private static final Identifier PILLAGER = new Identifier("entities/pillager");
    private static final Identifier[] ARCHAEOLOGY = {new Identifier("archaeology/desert_pyramid"), new Identifier("archaeology/trail_ruins_rare"),
            new Identifier("archaeology/ocean_ruin_warm")};

    public static void register() {
        BundleDyeRecipe.register();

        // Potions: an Awkward Potion plus stone, a slime block, cobweb or a breeze rod.
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(Items.STONE), INFESTED_POTION);
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(Items.SLIME_BLOCK), OOZING_POTION);
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(Items.COBWEB), WEAVING_POTION);
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Ingredient.ofItems(BackportCombat.BREEZE_ROD), WIND_CHARGED_POTION);

        // Infested: 10% chance per hit of 1 or 2 silverfish.
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.hasStatusEffect(INFESTED) && entity.getWorld() instanceof ServerWorld world && entity.getRandom().nextFloat() < 0.1F) {
                int count = 1 + entity.getRandom().nextInt(2);
                for (int i = 0; i < count; i++) {
                    SilverfishEntity silverfish = EntityType.SILVERFISH.create(world);
                    if (silverfish == null) continue;
                    silverfish.refreshPositionAndAngles(entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), entity.getRandom().nextFloat() * 360, 0);
                    world.spawnEntity(silverfish);
                }
            }
            return true;
        });
        // Oozing, Weaving, Wind Charged: something happens where the entity dies.
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity.getWorld() instanceof ServerWorld world)) return;
            if (entity.hasStatusEffect(OOZING)) {
                for (int i = 0; i < 2; i++) {
                    SlimeEntity slime = EntityType.SLIME.create(world);
                    if (slime == null) continue;
                    slime.setSize(2, true);
                    slime.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getRandom().nextFloat() * 360, 0);
                    world.spawnEntity(slime);
                }
            }
            if (entity.hasStatusEffect(WEAVING)) {
                int webs = 2 + entity.getRandom().nextInt(2);
                for (int i = 0; i < 12 && webs > 0; i++) {
                    BlockPos at = entity.getBlockPos().add(entity.getRandom().nextInt(3) - 1, entity.getRandom().nextInt(2), entity.getRandom().nextInt(3) - 1);
                    if (world.isAir(at)) {
                        world.setBlockState(at, Blocks.COBWEB.getDefaultState());
                        webs--;
                    }
                }
            }
            if (entity.hasStatusEffect(WIND_CHARGED)) {
                WindBurst.burst(world, entity.getPos().add(0, entity.getHeight() / 2, 0), 3.0, 1.2, null);
            }
        });

        // Things only found in Trial Chambers in vanilla turn up in other places here.
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin()) return;
            if (id.equals(SIMPLE_DUNGEON)) {
                tableBuilder.pool(LootPool.builder().conditionally(RandomChanceLootCondition.builder(0.12F))
                        .with(ItemEntry.builder(FLOW_TRIM)).with(ItemEntry.builder(BOLT_TRIM))
                        .with(ItemEntry.builder(FLOW_BANNER_PATTERN)).with(ItemEntry.builder(GUSTER_BANNER_PATTERN))
                        .with(ItemEntry.builder(BackportCombat.BREEZE_ROD)).with(ItemEntry.builder(BUNDLE))
                        .with(ItemEntry.builder(COPPER_HORSE_ARMOR)));
            } else if (id.equals(PILLAGER)) {
                NbtCompound amplifier = new NbtCompound();
                amplifier.putInt("Amplifier", 0);
                tableBuilder.pool(LootPool.builder().conditionally(RandomChanceLootCondition.builder(0.05F))
                        .with(ItemEntry.builder(OMINOUS_BOTTLE).apply(SetNbtLootFunction.builder(amplifier))));
            } else {
                for (Identifier table : ARCHAEOLOGY) {
                    if (id.equals(table)) {
                        tableBuilder.pool(LootPool.builder().conditionally(RandomChanceLootCondition.builder(0.15F))
                                .with(ItemEntry.builder(FLOW_SHERD)).with(ItemEntry.builder(GUSTER_SHERD)).with(ItemEntry.builder(SCRAPE_SHERD)));
                    }
                }
            }
        });
    }
}
