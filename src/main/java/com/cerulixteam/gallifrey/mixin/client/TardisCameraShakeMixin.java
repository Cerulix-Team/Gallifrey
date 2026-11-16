package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.client.TardisClientEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shakes the camera inside a TARDIS during take-off, flight and landing (TardisClientEffects). */
@Mixin(GameRenderer.class)
public class TardisCameraShakeMixin {
    // tiltViewWhenHurt runs every frame whatever the view-bobbing setting is.
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"))
    private void gallifrey$tardisShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        TardisClientEffects.applyShake(matrices, tickDelta);
    }
}
