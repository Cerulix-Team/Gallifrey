package com.cerulixteam.gallifrey.client.backport;

import com.cerulixteam.gallifrey.block.backport.SpearItem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

/**
 * 26.3's spear animations (net.minecraft.client.model.effects.SpearAnimations), ported to 1.20.1.
 * <ul>
 *   <li>Charging (holding use): the spear is lowered to point forward, sways once the dismount window
 *   is nearly over, dips when the knockback window ends, and is raised back up when the charge is spent.</li>
 *   <li>Jabbing (attacking): a forward stab instead of the usual swing.</li>
 * </ul>
 * Hooked in by SpearFirstPersonMixin, SpearArmPoseMixin and SpearHeldItemMixin.
 */
public final class SpearAnimations {
    private SpearAnimations() {}

    /** No hit feedback: 1.20.1 has no "spear just hit something" signal on the client. */
    private static final float NO_HIT_FEEDBACK = 100.0F;

    @Nullable
    public static SpearItem spear(ItemStack stack) {
        return stack.getItem() instanceof SpearItem spear ? spear : null;
    }

    /** Is this entity charging with the spear in this arm? */
    public static boolean isCharging(LivingEntity entity, Arm arm) {
        if (!entity.isUsingItem() || entity.getItemUseTimeLeft() <= 0) return false;
        Hand hand = entity.getActiveHand();
        Arm usingArm = hand == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
        return usingArm == arm && spear(entity.getStackInHand(hand)) != null;
    }

    /** Ticks the spear has been held up for, with partial ticks. */
    public static float ticksUsing(LivingEntity entity, float tickDelta) {
        return entity.getItemUseTime() + tickDelta;
    }

    /** Is this entity jabbing with a spear in this arm? */
    public static boolean isStabbing(LivingEntity entity, Arm arm) {
        if (!entity.handSwinging && entity.handSwingTicks == 0) return false;
        Hand hand = entity.preferredHand == null ? Hand.MAIN_HAND : entity.preferredHand;
        Arm swingArm = hand == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
        return swingArm == arm && spear(entity.getStackInHand(hand)) != null;
    }

    // ---------------------------------------------------------------- first person

    /** Replaces the item transform while charging (vanilla: ItemUseAnimation.SPEAR). */
    public static void firstPersonUse(MatrixStack matrices, float timeHeld, Arm arm, SpearItem spear) {
        UseParams p = UseParams.of(spear, timeHeld);
        int invert = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(invert * 0.56F, -0.52F, -0.72F);
        matrices.translate(
                invert * (p.raiseProgress * 0.15F + p.raiseProgressEnd * -0.05F + p.swayProgress * -0.1F + p.swayScaleSlow * 0.005F),
                p.raiseProgress * -0.075F + p.raiseProgressMiddle * 0.075F + p.swayScaleFast * 0.01F,
                p.raiseProgressStart * 0.05F + p.raiseProgressEnd * -0.05F + p.swayScaleSlow * 0.005F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(
                -65.0F * inOutBack(p.raiseProgress) - 35.0F * p.lowerProgress + 100.0F * p.raiseBackProgress - 0.5F * p.swayScaleFast),
                0.0F, 0.1F, 0.0F);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(
                invert * (-90.0F * progress(p.raiseProgress, 0.5F, 0.55F) + 90.0F * p.swayProgress + 2.0F * p.swayScaleSlow)),
                invert * 0.15F, 0.0F, 0.0F);
        matrices.translate(0.0F, -hitFeedbackAmount(NO_HIT_FEEDBACK), 0.0F);
    }

    /** Replaces the swing while jabbing (vanilla: SwingAnimationType.STAB). Call after the equip offset. */
    public static void firstPersonAttack(float attack, MatrixStack matrices, Arm arm) {
        int invert = arm == Arm.RIGHT ? 1 : -1;
        float start = inOutSine(progress(attack, 0.0F, 0.05F));
        float middle = outBack(progress(attack, 0.05F, 0.2F));
        float end = inOutExpo(progress(attack, 0.4F, 1.0F));
        matrices.translate(invert * 0.1F * (start - middle), -0.075F * (start - end), 0.65F * (start - middle));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70.0F * (start - end)));
        matrices.translate(0.0F, 0.0F, -0.25F * (end - middle));
    }

    // ---------------------------------------------------------------- third person

    /** The charging arm pose (vanilla: HumanoidModel.ArmPose.SPEAR). */
    public static void thirdPersonHandUse(ModelPart armPart, ModelPart head, Arm arm, SpearItem spear, LivingEntity entity, float ticksUsing, float tickDelta) {
        int invert = arm == Arm.RIGHT ? 1 : -1;
        armPart.yaw = -0.1F * invert + head.yaw;
        armPart.pitch = (float) (-Math.PI / 2) + head.pitch + 0.8F;
        if (entity.isFallFlying() || entity.getLeaningPitch(tickDelta) > 0.0F) {
            armPart.pitch -= 0.9599311F;
        }
        armPart.yaw = MathHelper.RADIANS_PER_DEGREE * MathHelper.clamp(MathHelper.DEGREES_PER_RADIAN * armPart.yaw, -60.0F, 60.0F);
        armPart.pitch = MathHelper.RADIANS_PER_DEGREE * MathHelper.clamp(MathHelper.DEGREES_PER_RADIAN * armPart.pitch, -120.0F, 30.0F);
        if (ticksUsing > 0.0F) {
            UseParams p = UseParams.of(spear, ticksUsing);
            armPart.yaw += -invert * p.swayScaleFast * MathHelper.RADIANS_PER_DEGREE * p.swayIntensity;
            armPart.roll += -invert * p.swayScaleSlow * MathHelper.RADIANS_PER_DEGREE * p.swayIntensity * 0.5F;
            armPart.pitch += MathHelper.RADIANS_PER_DEGREE * (-40.0F * p.raiseProgressStart + 30.0F * p.raiseProgressMiddle
                    - 20.0F * p.raiseProgressEnd + 20.0F * p.lowerProgress + 10.0F * p.raiseBackProgress
                    + 0.6F * p.swayScaleSlow * p.swayIntensity);
        }
    }

    /** Moves the held spear while charging. Call just before the item is drawn in the hand. */
    public static void thirdPersonUseItem(MatrixStack matrices, float ticksUsing, float swing, Arm arm, SpearItem spear) {
        if (ticksUsing == 0.0F) return;
        float attack = inQuad(progress(swing, 0.05F, 0.2F));
        float retract = inOutExpo(progress(swing, 0.4F, 1.0F));
        UseParams p = UseParams.of(spear, ticksUsing);
        int invert = arm == Arm.RIGHT ? 1 : -1;
        float raise = 1.0F - outBack(1.0F - p.raiseProgress);
        float feedback = hitFeedbackAmount(NO_HIT_FEEDBACK);
        matrices.translate(0.0F, -feedback * 0.4F, -spear.forwardMovement() * (raise - p.raiseBackProgress) + feedback);
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(70.0F * (p.raiseProgress - p.raiseBackProgress) - 40.0F * (attack - retract)),
                0.0F, -0.03125F, 0.125F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(invert * 90 * (p.raiseProgress - p.swayProgress + 3.0F * retract + attack)),
                0.0F, 0.0F, 0.125F);
    }

    /** The jabbing arm (vanilla: HumanoidModel.setupAttackAnimation, STAB). Replaces the swing's arm movement. */
    public static void thirdPersonAttackHand(ModelPart body, ModelPart rightArm, ModelPart leftArm, ModelPart attackArm, float animation) {
        rightArm.yaw -= body.yaw;
        leftArm.yaw -= body.yaw;
        leftArm.pitch -= body.yaw;
        float prepare = inOutSine(progress(animation, 0.0F, 0.05F));
        float attack = inQuad(progress(animation, 0.05F, 0.2F));
        float retract = inOutExpo(progress(animation, 0.4F, 1.0F));
        attackArm.pitch += (90.0F * prepare - 120.0F * attack + 30.0F * retract) * MathHelper.RADIANS_PER_DEGREE;
    }

    /** Moves the held spear while jabbing. Call just before the item is drawn in the hand. */
    public static void thirdPersonAttackItem(MatrixStack matrices, float animation, SpearItem spear) {
        if (animation <= 0.0F) return;
        float attack = inQuad(progress(animation, 0.05F, 0.2F));
        float retract = inOutExpo(progress(animation, 0.4F, 1.0F));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(70.0F * (attack - retract)), 0.0F, -0.125F, 0.125F);
        matrices.translate(0.0F, spear.forwardMovement() * (attack - retract), 0.0F);
    }

    // ---------------------------------------------------------------- timings

    private record UseParams(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle, float raiseProgressEnd,
                             float swayProgress, float lowerProgress, float raiseBackProgress,
                             float swayIntensity, float swayScaleSlow, float swayScaleFast) {
        static UseParams of(SpearItem spear, float time) {
            int finishRaising = spear.delayTicks();
            int finishSwaying = spear.dismountTicks() + finishRaising;
            int startSwaying = finishSwaying - 20;
            int finishLowering = spear.knockbackTicks() + finishRaising;
            int startLowering = finishLowering - 40;
            int finishRaisingBack = spear.damageTicks() + finishRaising;
            float raise = progress(time, 0.0F, finishRaising);
            float raiseStart = progress(raise, 0.0F, 0.5F);
            float raiseMiddle = progress(raise, 0.5F, 0.8F);
            float raiseEnd = progress(raise, 0.8F, 1.0F);
            float sway = progress(time, startSwaying, finishSwaying);
            float lower = outCubic(inOutElastic(progress(time - 20.0F, startLowering, finishLowering)));
            float raiseBack = progress(time, finishRaisingBack - 5, finishRaisingBack);
            float intensity = 2.0F * outCirc(sway) - 2.0F * inCirc(raiseBack);
            float slow = MathHelper.sin(time * 19.0F * MathHelper.RADIANS_PER_DEGREE) * intensity;
            float fast = MathHelper.sin(time * 30.0F * MathHelper.RADIANS_PER_DEGREE) * intensity;
            return new UseParams(raise, raiseStart, raiseMiddle, raiseEnd, sway, lower, raiseBack, intensity, slow, fast);
        }
    }

    private static float hitFeedbackAmount(float ticksSinceFeedback) {
        return 0.4F * (outQuart(progress(ticksSinceFeedback, 1.0F, 3.0F)) - inOutSine(progress(ticksSinceFeedback, 3.0F, 10.0F)));
    }

    // ---------------------------------------------------------------- easing (vanilla net.minecraft.util.Ease)

    private static float progress(float time, float start, float end) {
        if (end == start) return time >= end ? 1.0F : 0.0F;
        return MathHelper.clamp((time - start) / (end - start), 0.0F, 1.0F);
    }

    private static float inQuad(float x) { return x * x; }
    private static float inOutSine(float x) { return -(MathHelper.cos((float) Math.PI * x) - 1.0F) / 2.0F; }
    private static float outBack(float x) {
        float d = x - 1.0F;
        return 1.0F + 2.70158F * d * d * d + 1.70158F * d * d;
    }
    private static float outQuart(float x) {
        float d = 1.0F - x;
        return 1.0F - d * d * d * d;
    }
    private static float outCubic(float x) {
        float d = 1.0F - x;
        return 1.0F - d * d * d;
    }
    private static float inOutExpo(float x) {
        if (x < 0.5F) return x == 0.0F ? 0.0F : (float) (Math.pow(2.0, 20.0 * x - 10.0) / 2.0);
        return x == 1.0F ? 1.0F : (float) ((2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0);
    }
    private static float outCirc(float x) { return (float) Math.sqrt(1.0F - (x - 1.0F) * (x - 1.0F)); }
    private static float inCirc(float x) { return (float) (-Math.sqrt(1.0F - x * x)) + 1.0F; }
    private static float inOutElastic(float x) {
        if (x == 0.0F) return 0.0F;
        if (x == 1.0F) return 1.0F;
        double sin = Math.sin((20.0 * x - 11.125) * (float) Math.PI * 4.0F / 9.0F);
        return x < 0.5F ? (float) (-(Math.pow(2.0, 20.0 * x - 10.0) * sin) / 2.0) : (float) (Math.pow(2.0, -20.0 * x + 10.0) * sin / 2.0 + 1.0);
    }
    private static float inOutBack(float x) {
        if (x < 0.5F) return 4.0F * x * x * (7.189819F * x - 2.5949094F) / 2.0F;
        float dt = 2.0F * x - 2.0F;
        return (dt * dt * (3.5949094F * dt + 2.5949094F) + 2.0F) / 2.0F;
    }
}
