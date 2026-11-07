package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.backport.BackportBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.ChestBoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

/** Client side of the backported blocks: render layers, grass tints and the Pale Oak boat models. */
public final class BackportClient {
    private BackportClient() {}

    /** Leaf Litter's colour. 1.21.5 tints it from a new "dry foliage" colour map; this is that map's colour for a plains-like biome. */
    private static final int DRY_FOLIAGE = 0xA37546;

    public static void register() {
        for (Block block : BackportBlocks.CUTOUT) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
        for (Block block : BackportBlocks.CUTOUT_MIPPED) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());

        // Bush: tint 0 is the leaves. Wildflowers: tint 1 is the stems (tint 0 is the flowers, left as they are).
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (state.isOf(BackportBlocks.WILDFLOWERS) && tintIndex == 0) return -1;
            return world != null && pos != null ? BiomeColors.getGrassColor(world, pos) : GrassColors.getColor(0.5, 1.0);
        }, BackportBlocks.GRASS_TINTED.toArray(new Block[0]));
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> GrassColors.getColor(0.5, 1.0), BackportBlocks.BUSH);

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> DRY_FOLIAGE, BackportBlocks.LEAF_LITTER);

        EntityModelLayerRegistry.registerModelLayer(new EntityModelLayer(new Identifier("gallifrey", "boat/pale_oak_boat"), "main"),
                BoatEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(new EntityModelLayer(new Identifier("gallifrey", "chest_boat/pale_oak_boat"), "main"),
                ChestBoatEntityModel::getTexturedModelData);
    }
}
