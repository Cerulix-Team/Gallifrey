package com.cerulixteam.gallifrey.entity;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.LaserEntity;
import com.cerulixteam.gallifrey.entity.custom.DiamondMinecartEntity;
import com.cerulixteam.gallifrey.entity.custom.ClassicVillagerEntity;
import com.cerulixteam.gallifrey.entity.custom.TimeLordVillagerEntity;
import com.cerulixteam.gallifrey.entity.custom.SkaroCityDalekEntity;
import com.cerulixteam.gallifrey.entity.custom.SkaroCityDalekAltEntity;
import com.cerulixteam.gallifrey.entity.custom.SupremeCouncilDalekEntity;
import com.cerulixteam.gallifrey.entity.custom.AlphaGhastEntity;
import com.cerulixteam.gallifrey.entity.custom.AlphaZombiePigmanEntity;
import com.cerulixteam.gallifrey.entity.custom.TimeLordEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GallifreyEntities {

    /** Separate modern-model Time Lord villager; does not replace or alter vanilla villagers. */
    public static final EntityType<VillagerEntity> TIMELORD_VILLAGER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "timelord_villager"),
            FabricEntityTypeBuilder.<VillagerEntity>create(SpawnGroup.CREATURE, TimeLordVillagerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeChunks(10)
                    .build());

    public static final EntityType<ClassicVillagerEntity> CLASSIC_VILLAGER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "classic_villager"),
            FabricEntityTypeBuilder.<ClassicVillagerEntity>create(SpawnGroup.MISC, ClassicVillagerEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeChunks(10)
                    .build());

    public static final EntityType<DiamondMinecartEntity> DIAMOND_MINECART = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "diamond_minecart"),
            FabricEntityTypeBuilder.<DiamondMinecartEntity>create(SpawnGroup.MISC, DiamondMinecartEntity::new)
                    .dimensions(EntityDimensions.fixed(0.98f, 0.7f))
                    .trackRangeChunks(8)   // same as vanilla minecarts (8 chunks, not 8 blocks)
                    .trackedUpdateRate(3)
                    .build());

    public static final EntityType<AlphaGhastEntity> ALPHA_GHAST = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "alpha_ghast"),
            FabricEntityTypeBuilder.<AlphaGhastEntity>create(SpawnGroup.MONSTER, AlphaGhastEntity::new)
                    .dimensions(EntityDimensions.fixed(4.0f, 4.0f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(3)
                    .build());

    public static final EntityType<AlphaZombiePigmanEntity> ALPHA_ZOMBIE_PIGMAN = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "alpha_zombie_pigman"),
            FabricEntityTypeBuilder.<AlphaZombiePigmanEntity>create(SpawnGroup.MONSTER, AlphaZombiePigmanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .trackRangeBlocks(48)
                    .trackedUpdateRate(3)
                    .build());

    public static final EntityType<SkaroCityDalekEntity> SKARO_CITY_DALEK = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "skaro_city_dalek"),
            FabricEntityTypeBuilder.<SkaroCityDalekEntity>create(SpawnGroup.MONSTER, SkaroCityDalekEntity::new)
                    .dimensions(EntityDimensions.fixed(1.8f, 1.9f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    public static final EntityType<SkaroCityDalekAltEntity> SKARO_CITY_DALEK_ALT = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "skaro_city_dalek_alt"),
            FabricEntityTypeBuilder.<SkaroCityDalekAltEntity>create(SpawnGroup.MONSTER, SkaroCityDalekAltEntity::new)
                    .dimensions(EntityDimensions.fixed(1.8f, 1.9f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    public static final EntityType<SupremeCouncilDalekEntity> SUPREME_COUNCIL_DALEK = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "supreme_council_dalek"),
            FabricEntityTypeBuilder.<SupremeCouncilDalekEntity>create(SpawnGroup.MONSTER, SupremeCouncilDalekEntity::new)
                    .dimensions(EntityDimensions.fixed(1.8f, 1.9f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    public static final EntityType<LaserEntity> LASER = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "laser"),
            FabricEntityTypeBuilder.<LaserEntity>create(SpawnGroup.MISC, LaserEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(1)
                    .build());

    public static final EntityType<TimeLordEntity> TIME_LORD = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "time_lord"),
            FabricEntityTypeBuilder.<TimeLordEntity>create(SpawnGroup.CREATURE, TimeLordEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                    .trackRangeBlocks(48)
                    .build());

    public static final EntityType<com.cerulixteam.gallifrey.entity.custom.MondasianCybermanEntity> MONDASIAN_CYBERMAN = Registry.register(Registries.ENTITY_TYPE, new Identifier(GallifreyMod.MOD_ID, "mondasian_cyberman"), FabricEntityTypeBuilder.<com.cerulixteam.gallifrey.entity.custom.MondasianCybermanEntity>create(SpawnGroup.MONSTER, com.cerulixteam.gallifrey.entity.custom.MondasianCybermanEntity::new).dimensions(EntityDimensions.fixed(0.7f, 2.0f)).trackRangeBlocks(64).build());
    public static final EntityType<com.cerulixteam.gallifrey.entity.custom.ProtoCybermanEntity> PROTO_CYBERMAN = Registry.register(Registries.ENTITY_TYPE, new Identifier(GallifreyMod.MOD_ID, "proto_cyberman"), FabricEntityTypeBuilder.<com.cerulixteam.gallifrey.entity.custom.ProtoCybermanEntity>create(SpawnGroup.MONSTER, com.cerulixteam.gallifrey.entity.custom.ProtoCybermanEntity::new).dimensions(EntityDimensions.fixed(0.7f, 2.0f)).trackRangeBlocks(64).build());
    public static final EntityType<com.cerulixteam.gallifrey.entity.custom.CybermatEntity> CYBERMAT = Registry.register(Registries.ENTITY_TYPE, new Identifier(GallifreyMod.MOD_ID, "cybermat"), FabricEntityTypeBuilder.<com.cerulixteam.gallifrey.entity.custom.CybermatEntity>create(SpawnGroup.MONSTER, com.cerulixteam.gallifrey.entity.custom.CybermatEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.35f)).trackRangeBlocks(32).build());

    public static final EntityType<com.cerulixteam.gallifrey.entity.custom.CavemanEntity> CAVEMAN = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(GallifreyMod.MOD_ID, "caveman"),
            FabricEntityTypeBuilder.<com.cerulixteam.gallifrey.entity.custom.CavemanEntity>create(SpawnGroup.CREATURE, com.cerulixteam.gallifrey.entity.custom.CavemanEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                    .trackRangeBlocks(48)
                    .build());
}
