package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.item.custom.HeadwearItem;
import com.cerulixteam.gallifrey.item.custom.SonicShadesItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents wearable head items from also being rendered by vanilla's
 * third-person held-item feature. The head feature renderer is the ONLY
 * renderer that should draw these items on the player.
 *
 * This intentionally checks the item in either hand, rather than comparing
 * complete ItemStacks. Creative-mode copies can have different NBT/counts,
 * but they are still the same wearable item and must not produce the large,
 * upside-down held copy.
 */
@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gallifrey$hideHeadwearHeldCopy(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch,
            CallbackInfo ci) {

        if (!(entity instanceof AbstractClientPlayerEntity player)) {
            return;
        }

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        if (gallifrey$isHeadwear(mainHand) || gallifrey$isHeadwear(offHand)) {
            ci.cancel();
        }
    }

    private static boolean gallifrey$isHeadwear(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof HeadwearItem
                || stack.getItem() instanceof SonicShadesItem);
    }
}
