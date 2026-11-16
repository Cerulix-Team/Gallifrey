package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EquipmentSlot;

/**
 * Renders the supplied Blockbench Sonic Shades model on the player's face.
 * The model is an item model with a HEAD display transform, so it is rendered
 * through Minecraft's normal item-model pipeline instead of being treated as
 * armor geometry. This preserves the exact supplied texture/model.
 */
public final class SonicShadesFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public SonicShadesFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            AbstractClientPlayerEntity player,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch) {

        ItemStack stack = com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.getShades(player);
        // Support both the dedicated Gallifrey wearable slot and the vanilla head slot.
        // The latter is how shades were equipped in existing worlds/screens.
        if (!stack.isOf(GallifreyModItems.SONIC_SHADES)) {
            stack = player.getEquippedStack(EquipmentSlot.HEAD);
        }
        if (!stack.isOf(GallifreyModItems.SONIC_SHADES)) {
            return;
        }

        matrices.push();

        // Follow the player's actual head rotation/pitch.
        this.getContextModel().head.rotate(matrices);

        // Same rendering path as Gallifrey's HatFeatureRenderer. The model's
        // own HEAD display transform controls orientation, scale and placement.

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.renderItem(
                player,
                stack,
                ModelTransformationMode.HEAD,
                false,
                matrices,
                vertexConsumers,
                player.getWorld(),
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }
}
