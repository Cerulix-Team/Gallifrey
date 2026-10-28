package com.timelordmod.gallifrey;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.timelordmod.gallifrey.block.GallifreyModBlocks;
import com.timelordmod.gallifrey.entity.ModBoats;
import com.timelordmod.gallifrey.entity.GallifreyEntities;
import com.timelordmod.gallifrey.item.GallifreyCreativeTab;
import com.timelordmod.gallifrey.item.GallifreyModItems;
import com.timelordmod.gallifrey.networking.packets.VMPacket;
import com.timelordmod.gallifrey.world.biome.ModBiomes;
import com.timelordmod.gallifrey.world.ModPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.gen.GenerationStep;
import com.timelordmod.gallifrey.world.portal.GallifreyPortalAreaHelper;
import com.timelordmod.gallifrey.world.dimension.ModDimensions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import com.timelordmod.gallifrey.entity.custom.TimeLordEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.timelordmod.gallifrey.block.GallifreyModBlockEntities;
import com.timelordmod.gallifrey.fluid.GallifreyFluids;
import com.timelordmod.gallifrey.networking.packets.SonicCasingPacket;
import com.timelordmod.gallifrey.networking.packets.SonicShadesPacket;

import com.timelordmod.gallifrey.world.feature.PrehistoricVinesFeature;
public class GallifreyMod implements ModInitializer {
	public static final String MOD_ID = "gallifrey";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier VM_PACKET_ID = new Identifier(MOD_ID, "vm_packet");
	public static final Identifier GALLIFREY_FRAME_TESTER =
			new Identifier(GallifreyMod.MOD_ID, "gallifrey_frame");


	@Override
	public void onInitialize() {
        net.minecraft.registry.Registry.register(
                net.minecraft.registry.Registries.FEATURE,
                id("prehistoric_vines"),
                new PrehistoricVinesFeature(net.minecraft.world.gen.feature.DefaultFeatureConfig.CODEC)
        );

		MarsSurvival.register();
		com.timelordmod.gallifrey.world.VacuumMobSurvivalHandler.register();

		// A Time Lord can regenerate twelve times. The thirteenth death is permanent.
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof TimeLordEntity timeLord) || !(entity.getWorld() instanceof ServerWorld world)) {
				return;
			}
			if (timeLord.getRegenerationsUsed() >= 12) {
				return;
			}

			TimeLordEntity regenerated = GallifreyEntities.TIME_LORD.create(world);
			if (regenerated == null) return;
			regenerated.refreshPositionAndAngles(timeLord.getX(), timeLord.getY(), timeLord.getZ(),
					timeLord.getYaw(), timeLord.getPitch());
			regenerated.setRegenerationsUsed(timeLord.getRegenerationsUsed() + 1);
			if (timeLord.hasCustomName()) regenerated.setCustomName(timeLord.getCustomName());
			regenerated.setCustomNameVisible(timeLord.isCustomNameVisible());
			regenerated.setPersistent();
			if (world.spawnEntity(regenerated)) {
				world.spawnParticles(ParticleTypes.FLAME, timeLord.getX(), timeLord.getY() + 1.0, timeLord.getZ(), 45, 0.45, 0.7, 0.45, 0.03);
				world.spawnParticles(ParticleTypes.END_ROD, timeLord.getX(), timeLord.getY() + 1.0, timeLord.getZ(), 35, 0.4, 0.6, 0.4, 0.08);
				world.playSound(null, timeLord.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.HOSTILE, 1.0F, 0.7F);
			}
		});

		LOGGER.info("[Gallifrey] Initialising core systems...!");

		com.timelordmod.gallifrey.item.custom.SpaceSuitOxygenHandler.register();
		com.timelordmod.gallifrey.world.dimension.ClassicBedrockLayer.register();
		GallifreySounds.register();
		GallifreyFluids.registerModFluids();

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.CLASSIC_VILLAGER,
				com.timelordmod.gallifrey.entity.custom.ClassicVillagerEntity.createAttributes()
		);

        FabricDefaultAttributeRegistry.register(
                GallifreyEntities.TIMELORD_VILLAGER,
                VillagerEntity.createVillagerAttributes().build()
        );

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.SKARO_CITY_DALEK,
				com.timelordmod.gallifrey.entity.custom.SkaroCityDalekEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.SKARO_CITY_DALEK_ALT,
				com.timelordmod.gallifrey.entity.custom.SkaroCityDalekAltEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.SUPREME_COUNCIL_DALEK,
				com.timelordmod.gallifrey.entity.custom.SupremeCouncilDalekEntity.createAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.ALPHA_GHAST,
				com.timelordmod.gallifrey.entity.custom.AlphaGhastEntity.createGhastAttributes()
		);

		FabricDefaultAttributeRegistry.register(
				GallifreyEntities.ALPHA_ZOMBIE_PIGMAN,
				com.timelordmod.gallifrey.entity.custom.AlphaZombiePigmanEntity.createAttributes()
		);

		net.minecraft.entity.SpawnRestriction.register(
				GallifreyEntities.ALPHA_GHAST,
				net.minecraft.entity.SpawnRestriction.Location.NO_RESTRICTIONS,
				net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(type, world, reason, pos, random) -> true
		);

		net.minecraft.entity.SpawnRestriction.register(
				GallifreyEntities.ALPHA_ZOMBIE_PIGMAN,
				net.minecraft.entity.SpawnRestriction.Location.ON_GROUND,
				net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(type, world, reason, pos, random) -> world.getBlockState(pos.down()).isOpaqueFullCube(world, pos.down())
		);

        FabricDefaultAttributeRegistry.register(GallifreyEntities.TIME_LORD, com.timelordmod.gallifrey.entity.custom.TimeLordEntity.createAttributes());
        net.minecraft.entity.SpawnRestriction.register(
                GallifreyEntities.TIME_LORD,
                net.minecraft.entity.SpawnRestriction.Location.ON_GROUND,
                net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.toServerWorld().getRegistryKey().equals(com.timelordmod.gallifrey.world.dimension.ModDimensions.GALL_LEVEL_KEY)
                        && world.getBlockState(pos.down()).isOpaqueFullCube(world, pos.down())
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.all(),
                net.minecraft.entity.SpawnGroup.CREATURE,
                GallifreyEntities.TIME_LORD,
                2,
                1,
                1
        );

        FabricDefaultAttributeRegistry.register(GallifreyEntities.CAVEMAN, com.timelordmod.gallifrey.entity.custom.CavemanEntity.createAttributes());
        net.minecraft.entity.SpawnRestriction.register(
                GallifreyEntities.CAVEMAN,
                net.minecraft.entity.SpawnRestriction.Location.ON_GROUND,
                net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> world.getBlockState(pos.down()).isOpaqueFullCube(world, pos.down()));
        // Spawning itself is in the Prehistoric biome's JSON (worldgen/biome/prehistoric.json).
        com.timelordmod.gallifrey.entity.custom.CavemanEntity.registerMonsterHunting();

		com.timelordmod.gallifrey.villager.GallifreyVillagers.register();

        FabricDefaultAttributeRegistry.register(GallifreyEntities.MONDASIAN_CYBERMAN, com.timelordmod.gallifrey.entity.custom.AbstractCybermanEntity.createCybermanAttributes());
        FabricDefaultAttributeRegistry.register(GallifreyEntities.PROTO_CYBERMAN, com.timelordmod.gallifrey.entity.custom.AbstractCybermanEntity.createCybermanAttributes());
        FabricDefaultAttributeRegistry.register(GallifreyEntities.CYBERMAT, com.timelordmod.gallifrey.entity.custom.CybermatEntity.createCybermatAttributes());

		GallifreyModItems.register();
		GallifreyModBlocks.register();
		com.timelordmod.gallifrey.world.tree.decorator.ModTreeDecorators.register();
		com.timelordmod.gallifrey.world.GallifreySpawnRule.register();
		com.timelordmod.gallifrey.item.custom.GallifreyAccessories.register();
		com.timelordmod.gallifrey.item.custom.HoverBoots.register();
		com.timelordmod.gallifrey.item.custom.WhitePointStarGlow.register();
		com.timelordmod.gallifrey.compat.tardisrefined.TardisRefinedSonics.register();
		com.timelordmod.gallifrey.compat.adastra.AdAstraCompat.register();
		com.timelordmod.gallifrey.compat.ait.AitSonics.register();
		com.timelordmod.gallifrey.compat.ait.AitTravelWorlds.register();
		com.timelordmod.gallifrey.compat.deo.DeoPortals.register();
		com.timelordmod.gallifrey.compat.tempad.TempadDoors.register();
		com.timelordmod.gallifrey.compat.nirvana.NirvanaBlunt.register();
		com.timelordmod.gallifrey.compat.graves.UniversalGravesCompat.register();
		com.timelordmod.gallifrey.compat.waystones.WaystonesCompat.register();
		com.timelordmod.gallifrey.compat.thermoo.ThermooCompat.register();
		com.timelordmod.gallifrey.compat.pehkui.PehkuiCompat.init();
		com.timelordmod.gallifrey.compat.heracles.HeraclesQuests.install();
		com.timelordmod.gallifrey.compat.dehydration.DehydrationCompat.register();
		com.timelordmod.gallifrey.compat.angels.WeepingAngelsCompat.register();
		com.timelordmod.gallifrey.compat.trinkets.TrinketsCompat.register();
		com.timelordmod.gallifrey.compat.claims.ClaimsCompat.register();
		com.timelordmod.gallifrey.compat.farmersdelight.FarmersDelightCompat.register();
		com.timelordmod.gallifrey.world.PetesWorldPortals.register();
		GallifreyModBlockEntities.register();

		// Atrium fuel burns for 3x the time of coal: 4800 ticks vs 1600.
		FuelRegistry.INSTANCE.add(GallifreyModItems.ATRIUM_FUEL, 4800);

		FuelRegistry.INSTANCE.add(GallifreyModItems.CLASSIC_COAL, 1600);
		FuelRegistry.INSTANCE.add(GallifreyModBlocks.CLASSIC_COAL_BLOCK, 16000);

		// Atrium generation is explicitly limited to vanilla Overworld biomes.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.ATRIUM_ORE_UPPER_PLACED_KEY
		);
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.ATRIUM_ORE_MIDDLE_PLACED_KEY
		);
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.ATRIUM_ORE_SMALL_PLACED_KEY
		);

		// Nether ore generation: both rare ores use the Nether-specific configured targets.
		// Every Nether biome, not just the five vanilla ones, so nether biome mods (Incendium,
		// Amplified Nether, BetterNether...) get these ores too.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInTheNether(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.NETHER_SONIC_CRYSTAL_ORE_PLACED_KEY);
		BiomeModifications.addFeature(
				BiomeSelectors.foundInTheNether(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.NETHER_WHITE_POINT_ORE_PLACED_KEY);

		// Ruby and sapphire generate like emerald ore: same vein size, count and heights (see their
		// placed_feature JSON), in every biome that has emerald ore. That's vanilla's mountains and
		// windswept hills, plus the matching biomes from world mods like Terralith.
		RegistryKey<PlacedFeature> emeraldOre = RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier("minecraft", "ore_emerald"));
		BiomeModifications.addFeature(
				context -> context.hasPlacedFeature(emeraldOre),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.RUBY_ORE_PLACED_KEY);
		BiomeModifications.addFeature(
				context -> context.hasPlacedFeature(emeraldOre),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.SAPPHIRE_ORE_PLACED_KEY);

		// Sonic Crystal also belongs in the vanilla Overworld.
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				ModPlacedFeatures.SONIC_CRYSTAL_ORE_PLACED_KEY);

		GallifreyCreativeTab.register();
		ModBoats.registerBoats();
		// Biolith works on whatever overworld is in use, including Terralith's (which still has
		// vanilla forests), so the Treeborg forest generates with Terralith installed too.
		BiomePlacement.replaceOverworld(BiomeKeys.FOREST, ModBiomes.TREEBORG_FOREST, 0.3d);



		ServerPlayNetworking.registerGlobalReceiver(
				new Identifier("gallifrey", "vm_packet"),
				VMPacket::receive
		);

		ServerPlayNetworking.registerGlobalReceiver(
				new Identifier(MOD_ID, "change_sonic_workshop_casing"),
				SonicCasingPacket::receive
		);

        ServerPlayNetworking.registerGlobalReceiver(
                new Identifier(MOD_ID, "sonic_shades_use"),
                SonicShadesPacket::receive
        );


		// Strippable blocks registry

		//Tardis wood type
		StrippableBlockRegistry.register(GallifreyModBlocks.TARDIS_LOG, GallifreyModBlocks.STRIP_TARDIS_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.TARDIS_WOOD, GallifreyModBlocks.STRIP_TARDIS_WOOD);

		//Ulanda wood type
		StrippableBlockRegistry.register(GallifreyModBlocks.ULANDA_LOG, GallifreyModBlocks.STRIP_ULANDA_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.ULANDA_WOOD, GallifreyModBlocks.STRIP_ULANDA_WOOD);

		//Treeborg wood set
		StrippableBlockRegistry.register(GallifreyModBlocks.TREEBORG_LOG, GallifreyModBlocks.STRIP_TREEBORG_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.TREEBORG_WOOD, GallifreyModBlocks.STRIP_TREEBORG_WOOD);

		//Ash wood set
		StrippableBlockRegistry.register(GallifreyModBlocks.ASH_LOG, GallifreyModBlocks.STRIP_ASH_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.ASH_WOOD, GallifreyModBlocks.STRIP_ASH_WOOD);

		//Maple wood set
		StrippableBlockRegistry.register(GallifreyModBlocks.MAPLE_LOG, GallifreyModBlocks.STRIP_MAPLE_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.MAPLE_WOOD, GallifreyModBlocks.STRIP_MAPLE_WOOD);

		//Moonpine wood set
		StrippableBlockRegistry.register(GallifreyModBlocks.MOONPINE_LOG, GallifreyModBlocks.STRIP_MOONPINE_LOG);
		StrippableBlockRegistry.register(GallifreyModBlocks.MOONPINE_WOOD, GallifreyModBlocks.STRIP_MOONPINE_WOOD);

		//Skaro wasted wood set


		//Moon-pine wood set


		//GRASSBLOCK STUFF
		com.timelordmod.gallifrey.block.custom.GrassInteractions.register();


		// Custom Dimension Stuff
		CustomPortalApiRegistry.registerPortalFrameTester(GALLIFREY_FRAME_TESTER, GallifreyPortalAreaHelper::new);

		CustomPortalBuilder.beginPortal()
				.frameBlock(GallifreyModBlocks.CLASSIC_OBSIDIAN)
				.destDimID(ModDimensions.CLASSIC_NETHER_LEVEL_KEY.getValue())
				.returnDim(ModDimensions.CLASSIC_LEVEL_KEY.getValue(), true)
				.tintColor(131, 66, 184)
				.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(GallifreyModBlocks.REINFORCED_STEEL_BLOCK)
						.customFrameTester(GALLIFREY_FRAME_TESTER)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "gallifrey"))
						.tintColor(230, 142, 48)
						.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(GallifreyModBlocks.MARS_CHISELED_STONE_BRICKS)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "mars"))
						.tintColor(150, 55, 35)
						.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(GallifreyModBlocks.DALEKANIUM_BLOCK)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "skaro"))
						.tintColor(150, 110, 45)
						.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(GallifreyModBlocks.STEEL_BLOCK)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "mondas"))
						.tintColor(130, 150, 170)
						.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(net.minecraft.block.Blocks.CRYING_OBSIDIAN)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "petes_world"))
						.tintColor(90, 140, 230)
						.registerPortal();

		CustomPortalBuilder.beginPortal()
						.frameBlock(GallifreyModBlocks.LOST_DIRT)
						.lightWithItem(GallifreyModItems.WHITE_POINT_STAR)
						.registerIgniteEvent(GallifreyMod::useUpWhitePointStar)
						.destDimID(new Identifier(GallifreyMod.MOD_ID, "lost_reality"))
						.tintColor(40, 160, 150)
						.registerPortal();



		com.timelordmod.gallifrey.world.UuidRewards.register();
		com.timelordmod.gallifrey.util.BannedPlayers.register();

		LOGGER.info("[Gallifrey] Core systems ready.");
	}

	public static Identifier id(String path) { return new Identifier(MOD_ID, path);}

	/** Lighting a portal with a White Point Star uses one up (Custom Portal API leaves the item alone). */
	private static void useUpWhitePointStar(net.minecraft.entity.player.PlayerEntity player, net.minecraft.world.World world,
											 net.minecraft.util.math.BlockPos portalPos, net.minecraft.util.math.BlockPos framePos,
											 net.kyrptonaught.customportalapi.portal.PortalIgnitionSource source) {
		if (player == null || player.getAbilities().creativeMode) return;
		for (net.minecraft.util.Hand hand : net.minecraft.util.Hand.values()) {
			net.minecraft.item.ItemStack stack = player.getStackInHand(hand);
			if (stack.isOf(GallifreyModItems.WHITE_POINT_STAR)) {
				stack.decrement(1);
				return;
			}
		}
	}
}
