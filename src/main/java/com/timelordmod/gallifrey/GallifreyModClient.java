package com.timelordmod.gallifrey;

import com.timelordmod.gallifrey.block.GallifreyModBlockEntities;
import com.timelordmod.gallifrey.block.GallifreyModBlocks;
import com.timelordmod.gallifrey.block.custom.SonicWorkshopBlock;
import com.timelordmod.gallifrey.client.CreativeSectionSidebar;
import com.timelordmod.gallifrey.client.PlanetWeatherClient;
import com.timelordmod.gallifrey.client.SonicShadesClient;
import com.timelordmod.gallifrey.client.TardisExteriorRenderer;
import com.timelordmod.gallifrey.entity.client.DiamondMinecartRenderer;
import com.timelordmod.gallifrey.entity.client.ClassicVillagerRenderer;
import com.timelordmod.gallifrey.client.render.SonicWorkshopBlockEntityRenderer;
import com.timelordmod.gallifrey.fluid.GallifreyFluids;
import com.timelordmod.gallifrey.item.GallifreyModItems;
import com.timelordmod.gallifrey.item.custom.SonicScrewdriver;
import com.timelordmod.gallifrey.item.custom.VortexManipulator;
import com.timelordmod.gallifrey.model.TardisModel;
import com.timelordmod.gallifrey.screens.SonicWorkshopScreen;
import com.timelordmod.gallifrey.screens.VortexManipulatorScreen;
import com.timelordmod.gallifrey.sonic.SonicCasing;
import com.timelordmod.gallifrey.world.dimension.ModDimensions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import com.timelordmod.gallifrey.entity.GallifreyEntities;
import com.timelordmod.gallifrey.client.SkaroCityDalekRenderer;
import com.timelordmod.gallifrey.client.SkaroCityDalekAltRenderer;
import com.timelordmod.gallifrey.client.SupremeCouncilDalekRenderer;
import com.timelordmod.gallifrey.client.AlphaGhastRenderer;
import com.timelordmod.gallifrey.client.AlphaZombiePigmanRenderer;
import com.timelordmod.gallifrey.entity.client.LaserRenderer;

public class GallifreyModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        com.timelordmod.gallifrey.compat.nea.NotEnoughAnimationsCompat.register();
        // Distant Horizons: hide its far terrain in the foggy dimensions (Mars, Lost Reality).
        // Checked here so the compat class, which uses DH's API, is only loaded when DH is installed.
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("distanthorizons")) {
            try {
                com.timelordmod.gallifrey.compat.dh.DistantHorizonsCompat.register();
                GallifreyMod.LOGGER.info("[Gallifrey] Distant Horizons found: far terrain hidden in foggy dimensions");
            } catch (Throwable t) {
                GallifreyMod.LOGGER.warn("[Gallifrey] Distant Horizons API wasn't what Gallifrey expected; fog compat is off", t);
            }
        }

        // Flat 2D inventory icons for the fezzes and the Dalek eyestalk (see HeadwearFlatModels).
        com.timelordmod.gallifrey.client.HeadwearFlatModels.register();

        // Classic leather armour: the item icons are greyscale and get the leather colour
        // (or the dye colour) at runtime, exactly like vanilla leather armour.
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex > 0 ? -1
                        : ((net.minecraft.item.DyeableItem) stack.getItem()).getColor(stack),
                com.timelordmod.gallifrey.item.GallifreyModItems.CLASSIC_LEATHER_HELMET,
                com.timelordmod.gallifrey.item.GallifreyModItems.CLASSIC_LEATHER_CHESTPLATE,
                com.timelordmod.gallifrey.item.GallifreyModItems.CLASSIC_LEATHER_LEGGINGS,
                com.timelordmod.gallifrey.item.GallifreyModItems.CLASSIC_LEATHER_BOOTS);

        // Cyber spawn eggs use complete custom PNG artwork. Force white/no tint for
        // every model layer so Minecraft does not recolour the supplied textures.
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> 0xFFFFFFFF,
                GallifreyModItems.MONDASIAN_CYBERMAN_SPAWN_EGG,
                GallifreyModItems.PROTO_CYBERMAN_SPAWN_EGG,
                GallifreyModItems.CYBERMAT_SPAWN_EGG);

        // Register the complete supplied Classic-era asset pack as an optional built-in
        // resource pack. The integrated Classic blocks use the supplied textures directly;
        // this pack provides the remaining vanilla item/entity/sound overrides when enabled.
        FabricLoader.getInstance().getModContainer(GallifreyMod.MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier(GallifreyMod.MOD_ID, "classic_assets"),
                        container,
                        ResourcePackActivationType.NORMAL));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), GallifreyFluids.STILL_RADIATION, GallifreyFluids.FLOWING_RADIATION);
        FluidRenderHandlerRegistry.INSTANCE.register(GallifreyFluids.STILL_RADIATION, GallifreyFluids.FLOWING_RADIATION,
                new SimpleFluidRenderHandler(
                        new Identifier("minecraft:block/water_still"),
                        new Identifier("minecraft:block/water_flow"),
                        0x2EFF2E
                ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                GallifreyFluids.STILL_CLASSIC_WATER, GallifreyFluids.FLOWING_CLASSIC_WATER);
        FluidRenderHandlerRegistry.INSTANCE.register(
                GallifreyFluids.STILL_CLASSIC_WATER, GallifreyFluids.FLOWING_CLASSIC_WATER,
                new SimpleFluidRenderHandler(
                        new Identifier("minecraft:block/water_still"),
                        new Identifier("minecraft:block/water_flow"),
                        0x3F76E4
                ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                GallifreyFluids.STILL_CLASSIC_LAVA, GallifreyFluids.FLOWING_CLASSIC_LAVA);
        FluidRenderHandlerRegistry.INSTANCE.register(
                GallifreyFluids.STILL_CLASSIC_LAVA, GallifreyFluids.FLOWING_CLASSIC_LAVA,
                new SimpleFluidRenderHandler(
                        new Identifier("gallifrey:block/classic_nether/lava_still"),
                        new Identifier("gallifrey:block/classic_nether/lava_flow"),
                        0xFFFFFF
                ));

        com.timelordmod.gallifrey.client.ModClientPackets.register();;

        EntityRendererRegistry.register(GallifreyEntities.TIME_LORD, com.timelordmod.gallifrey.entity.client.TimeLordRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.CAVEMAN, com.timelordmod.gallifrey.entity.client.CavemanRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.MONDASIAN_CYBERMAN, com.timelordmod.gallifrey.entity.client.MondasianCybermanRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.PROTO_CYBERMAN, com.timelordmod.gallifrey.entity.client.ProtoCybermanRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.CYBERMAT, com.timelordmod.gallifrey.entity.client.CybermatRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(com.timelordmod.gallifrey.entity.client.GallifreyCyberModelLayers.MONDASIAN_CYBERMAN, com.timelordmod.gallifrey.entity.client.MondasianCybermanModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(com.timelordmod.gallifrey.entity.client.GallifreyCyberModelLayers.PROTO_CYBERMAN, com.timelordmod.gallifrey.entity.client.ProtoCybermanModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(com.timelordmod.gallifrey.entity.client.GallifreyCyberModelLayers.CYBERMAT, com.timelordmod.gallifrey.entity.client.CybermatModel::getTexturedModelData);
        EntityRendererRegistry.register(GallifreyEntities.SKARO_CITY_DALEK, SkaroCityDalekRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.SKARO_CITY_DALEK_ALT, SkaroCityDalekAltRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.SUPREME_COUNCIL_DALEK, SupremeCouncilDalekRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.LASER, LaserRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.DIAMOND_MINECART, DiamondMinecartRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.CLASSIC_VILLAGER, ClassicVillagerRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.TIMELORD_VILLAGER, com.timelordmod.gallifrey.entity.client.TimeLordVillagerRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.ALPHA_GHAST, AlphaGhastRenderer::new);
        EntityRendererRegistry.register(GallifreyEntities.ALPHA_ZOMBIE_PIGMAN, AlphaZombiePigmanRenderer::new);
        SonicShadesClient.register();
        com.timelordmod.gallifrey.compat.trinkets.TrinketsClientCompat.register();
        com.timelordmod.gallifrey.client.GearTooltips.register();
        ClientTickEvents.END_CLIENT_TICK.register(PlanetWeatherClient::tick);

        // =========================================================
        // SONIC SCREWDRIVER - CASING MODEL
        // =========================================================

        ModelPredicateProviderRegistry.register(
                GallifreyModItems.SONIC_SCREWDRIVER,
                new Identifier("gallifrey", "sonic_casing"),
                (stack, world, entity, seed) -> {

                    SonicCasing casing =
                            SonicScrewdriver.getCasing(stack);

                    return casing.ordinal() / (float) (SonicCasing.values().length - 1);
                }
        );

        // =========================================================
        // SONIC SCREWDRIVER - ON/OFF MODEL
        // =========================================================

        ModelPredicateProviderRegistry.register(
                GallifreyModItems.SONIC_SCREWDRIVER,
                new Identifier(
                        "gallifrey",
                        "sonic_on"
                ),
                (stack, world, entity, seed) ->
                        SonicScrewdriver.isOn(stack)
                                ? 1.0F
                                : 0.0F
        );

        // =========================================================
        // VORTEX MANIPULATOR
        // =========================================================

        UseItemCallback.EVENT.register(
                (player, world, hand) -> {

                    ItemStack stack =
                            player.getStackInHand(hand);

                    if (
                            stack.getItem()
                                    instanceof VortexManipulator
                    ) {

                        // Holding an Atrium Core in the other hand means "refuel", not "open the
                        // screen": let VortexManipulator.use() handle it. (This callback also runs
                        // on the singleplayer server, so returning success here used to stop the
                        // refuel from ever happening.)
                        if (VortexManipulator.isRefuelling(player, hand)) {
                            return TypedActionResult.pass(stack);
                        }

                        if (world.isClient()) {

                            MinecraftClient
                                    .getInstance()
                                    .setScreen(
                                            new VortexManipulatorScreen()
                                    );
                        }

                        return TypedActionResult.success(
                                stack,
                                world.isClient()
                        );
                    }

                    return TypedActionResult.pass(stack);
                }
        );

        // =========================================================
        // DIMENSION SKY
        // =========================================================

        DimensionRenderingRegistry.registerSkyRenderer(
                ModDimensions.MOON_LEVEL_KEY,
                new MoonSkyRenderer()
        );

        DimensionRenderingRegistry.registerSkyRenderer(
                ModDimensions.GALL_LEVEL_KEY,
                new TwinSunSkyRenderer()
        );

        DimensionRenderingRegistry.registerSkyRenderer(
                ModDimensions.SKARO_LEVEL_KEY,
                new SkaroSkyRenderer()
        );

        DimensionRenderingRegistry.registerSkyRenderer(
                ModDimensions.MONDAS_LEVEL_KEY,
                new MondasSkyRenderer()
        );

        DimensionRenderingRegistry.registerSkyRenderer(
                ModDimensions.LOST_REALITY_LEVEL_KEY,
                new LostRealitySkyRenderer()
        );

        // =========================================================
        // MARS CRYSTAL RENDER LAYER
        // =========================================================
        // The crystal texture contains transparent pixels.  Cutout rendering
        // prevents those transparent pixels from appearing as black planes.
        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.PISS_CRYSTAL,
                RenderLayer.getCutout()
        );

        // =========================================================
        // Creative tab sections register
        // =========================================================

        CreativeSectionSidebar.register();
        // Title screen backgrounds from resource packs and addon mods (see the README).
        com.timelordmod.gallifrey.client.CustomMenuBackgrounds.register();

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TREE_TAPPER,
                RenderLayer.getCutout()
        );
        // =========================================================
        // TARDIS RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TARDIS_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_TARDIS_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TARDIS_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TARDIS_WOOD_DOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/tardis_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/tardis_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                TardisExteriorRenderer.TARDIS_EXTERIOR_LAYER,
                TardisModel::getTexturedModelData
        );

        BlockEntityRendererRegistry.register(
                GallifreyModBlockEntities.TARDIS_EXTERIOR,
                TardisExteriorRenderer::new
        );

        BlockEntityRendererRegistry.register(
                GallifreyModBlockEntities.SONIC_WORKSHOP_BLOCK_ENTITY,
                SonicWorkshopBlockEntityRenderer::new
        );

        // =========================================================
        // ULANDA RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ULANDA_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_ULANDA_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ULANDA_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ULANDA_DOOR,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ULANDA_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/ulanda_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/ulanda_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        // =========================================================
        // TREEBORG RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TREEBORG_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_TREEBORG_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TREEBORG_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TREEBORG_DOOR,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.TREEBORG_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/treeborg_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/treeborg_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        // =========================================================
        // ASH RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ASH_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_ASH_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ASH_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ASH_DOOR,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.ASH_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/ash_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/ash_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        // =========================================================
        // MAPLE RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MAPLE_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_MAPLE_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MAPLE_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MAPLE_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/maple_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/maple_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        // =========================================================
        // MOONPINE RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MOONPINE_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_MOONPINE_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MOONPINE_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.MOONPINE_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "boat/moonpine_boat"
                        ),
                        "main"
                ),
                BoatEntityModel::getTexturedModelData
        );

        EntityModelLayerRegistry.registerModelLayer(
                new EntityModelLayer(
                        new Identifier(
                                "gallifrey",
                                "chest_boat/moonpine_boat"
                        ),
                        "main"
                ),
                ChestBoatEntityModel::getTexturedModelData
        );

        // =========================================================
        // WASTED RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_GLASS,
                RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_GLASS_PANE,
                RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.GRATE_BLOCK,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.THICK_GRATE_BLOCK,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.RUSTY_GRATE_BLOCK,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.RUSTY_THICK_GRATE_BLOCK,
                RenderLayer.getCutout()
        );


        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.WASTED_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.WASTED_DOOR,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.WASTED_TRAPDOOR,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.WASTED_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_WASTED_SAPLING,
                RenderLayer.getCutout()
        );


        // =========================================================
        // CLASSIC RENDER LAYER
        // =========================================================

        // Classic saplings and flowers use transparent cross-plane textures.
        // They must be rendered with cutout rather than the default solid layer
        // or the transparent pixels appear as black squares in-world.
        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_RED_FLOWER,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_YELLOW_FLOWER,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.CLASSIC_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        // =========================================================
        // PREHISTORIC RENDER LAYER
        // =========================================================

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.PREHISTORIC_LEAVES,
                RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.PREHISTORIC_VINE,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
        GallifreyModBlocks.PREHISTORIC_DOOR,
        RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
        GallifreyModBlocks.PREHISTORIC_TRAPDOOR,
        RenderLayer.getCutoutMipped()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.PREHISTORIC_SAPLING,
                RenderLayer.getCutout()
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                GallifreyModBlocks.POTTED_PREHISTORIC_SAPLING,
                RenderLayer.getCutout()
        );

    }


}
