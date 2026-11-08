package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.SpearItem;
import com.cerulixteam.gallifrey.client.backport.SpearAnimations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Third person spear in the hand (26.3): pushed forward while charging, and thrust forward when jabbing. */
@Mixin(HeldItemFeatureRenderer.class)
public abstract class SpearHeldItemMixin {
    @Inject(method = "renderItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void gallifrey$spear(LivingEntity entity, ItemStack stack, ModelTransformationMode mode, Arm arm, MatrixStack matrices,
                                 VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        SpearItem spear = SpearAnimations.spear(stack);
        if (spear == null) return;
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float swing = SpearAnimations.isStabbing(entity, arm) ? entity.getHandSwingProgress(tickDelta) : 0.0F;
        if (swing > 0.0F) SpearAnimations.thirdPersonAttackItem(matrices, swing, spear);
        if (SpearAnimations.isCharging(entity, arm)) {
            SpearAnimations.thirdPersonUseItem(matrices, SpearAnimations.ticksUsing(entity, tickDelta), swing, arm, spear);
        }
    }
}
