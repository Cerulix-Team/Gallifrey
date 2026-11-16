package com.cerulixteam.gallifrey.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Splash lines containing %player% show the player's username. */
@Mixin(SplashTextResourceSupplier.class)
public abstract class SplashPlayerNameMixin {
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private void gallifrey$playerName(CallbackInfoReturnable<SplashTextRenderer> cir) {
        SplashTextRenderer splash = cir.getReturnValue();
        if (splash == null) return;
        String text = ((SplashTextRendererAccessor) splash).gallifrey$getText();
        if (text == null || !text.contains("%player%")) return;
        String name = MinecraftClient.getInstance().getSession().getUsername();
        cir.setReturnValue(new SplashTextRenderer(text.replace("%player%", name)));
    }
}