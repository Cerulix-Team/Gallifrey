package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * Applies reduced gravity to living entities in Gallifrey's Moon and Mars dimensions.
 * Moon gravity is about 16.54% of Earth's; Mars gravity is about 38% of Earth's.
 * The same scaling is applied to fall-distance damage calculations.
 */
@Mixin(LivingEntity.class)
public abstract class MoonGravityMixin {
    private static final double GALLIFREY_MOON_GRAVITY = 0.1654D;
    private static final double GALLIFREY_MARS_GRAVITY = 0.38D;

    private double gallifrey$gravityScale(LivingEntity entity) {
        if (entity.getWorld().getRegistryKey().equals(ModDimensions.MOON_LEVEL_KEY)) {
            return GALLIFREY_MOON_GRAVITY;
        }
        if (entity.getWorld().getRegistryKey().equals(ModDimensions.MARS_LEVEL_KEY)) {
            return GALLIFREY_MARS_GRAVITY;
        }
        return 1.0D;
    }

    @ModifyConstant(
            method = "travel",
            constant = @Constant(doubleValue = 0.08D)
    )
    private double gallifrey$applyMoonGravity(double vanillaGravity) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return vanillaGravity * gallifrey$gravityScale(entity);
    }

    @ModifyVariable(
            method = "computeFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float gallifrey$reduceMoonFallDamageDistance(float fallDistance) {
        LivingEntity entity = (LivingEntity) (Object) this;
        return fallDistance * (float) gallifrey$gravityScale(entity);
    }
}
