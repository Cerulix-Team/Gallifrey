package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.backport.CushionEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Draws a Cushion (26.3): a 16x4x16 pad, with vanilla's cushion textures (64x64, standard box layout). */
public class CushionRenderer extends EntityRenderer<CushionEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(new Identifier("gallifrey", "cushion"), "main");
    private final ModelPart model;

    public CushionRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = context.getPart(LAYER);
        this.shadowRadius = 0.4F;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        data.getRoot().addChild("cushion", ModelPartBuilder.create().uv(0, 0).cuboid(-8, -4, -8, 16, 4, 16, Dilation.NONE), ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public void render(CushionEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // Entity models are drawn upside down, like every vanilla one.
        matrices.scale(-1.0F, -1.0F, 1.0F);
        this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.getTexture(entity))), light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(CushionEntity entity) {
        return new Identifier("gallifrey", "textures/entity/cushion/" + entity.getColor().getName() + "_cushion.png");
    }
}
