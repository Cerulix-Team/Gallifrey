package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.GallifreySounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClassicHurtSoundMixin {
    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void gallifrey$classicHurtSound(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld() != null && self.getWorld().getRegistryKey().getValue().equals(new net.minecraft.util.Identifier("gallifrey", "classic"))) {
            self.playSound(GallifreySounds.CLASSIC_HURT, 1.0F, 1.0F);
            ci.cancel();
        }
    }
}
