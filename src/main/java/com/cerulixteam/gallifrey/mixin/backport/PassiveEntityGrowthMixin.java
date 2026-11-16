package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.Backport;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Golden Dandelion (26.1): a baby given one doesn't age towards adulthood until it's given another. */
@Mixin(PassiveEntity.class)
public abstract class PassiveEntityGrowthMixin {
    @Shadow
    public abstract int getBreedingAge();

    @Inject(method = "setBreedingAge", at = @At("HEAD"), cancellable = true)
    private void gallifrey$stayBaby(int age, CallbackInfo ci) {
        int current = this.getBreedingAge();
        if (current < 0 && age > current && ((PassiveEntity) (Object) this).getCommandTags().contains(Backport.STAY_BABY_TAG)) {
            ci.cancel();
        }
    }
}
