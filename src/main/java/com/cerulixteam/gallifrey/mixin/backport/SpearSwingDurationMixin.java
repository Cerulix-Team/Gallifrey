package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.SpearItem;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A spear's jab lasts as long as 26.3 says (0.65-1.15 seconds), not the usual 6 tick swing. */
@Mixin(LivingEntity.class)
public abstract class SpearSwingDurationMixin {
    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void gallifrey$spearJab(CallbackInfoReturnable<Integer> cir) {
        if (((LivingEntity) (Object) this).getMainHandStack().getItem() instanceof SpearItem spear) {
            cir.setReturnValue(spear.attackDurationTicks());
        }
    }
}
