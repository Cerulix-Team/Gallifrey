package com.cerulixteam.gallifrey.mixin.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.cerulixteam.gallifrey.compat.betterinventory.BetterInventoryCompat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the icon on the Gallifrey tab of Better Inventory 2.x's Accessories panel (the Sonic
 * Shades symbol). Better Inventory only knows icons for the accessory mods it supports itself,
 * so without this the tab would be blank. Client only; @Pseudo, so skipped without Better Inventory.
 */
@Pseudo
@Mixin(targets = "dev.fallingcloud.betterinventory.client.CompatButtons", remap = false)
public abstract class BetterInventoryTabIconMixin {

    @Unique
    private static final Identifier GALLIFREY_TAB_ICON =
            new Identifier("gallifrey", "textures/gui/sonic_shades_symbol.png");

    @Inject(method = "drawIcon", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gallifrey$drawTabIcon(DrawContext context, String modId, int x, int y, boolean hovered,
                                              CallbackInfo ci) {
        if (!BetterInventoryCompat.PROVIDER_ID.equals(modId)) return;
        RenderSystem.enableBlend();
        // 64x64 texture drawn at 16x16
        context.drawTexture(GALLIFREY_TAB_ICON, x, y, 16, 16, 0.0F, 0.0F, 64, 64, 64, 64);
        RenderSystem.disableBlend();
        ci.cancel();
    }
}
