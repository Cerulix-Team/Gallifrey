package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.client.TardisClientEffects;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inside a TARDIS with its power off, the interior's ambient light (0.4 in tardis_interior_type) drops to 0,
 * so only real light sources light it up.
 */
@Mixin(LightmapTextureManager.class)
public abstract class TardisInteriorLightMixin {
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$powerOffDarkness(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (!TardisClientEffects.isInteriorDark()) return;
        float f = lightLevel / 15.0F;
        cir.setReturnValue(f / (4.0F - 3.0F * f)); // vanilla curve with an ambient light of 0
    }
}
