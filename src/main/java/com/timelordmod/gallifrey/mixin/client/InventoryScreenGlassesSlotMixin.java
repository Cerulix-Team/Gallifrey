package com.timelordmod.gallifrey.mixin.client;

import com.timelordmod.gallifrey.item.custom.AccessorySlotLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws vanilla-style slot squares for the Sonic Shades and fez slots (the slots draw their own empty icons). */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenGlassesSlotMixin {
    private static final Identifier GALLIFREY$INVENTORY_TEXTURE =
            new Identifier("minecraft", "textures/gui/container/inventory.png");

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void gallifrey$drawGlassesSlot(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        // x/y are declared by HandledScreen, the superclass of InventoryScreen.
        // Read them through the existing accessor mixin rather than shadowing
        // inherited fields on InventoryScreen (which crashes mixin application).
        if (com.timelordmod.gallifrey.item.custom.GallifreyAccessories.usesTrinkets()) return; // Trinkets' own slots instead
        HandledScreenAccessor screen = (HandledScreenAccessor) this;
        int x = screen.gallifrey$getX() - 1;
        int y = screen.gallifrey$getY() - 1;
        // Slot squares are 18x18 and start one pixel up and left of the slot itself.
        context.drawTexture(GALLIFREY$INVENTORY_TEXTURE,
                x + AccessorySlotLayout.SHADES_X, y + AccessorySlotLayout.SHADES_Y,
                7.0F, 83.0F, 18, 18, 256, 256);
        context.drawTexture(GALLIFREY$INVENTORY_TEXTURE,
                x + AccessorySlotLayout.FEZ_X, y + AccessorySlotLayout.FEZ_Y,
                7.0F, 83.0F, 18, 18, 256, 256);
    }
}
