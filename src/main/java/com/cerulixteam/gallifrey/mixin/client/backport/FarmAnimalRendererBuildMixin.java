package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.client.backport.BackportMobsClient;
import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Builds the warm and cold animal renderers whenever the vanilla ones are (each resource reload). */
@Mixin({CowEntityRenderer.class, PigEntityRenderer.class, ChickenEntityRenderer.class})
public abstract class FarmAnimalRendererBuildMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void gallifrey$buildVariants(EntityRendererFactory.Context ctx, CallbackInfo ci) {
        Object self = this;
        BackportMobsClient.buildFarm(ctx, self instanceof CowEntityRenderer ? EntityType.COW : self instanceof PigEntityRenderer ? EntityType.PIG : EntityType.CHICKEN);
    }
}
