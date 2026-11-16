package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.BackportExtras;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the backported pottery sherds (Flow, Guster, Scrape) put their pattern on a decorated pot. */
@Mixin(DecoratedPotPatterns.class)
public abstract class DecoratedPotPatternsMixin {
    @Inject(method = "fromSherd", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$backportSherds(Item sherd, CallbackInfoReturnable<RegistryKey<String>> cir) {
        RegistryKey<String> pattern = BackportExtras.SHERDS.get(sherd);
        if (pattern != null) cir.setReturnValue(pattern);
    }
}
