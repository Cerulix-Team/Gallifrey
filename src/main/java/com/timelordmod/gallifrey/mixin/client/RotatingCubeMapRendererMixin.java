package com.timelordmod.gallifrey.mixin.client;

import com.timelordmod.gallifrey.client.TitleScreenBackground;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the title screen panorama for Gallifrey's chosen vortex.
 *
 * This hooks the panorama renderer itself, not the line in TitleScreen.render that calls it.
 * Adventures in Time replaces that line with a @Redirect, but its replacement still ends up
 * calling a RotatingCubeMapRenderer, so this works with or without AiT (and with other mods
 * that swap the panorama the same way).
 *
 * Only acts while the title screen is drawing (TitleScreenBackground is set); any other
 * screen that uses a panorama is left alone.
 */
@Mixin(RotatingCubeMapRenderer.class)
public abstract class RotatingCubeMapRendererMixin {

    @Inject(method = "render(FF)V", at = @At("HEAD"), cancellable = true)
    private void gallifrey$replacePanorama(float delta, float alpha, CallbackInfo ci) {
        if (TitleScreenBackground.draw(alpha)) {
            ci.cancel();
        }
    }
}
