package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.SkaroCityDalekEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

/** Shared renderer for Gallifrey Daleks. The head only rotates around Y; the eye stalk handles pitch. */
public class DalekRenderer<T extends SkaroCityDalekEntity> extends GeoEntityRenderer<T> {
    public DalekRenderer(EntityRendererFactory.Context context, String modelName) {
        super(context, new DefaultedEntityGeoModel<>(GallifreyMod.id(modelName)));
        this.shadowRadius = 0.8F;
    }

    @Override
    public void preRender(MatrixStack matrices, T dalek, BakedGeoModel model,
                          VertexConsumerProvider vertexConsumers, VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(matrices, dalek, model, vertexConsumers, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        float bodyYaw = dalek.getBodyYaw();
        float headYaw = dalek.getHeadYaw();
        float relativeYaw = net.minecraft.util.math.MathHelper.wrapDegrees(headYaw - bodyYaw);
        float headPitch = dalek.getPitch(partialTick);

        GeoBone head = this.getGeoModel().getBone("head").orElse(null);
        if (head != null) {
            // Do not pitch/roll the Dalek's head. It only spins left/right.
            head.setRotX(0.0F);
            head.setRotZ(0.0F);
            head.setRotY(relativeYaw * net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE);
        }

        GeoBone eyeStalk = this.getGeoModel().getBone("eye_stalk").orElse(null);
        if (eyeStalk != null) {
            // Looking up/down is done entirely by the eye stalk.
            eyeStalk.setRotY(0.0F);
            eyeStalk.setRotZ(0.0F);
            eyeStalk.setRotX(-headPitch * net.minecraft.util.math.MathHelper.RADIANS_PER_DEGREE);
        }
    }
}
