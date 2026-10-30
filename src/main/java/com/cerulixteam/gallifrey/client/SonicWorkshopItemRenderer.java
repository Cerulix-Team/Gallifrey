package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SonicWorkshopBlockItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class SonicWorkshopItemRenderer extends GeoItemRenderer<SonicWorkshopBlockItem> {
    public SonicWorkshopItemRenderer() {
        super(new DefaultedItemGeoModel<>(GallifreyMod.id("sonic_workshop")));
    }

    @Override
    public void actuallyRender(MatrixStack matrices, SonicWorkshopBlockItem animatable,
                               BakedGeoModel model, net.minecraft.client.render.RenderLayer renderType,
                               VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender,
                               float partialTick, int packedLight, int packedOverlay, float red, float green,
                               float blue, float alpha) {
        matrices.push();
        matrices.scale(0.65F, 0.65F, 0.65F);
        super.actuallyRender(matrices, animatable, model, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        matrices.pop();
    }
}
