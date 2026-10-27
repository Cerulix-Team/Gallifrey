package com.timelordmod.gallifrey.mixin.compat;

import com.timelordmod.gallifrey.compat.betterinventory.BetterInventoryCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds Gallifrey's accessory provider (Sonic Shades + fez) to Better Inventory 2.x's list, so
 * they get their own tab in its Accessories side panel. See BetterInventoryCompat.
 *
 * Better Inventory works out its provider list once and keeps it in a static field; this appends
 * Gallifrey's provider to the end of it the first time it's read. @Pseudo: skipped silently when
 * Better Inventory (or this class, in 1.x) isn't there. Checked against Better Inventory 2.0.2.
 */
@Pseudo
@Mixin(targets = "dev.fallingcloud.betterinventory.compat.accessory.Accessories", remap = false)
public abstract class BetterInventoryAccessoriesMixin {

    @Shadow(remap = false)
    private static List<Object> providers;

    @Inject(method = "providers", at = @At("RETURN"), cancellable = true, remap = false)
    private static void gallifrey$addProvider(CallbackInfoReturnable<List<Object>> cir) {
        List<Object> list = cir.getReturnValue();
        if (list == null) return;
        for (Object existing : list) {
            if (BetterInventoryCompat.isOurs(existing)) return;
        }
        Object ours = BetterInventoryCompat.provider();
        if (ours == null) return;
        List<Object> withOurs = new ArrayList<>(list);
        withOurs.add(ours);
        providers = List.copyOf(withOurs);
        cir.setReturnValue(providers);
    }
}
