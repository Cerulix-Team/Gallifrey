package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.SpearItem;
import com.cerulixteam.gallifrey.client.backport.SpearAnimations;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First person spears (26.3): lowered to point forward while charging, and a stab instead of a swing.
 * Without this, 1.20.1 would use the trident's throwing pose and the sword swing.
 */
@Mixin(HeldItemRenderer.class)
public abstract class SpearFirstPersonMixin {
    @Shadow
    public abstract void renderItem(net.minecraft.entity.LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode,
                                    boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void gallifrey$spear(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress,
                                 ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                 int light, CallbackInfo ci) {
        SpearItem spear = SpearAnimations.spear(item);
        if (spear == null || player.isUsingSpyglass()) return;
        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean right = arm == Arm.RIGHT;

        matrices.push();
        if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
            float timeHeld = item.getMaxUseTime() - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
            SpearAnimations.firstPersonUse(matrices, timeHeld, arm, spear);
        } else {
            // HeldItemRenderer.applyEquipOffset
            matrices.translate((right ? 1 : -1) * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            if (swingProgress > 0.0F) SpearAnimations.firstPersonAttack(swingProgress, matrices, arm);
        }
        this.renderItem(player, item, right ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                !right, matrices, vertexConsumers, light);
        matrices.pop();
        ci.cancel();
    }
}
