package com.cerulixteam.gallifrey.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lost Reality's thick cyan fog. Same idea as the Mars and Mondas fog mixins,
 * but it only touches normal terrain fog, so underwater, lava, powder snow,
 * Blindness and Darkness keep their vanilla fog.
 */
/*
 * priority = 1100: runs after other mods' applyFog hooks (default 1000). Iris records the fog
 * density for shader packs at the start of applyFog; if this cancelled first, Iris kept a stale
 * value (e.g. thick underwater fog after surfacing). Sodium and Iris both read the fog
 * start/end/colour set here, so the fog itself still shows with them installed.
 */
@Mixin(value = BackgroundRenderer.class, priority = 1100)
public class LostRealityFogMixin {
    /** Blocks from the camera where the fog starts. 0 = right on top of you. */
    private static final float FOG_START = 0.0F;
    /** Blocks from the camera where the fog is fully opaque. Lower = thicker. */
    private static final float FOG_END = 40.0F;

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$lostRealityFog(Camera camera, BackgroundRenderer.FogType fogType,
                                                 float viewDistance, boolean thickFog, float tickDelta,
                                                 CallbackInfo ci) {
        if (fogType != BackgroundRenderer.FogType.FOG_TERRAIN) return;
        if (camera.getSubmersionType() != CameraSubmersionType.NONE) return;

        Entity entity = camera.getFocusedEntity();
        if (entity == null) return;
        if (entity instanceof LivingEntity living
                && (living.hasStatusEffect(StatusEffects.BLINDNESS)
                || living.hasStatusEffect(StatusEffects.DARKNESS))) return;
        if (!"lost_reality".equals(entity.getWorld().getRegistryKey().getValue().getPath())) return;

        RenderSystem.setShaderFogStart(FOG_START);
        RenderSystem.setShaderFogEnd(Math.min(viewDistance, FOG_END));
        // Same cyan as the Lost Reality biomes' fog_color (#5CCFD6), so the horizon matches the sky.
        RenderSystem.setShaderFogColor(0.36F, 0.81F, 0.84F);
        ci.cancel();
    }
}
