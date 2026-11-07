package com.cerulixteam.gallifrey.block.backport.mob;

import com.cerulixteam.gallifrey.block.backport.BackportBlocks;
import com.cerulixteam.gallifrey.block.backport.CreakingHeartBlock;
import com.cerulixteam.gallifrey.block.backport.DriedGhastBlock;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

/**
 * The backported mobs (1.20.5 to 26.3): Breeze, Bogged, Parched, Armadillo, Creaking, Happy Ghast, Copper
 * Golem, Nautilus, Zombie Nautilus, Camel Husk and Sulfur Cube, plus their items. Wolf armour, wolf
 * variants and the warm and cold cows, pigs and chickens are mixins (mixin/backport).
 *
 * The new mobs are drawn with GeckoLib from Mojang's Bedrock models (assets/gallifrey/geo/backport),
 * since Java's are code. The skeletons and the camel husk use Java's own models and textures.
 */
public final class BackportMobs {
    private BackportMobs() {}

    private static Identifier id(String path) {
        return new Identifier("gallifrey", path);
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, id(name), type);
    }

    public static final EntityType<BreezeEntity> BREEZE = register("breeze", FabricEntityTypeBuilder.<BreezeEntity>create(SpawnGroup.MONSTER, BreezeEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.77F)).trackRangeChunks(10).build());
    public static final EntityType<BackportSkeletonEntity> BOGGED = register("bogged", FabricEntityTypeBuilder.<BackportSkeletonEntity>create(SpawnGroup.MONSTER,
            (type, world) -> new BackportSkeletonEntity(type, world, BackportSkeletonEntity.Kind.BOGGED)).dimensions(EntityDimensions.fixed(0.6F, 1.99F)).trackRangeChunks(8).build());
    public static final EntityType<BackportSkeletonEntity> PARCHED = register("parched", FabricEntityTypeBuilder.<BackportSkeletonEntity>create(SpawnGroup.MONSTER,
            (type, world) -> new BackportSkeletonEntity(type, world, BackportSkeletonEntity.Kind.PARCHED)).dimensions(EntityDimensions.fixed(0.6F, 1.99F)).trackRangeChunks(8).build());
    public static final EntityType<ArmadilloEntity> ARMADILLO = register("armadillo", FabricEntityTypeBuilder.<ArmadilloEntity>create(SpawnGroup.CREATURE, ArmadilloEntity::new)
            .dimensions(EntityDimensions.fixed(0.7F, 0.65F)).trackRangeChunks(10).build());
    public static final EntityType<CreakingEntity> CREAKING = register("creaking", FabricEntityTypeBuilder.<CreakingEntity>create(SpawnGroup.MONSTER, CreakingEntity::new)
            .dimensions(EntityDimensions.fixed(0.9F, 2.7F)).trackRangeChunks(8).build());
    public static final EntityType<HappyGhastEntity> HAPPY_GHAST = register("happy_ghast", FabricEntityTypeBuilder.<HappyGhastEntity>create(SpawnGroup.CREATURE, HappyGhastEntity::new)
            .dimensions(EntityDimensions.fixed(4.0F, 4.0F)).trackRangeChunks(10).build());
    public static final EntityType<CopperGolemEntity> COPPER_GOLEM = register("copper_golem", FabricEntityTypeBuilder.<CopperGolemEntity>create(SpawnGroup.MISC, CopperGolemEntity::new)
            .dimensions(EntityDimensions.fixed(0.49F, 0.98F)).trackRangeChunks(10).build());
    public static final EntityType<NautilusEntity> NAUTILUS = register("nautilus", FabricEntityTypeBuilder.<NautilusEntity>create(SpawnGroup.WATER_CREATURE,
            (type, world) -> new NautilusEntity(type, world, false)).dimensions(EntityDimensions.fixed(0.875F, 0.95F)).trackRangeChunks(10).build());
    public static final EntityType<NautilusEntity> ZOMBIE_NAUTILUS = register("zombie_nautilus", FabricEntityTypeBuilder.<NautilusEntity>create(SpawnGroup.MONSTER,
            (type, world) -> new NautilusEntity(type, world, true)).dimensions(EntityDimensions.fixed(0.875F, 0.95F)).trackRangeChunks(10).build());
    public static final EntityType<CamelHuskEntity> CAMEL_HUSK = register("camel_husk", FabricEntityTypeBuilder.<CamelHuskEntity>create(SpawnGroup.MONSTER, CamelHuskEntity::new)
            .dimensions(EntityDimensions.fixed(1.7F, 2.375F)).trackRangeChunks(10).build());
    public static final EntityType<SulfurCubeEntity> SULFUR_CUBE = register("sulfur_cube", FabricEntityTypeBuilder.<SulfurCubeEntity>create(SpawnGroup.CREATURE, SulfurCubeEntity::new)
            .dimensions(EntityDimensions.changing(1.0F, 1.0F)).trackRangeChunks(10).build());
    public static final EntityType<VariantEggEntity> VARIANT_EGG = register("variant_egg", FabricEntityTypeBuilder.<VariantEggEntity>create(SpawnGroup.MISC, VariantEggEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F)).trackRangeChunks(4).trackedUpdateRate(10).build());

    // =========================================================
    // ITEMS
    // =========================================================

    private static Item item(String name, Item item) {
        Registry.register(Registries.ITEM, id(name), item);
        BackportBlocks.TAB.add(item);
        return item;
    }

    public static final Item ARMADILLO_SCUTE = item("armadillo_scute", new Item(new FabricItemSettings()));
    public static final Item WOLF_ARMOR = item("wolf_armor", new WolfArmorItem(new FabricItemSettings().maxDamage(64)));
    public static final Item[] HARNESSES = new Item[16];
    public static final Item[] NAUTILUS_ARMOR = new Item[5];

    static {
        for (DyeColor color : DyeColor.values()) {
            HARNESSES[color.getId()] = item(color.getName() + "_harness", new HarnessItem(color, new FabricItemSettings().maxCount(1)));
        }
        for (int i = 0; i < 5; i++) {
            NAUTILUS_ARMOR[i] = item(NautilusArmorItem.MATERIALS[i] + "_nautilus_armor", new NautilusArmorItem(i,
                    i == 4 ? new FabricItemSettings().maxCount(1).fireproof() : new FabricItemSettings().maxCount(1)));
        }
    }

    public static final Item SULFUR_CUBE_BUCKET = item("sulfur_cube_bucket", new SulfurCubeBucketItem(new FabricItemSettings().maxCount(1)));
    public static final Item BLUE_EGG = item("blue_egg", new VariantEggItem(2, new FabricItemSettings().maxCount(16)));
    public static final Item BROWN_EGG = item("brown_egg", new VariantEggItem(1, new FabricItemSettings().maxCount(16)));

    static {
        egg("breeze", BREEZE, 0xAF94DF, 0x9166DF);
        egg("bogged", BOGGED, 0x8A9C72, 0x314D1B);
        egg("parched", PARCHED, 0xD8C49A, 0x8C6A3F);
        egg("armadillo", ARMADILLO, 0xAD716D, 0x824848);
        egg("creaking", CREAKING, 0x5F5F5F, 0xFC7812);
        egg("happy_ghast", HAPPY_GHAST, 0xF9F9F9, 0xBCBCBC);
        egg("copper_golem", COPPER_GOLEM, 0xC15A36, 0x5D9A85);
        egg("nautilus", NAUTILUS, 0xE6D7B3, 0x8F5C3B);
        egg("zombie_nautilus", ZOMBIE_NAUTILUS, 0x6E8A6A, 0x3B4F3A);
        egg("camel_husk", CAMEL_HUSK, 0xB59E73, 0x6E5A3B);
        egg("sulfur_cube", SULFUR_CUBE, 0xE3D35C, 0x9E8E2B);
    }

    private static void egg(String name, EntityType<? extends MobEntity> type, int primary, int secondary) {
        item(name + "_spawn_egg", new BackportSpawnEggItem(type, primary, secondary, new FabricItemSettings()));
    }

    // =========================================================
    // SETUP
    // =========================================================

    public static void register() {
        FabricDefaultAttributeRegistry.register(BREEZE, BreezeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(BOGGED, BackportSkeletonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(PARCHED, BackportSkeletonEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ARMADILLO, ArmadilloEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CREAKING, CreakingEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HAPPY_GHAST, HappyGhastEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(COPPER_GOLEM, CopperGolemEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(NAUTILUS, NautilusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ZOMBIE_NAUTILUS, NautilusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CAMEL_HUSK, CamelEntity.createCamelAttributes());
        FabricDefaultAttributeRegistry.register(SULFUR_CUBE, SulfurCubeEntity.createAttributes());

        SpawnRestriction.register(BOGGED, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(PARCHED, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(CAMEL_HUSK, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && HostileEntity.isSpawnDark(world, pos, random) && MobEntity.canMobSpawn(type, world, reason, pos, random));
        SpawnRestriction.register(ARMADILLO, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getBlockState(pos.down()).isIn(BlockTags.ANIMALS_SPAWNABLE_ON)
                        || world.getBlockState(pos.down()).isIn(BlockTags.SAND) || world.getBlockState(pos.down()).isIn(BlockTags.TERRACOTTA));
        SpawnRestriction.register(NAUTILUS, SpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getFluidState(pos).isIn(net.minecraft.registry.tag.FluidTags.WATER));
        SpawnRestriction.register(ZOMBIE_NAUTILUS, SpawnRestriction.Location.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getFluidState(pos).isIn(net.minecraft.registry.tag.FluidTags.WATER)
                        && HostileEntity.isSpawnDark(world, pos, random));
        SpawnRestriction.register(SULFUR_CUBE, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);

        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP), SpawnGroup.MONSTER, BOGGED, 40, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DESERT), SpawnGroup.MONSTER, PARCHED, 25, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DESERT), SpawnGroup.MONSTER, CAMEL_HUSK, 2, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SAVANNA, BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.BADLANDS, BiomeKeys.WOODED_BADLANDS),
                SpawnGroup.CREATURE, ARMADILLO, 10, 1, 2);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN),
                SpawnGroup.WATER_CREATURE, NAUTILUS, 2, 1, 1);
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.WARM_OCEAN, BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeKeys.OCEAN, BiomeKeys.DEEP_OCEAN),
                SpawnGroup.MONSTER, ZOMBIE_NAUTILUS, 1, 1, 1);

        DriedGhastBlock.hatch = HappyGhastEntity::hatch;
        CreakingHeartBlock.spawnCreaking = CreakingEntity::spawnFor;
    }
}
