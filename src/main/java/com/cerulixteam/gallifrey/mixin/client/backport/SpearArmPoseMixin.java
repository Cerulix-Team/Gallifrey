package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.SpearItem;
import com.cerulixteam.gallifrey.client.backport.SpearAnimations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Third person spear arms (26.3): the charging pose (arm forward with the spear lowered, swaying)
 * and the forward stab instead of the overhead swing.
 */
@Mixin(BipedEntityModel.class)
public abstract class SpearArmPoseMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow protected abstract Arm getPreferredArm(T entity);
    @Shadow protected abstract ModelPart getArm(Arm arm);

    /** The stab: same body turn as the normal swing, but the arm thrusts forward instead. */
    @Inject(method = "animateArms", at = @At("HEAD"), cancellable = true)
    private void gallifrey$stab(T entity, float animationProgress, CallbackInfo ci) {
        // handSwingProgress lives in EntityModel (the superclass), so it can't be @Shadowed here.
        float swing = ((BipedEntityModel<?>) (Object) this).handSwingProgress;
        if (swing <= 0.0F) return;
        Arm arm = this.getPreferredArm(entity);
        if (!SpearAnimations.isStabbing(entity, arm)) return;
        this.body.yaw = MathHelper.sin(MathHelper.sqrt(swing) * (float) (Math.PI * 2)) * 0.2F;
        if (arm == Arm.LEFT) this.body.yaw *= -1.0F;
        this.rightArm.pivotZ = MathHelper.sin(this.body.yaw) * 5.0F;
        this.rightArm.pivotX = -MathHelper.cos(this.body.yaw) * 5.0F;
        this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * 5.0F;
        this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * 5.0F;
        this.rightArm.yaw += this.body.yaw;
        this.leftArm.yaw += this.body.yaw;
        this.leftArm.pitch += this.body.yaw;
        SpearAnimations.thirdPersonAttackHand(this.body, this.rightArm, this.leftArm, this.getArm(arm), swing);
        ci.cancel();
    }

    /** The charging pose replaces whatever pose the arm had. */
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void gallifrey$charge(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch,
                                  CallbackInfo ci) {
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        for (Arm arm : Arm.values()) {
            if (!SpearAnimations.isCharging(entity, arm)) continue;
            SpearItem spear = SpearAnimations.spear(entity.getActiveItem());
            if (spear == null) continue;
            SpearAnimations.thirdPersonHandUse(arm == Arm.RIGHT ? this.rightArm : this.leftArm, this.head, arm, spear, entity,
                    SpearAnimations.ticksUsing(entity, tickDelta), tickDelta);
        }
    }
}
