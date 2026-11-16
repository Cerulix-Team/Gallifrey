package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.client.AccessoryFeatureRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gallifrey$addHeadwearFeature(CallbackInfo ci) {
        LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) (Object) this;
        PlayerEntityRenderer renderer = (PlayerEntityRenderer) (Object) this;
        // Helmet-slot items (fezzes, shades, hats) are already drawn by vanilla's head layer.
        // This adds the Sonic Shades and fez slots.
        accessor.gallifrey$getFeatures().add(new AccessoryFeatureRenderer(renderer));
    }
}