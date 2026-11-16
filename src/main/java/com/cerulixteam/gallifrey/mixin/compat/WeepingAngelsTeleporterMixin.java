package com.cerulixteam.gallifrey.mixin.compat;

import com.cerulixteam.gallifrey.compat.angels.WeepingAngelsCompat;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Weeping Angels picks a random dimension to send its victims to. If it picks the TARDIS
 * interior, Lost Reality or an airless Gallifrey dimension, keep them in the dimension they're
 * already in (the angel still sends them somewhere random there). See WeepingAngelsCompat.
 *
 * @Pseudo: skipped silently when Weeping Angels isn't installed.
 * Target: dev.jeryn.angels.util.Teleporter#getRandomDimension(RandomSource, ServerLevel), 46.1.6.
 */
@Pseudo
@Mixin(targets = "dev.jeryn.angels.util.Teleporter", remap = false)
public abstract class WeepingAngelsTeleporterMixin {

    @Inject(method = "getRandomDimension", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void gallifrey$avoidUnsafeDimensions(Random random, ServerWorld current,
                                                        CallbackInfoReturnable<ServerWorld> cir) {
        ServerWorld picked = cir.getReturnValue();
        if (picked != null && picked != current && WeepingAngelsCompat.isOffLimits(picked.getRegistryKey())) {
            cir.setReturnValue(current);
        }
    }
}
