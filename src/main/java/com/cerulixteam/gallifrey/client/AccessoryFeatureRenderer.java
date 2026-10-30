package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.custom.GallifreyAccessories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * Draws whatever is in the Sonic Shades slot and the fez slot on the player's head.
 *
 * It uses exactly the same steps vanilla uses for an item in the helmet slot (follow the head,
 * then HeadFeatureRenderer.translate: move up, turn round, scale by 0.625 and flip upright), then
 * the model's own "head" display transform. So the shades and fezzes look the same in their own
 * slots as they do in the helmet slot. Without the flip, the old renderer drew the shades
 * upside down and in the wrong place.
 *
 * Items in the actual helmet slot are left to vanilla. Nothing is drawn while the player is
 * invisible, so the Vortex Manipulator's cloak hides them too.
 */
public final class AccessoryFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public AccessoryFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        // First-person Model is drawing your own body: nothing on the head, or it covers the camera.
        if (com.cerulixteam.gallifrey.compat.firstperson.FirstPersonCompat.hideHeadwear()) return;
        renderOnHead(matrices, vertexConsumers, light, player, GallifreyAccessories.getFez(player));
        renderOnHead(matrices, vertexConsumers, light, player, GallifreyAccessories.getShades(player));
    }

    private void renderOnHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                              AbstractClientPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return;
        matrices.push();
        this.getContextModel().getHead().rotate(matrices);
        HeadFeatureRenderer.translate(matrices, false);
        MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer()
                .renderItem(player, stack, ModelTransformationMode.HEAD, false, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
