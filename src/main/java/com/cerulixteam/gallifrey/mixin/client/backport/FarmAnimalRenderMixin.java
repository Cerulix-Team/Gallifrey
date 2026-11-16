package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import com.cerulixteam.gallifrey.client.backport.BackportGeo;
import com.cerulixteam.gallifrey.client.backport.BackportMobsClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Warm and cold cows, pigs and chickens (1.21.5) are drawn with their Bedrock models instead of the vanilla ones. */
@Mixin(LivingEntityRenderer.class)
public abstract class FarmAnimalRenderMixin {
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$variantModel(LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light,
                                        CallbackInfo ci) {
        if (!(entity instanceof AnimalEntity animal) || !AnimalVariants.hasFarmVariants(animal) || AnimalVariants.get(animal) == AnimalVariants.TEMPERATE) return;
        BackportGeo.ReplacedRenderer<AnimalEntity> renderer = BackportMobsClient.farm(animal);
        if (renderer == null) return;
        renderer.render(animal, yaw, tickDelta, matrices, consumers, light);
        ci.cancel();
    }
}
