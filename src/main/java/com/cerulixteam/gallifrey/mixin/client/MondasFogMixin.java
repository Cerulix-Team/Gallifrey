package com.cerulixteam.gallifrey.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.cerulixteam.gallifrey.client.PlanetWeatherClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * priority = 1100: runs after other mods' applyFog hooks (default 1000). Iris records the fog
 * density for shader packs at the start of applyFog; if this cancelled first, Iris kept a stale
 * value (e.g. thick underwater fog after surfacing). Sodium and Iris both read the fog
 * start/end/colour set here, so the fog itself still shows with them installed.
 */
@Mixin(value = BackgroundRenderer.class, priority = 1100)
public class MondasFogMixin {
    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$mondasFog(Camera camera, BackgroundRenderer.FogType fogType,
                                            float viewDistance, boolean thickFog, float tickDelta,
                                            CallbackInfo ci) {
        if (camera.getFocusedEntity() == null) return;
        World world = camera.getFocusedEntity().getWorld();
        if (!"mondas".equals(world.getRegistryKey().getValue().getPath())) return;

        if (PlanetWeatherClient.isMondasBlizzard()) {
            float intensity = PlanetWeatherClient.mondasBlizzardIntensity(tickDelta);
            float fogEnd = 34.0F - (25.0F * intensity);
            RenderSystem.setShaderFogStart(0.0F);
            RenderSystem.setShaderFogEnd(Math.min(viewDistance, fogEnd));
            // Cold white blizzard fog; the normal Mondas night remains space-dark.
            RenderSystem.setShaderFogColor(
                    0.88F + 0.08F * intensity,
                    0.91F + 0.07F * intensity,
                    0.94F + 0.06F * intensity
            );
        } else {
            // Do not wash out the permanent-night space sky when the storm is absent.
            RenderSystem.setShaderFogStart(8.0F);
            RenderSystem.setShaderFogEnd(Math.min(viewDistance, 160.0F));
            RenderSystem.setShaderFogColor(0.055F, 0.065F, 0.095F);
        }
        ci.cancel();
    }
}
