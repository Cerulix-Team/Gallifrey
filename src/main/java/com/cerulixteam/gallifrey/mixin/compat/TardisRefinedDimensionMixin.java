package com.cerulixteam.gallifrey.mixin.compat;

import com.cerulixteam.gallifrey.compat.ait.AitTravelWorlds;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TARDIS Refined offers every dimension that isn't in its banned_dimensions config. Treat Lost
 * Reality and Gallifrey's TARDIS interior dimension as banned too, so they aren't listed on the
 * TARDIS's dimension selection and can't be set as destinations.
 *
 * @Pseudo: skipped silently when TARDIS Refined isn't installed.
 * Target: whocraft.tardis_refined.common.util.DimensionUtil#isAllowedDimension(ResourceKey).
 */
@Pseudo
@Mixin(targets = "whocraft.tardis_refined.common.util.DimensionUtil", remap = false)
public abstract class TardisRefinedDimensionMixin {

    @Inject(method = "isAllowedDimension", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gallifrey$hideGallifreyDimensions(RegistryKey<World> level, CallbackInfoReturnable<Boolean> cir) {
        if (level != null && AitTravelWorlds.HIDDEN.contains(level.getValue())) {
            cir.setReturnValue(false);
        }
    }
}
