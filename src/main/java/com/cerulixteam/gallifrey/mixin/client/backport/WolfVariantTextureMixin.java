package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Wolf variants (1.20.5): each of the eight new coats has its own wild, tame and angry texture. */
@Mixin(WolfEntityRenderer.class)
public abstract class WolfVariantTextureMixin {
    @Inject(method = "getTexture(Lnet/minecraft/entity/passive/WolfEntity;)Lnet/minecraft/util/Identifier;", at = @At("RETURN"), cancellable = true)
    private void gallifrey$variantTexture(WolfEntity wolf, CallbackInfoReturnable<Identifier> cir) {
        int variant = AnimalVariants.get(wolf);
        if (variant <= 0 || variant >= AnimalVariants.WOLVES.length) return;
        String suffix = wolf.isTamed() ? "_tame" : wolf.hasAngerTime() ? "_angry" : "";
        cir.setReturnValue(new Identifier("gallifrey", "textures/entity/backport/wolf/wolf_" + AnimalVariants.WOLVES[variant] + suffix + ".png"));
    }
}
