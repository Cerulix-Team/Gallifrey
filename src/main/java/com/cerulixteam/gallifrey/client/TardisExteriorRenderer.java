package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.TardisExteriorBlock;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.model.TardisModel;
import com.cerulixteam.gallifrey.tardis.TardisExteriorCatalog;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.LightmapTextureManager;

/**
 * Renders the supplied Blockbench police-box model with the selected shell texture.
 *
 * Take-off and landing look like the show: the box fades out (or in) in pulses, in time with the
 * wheeze of the sound (TardisPhaseCurves), and the lamp on top flashes with each pulse. In the
 * vortex there is nothing to draw. The windows and lamp glow when the TARDIS has power.
 */
public class TardisExteriorRenderer implements BlockEntityRenderer<TardisExteriorBlockEntity> {
    public static final EntityModelLayer TARDIS_EXTERIOR_LAYER =
            new EntityModelLayer(new Identifier("gallifrey", "tardis_exterior"), "main");

    private final TardisModel model;

    public TardisExteriorRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new TardisModel(context.getLayerModelPart(TARDIS_EXTERIOR_LAYER));
    }

    public static Identifier texture(String style) {
        String id = TardisExteriorCatalog.get(style).id();
        return new Identifier("gallifrey", "textures/block/tardis/" + id + ".png");
    }

    public static Identifier emissionTexture(String style) {
        String id = TardisExteriorCatalog.get(style).id();
        return new Identifier("gallifrey", "textures/block/tardis/" + id + "_emission.png");
    }

    @Override
    public void render(TardisExteriorBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        float alpha = blockEntity.visibility(tickDelta);
        if (alpha <= 0.01F) return;
        float lamp = blockEntity.isPowered() ? blockEntity.lampBrightness(tickDelta) : 0.0F;

        matrices.push();
        matrices.translate(0.5, 1.5, 0.5);
        int rotation = blockEntity.getCachedState().get(TardisExteriorBlock.ROTATION);
        // Rotation r means the doors face yaw r*45 (0 = south), the same way TardisFlight.frontVector reads it.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - rotation * 45.0F));
        float baseScale = 0.94F;
        matrices.scale(-baseScale, -baseScale, baseScale);

        model.setDoorsOpen(blockEntity.getCachedState().get(TardisExteriorBlock.OPEN));
        String style = blockEntity.getExteriorStyle();
        Identifier texture = texture(style);
        Identifier emission = emissionTexture(style);

        boolean solid = alpha >= 0.995F;
        RenderLayer layer = solid ? RenderLayer.getEntityCutoutNoCull(texture) : RenderLayer.getEntityTranslucent(texture);
        model.render(matrices, vertexConsumers.getBuffer(layer), light, overlay, 1.0F, 1.0F, 1.0F, alpha);

        // Glow pass: windows and lamp at full brightness, fading and flashing with the box.
        if (lamp > 0.01F) {
            model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(emission)),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay, 1.0F, 1.0F, 1.0F, alpha * lamp);
        }

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TardisExteriorBlockEntity blockEntity) {
        return true;
    }
}
