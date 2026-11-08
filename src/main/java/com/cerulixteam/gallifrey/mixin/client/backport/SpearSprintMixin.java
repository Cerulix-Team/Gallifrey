package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.client.backport.SpearAnimations;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Charging with a spear doesn't slow you down or stop you sprinting (26.3: the spear's use_effects
 * are can_sprint true, speed_multiplier 1.0). In 1.20.1 using any item slows you to 20% and blocks sprinting.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class SpearSprintMixin {
    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean gallifrey$spearDoesNotSlow(ClientPlayerEntity self) {
        return self.isUsingItem() && SpearAnimations.spear(self.getActiveItem()) == null;
    }
}
