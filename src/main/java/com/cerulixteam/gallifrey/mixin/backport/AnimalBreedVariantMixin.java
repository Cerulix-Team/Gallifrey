package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.mob.AnimalVariants;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A bred cow, pig, chicken or wolf takes one of its parents' variants. */
@Mixin(AnimalEntity.class)
public abstract class AnimalBreedVariantMixin {
    @Inject(method = "breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V", at = @At("HEAD"))
    private void gallifrey$inheritVariant(ServerWorld world, AnimalEntity other, PassiveEntity baby, CallbackInfo ci) {
        AnimalEntity self = (AnimalEntity) (Object) this;
        if (baby != null && AnimalVariants.hasVariants(baby)) {
            AnimalVariants.set(baby, AnimalVariants.get(self.getRandom().nextBoolean() ? self : other));
        }
    }
}
