package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla fire only lights Nether portals in the Overworld and the Nether.
 * This adds the Classic dimension, the Classic Nether and Pete's World, so flint and steel,
 * fire charges and spreading fire all light portals there the normal way. (Pete's World portals
 * lead to the vanilla Nether and back: see NetherPortalReturnMixin.)
 */
@Mixin(AbstractFireBlock.class)
public abstract class ClassicPortalLightingMixin {

    @Inject(method = "isOverworldOrNether", at = @At("HEAD"), cancellable = true)
    private static void gallifrey$classicPortalDimensions(World world, CallbackInfoReturnable<Boolean> cir) {
        if (world.getRegistryKey().equals(ModDimensions.CLASSIC_LEVEL_KEY)
                || world.getRegistryKey().equals(ModDimensions.CLASSIC_NETHER_LEVEL_KEY)
                || world.getRegistryKey().equals(ModDimensions.PETES_WORLD_LEVEL_KEY)) {
            cir.setReturnValue(true);
        }
    }
}
