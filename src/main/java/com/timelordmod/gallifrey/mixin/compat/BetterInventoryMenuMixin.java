package com.timelordmod.gallifrey.mixin.compat;

import com.timelordmod.gallifrey.compat.betterinventory.BetterInventoryCompat;
import com.timelordmod.gallifrey.item.custom.AccessorySlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Better Inventory (Fabric) replaces the player inventory with its own screen and menu, so the
 * Sonic Shades and fez slots Gallifrey adds to the vanilla inventory never show up there. This adds
 * them to Better Inventory's menu too, in the bottom row of its top-left 2x2 panel. That panel holds
 * buttons for other mods, and on Fabric 1.20.1 its only one is Accessories, in the first cell, so
 * the bottom row is always free. The panel already draws the slot squares, and the slots draw their
 * own empty icons.
 *
 * They're the same player-stored slots as in the vanilla inventory, so it doesn't matter which
 * screen you use. Shift-clicking out of them works through Better Inventory's own rules (into
 * storage, then the hotbar).
 *
 * @Pseudo: if Better Inventory isn't installed, this mixin is skipped silently. Checked against
 * Better Inventory 1.5.0 for Fabric 1.20.1, whose menu has one constructor (int, PlayerInventory)
 * and numbers its own slots 0-119, so these become slots 120 and 121.
 *
 * Better Inventory 1.x only. 2.0 redesigned the screen (that spot is now its extra storage grid),
 * and has an Accessories panel instead, so on 2.0+ this does nothing and the slots go in a
 * Gallifrey tab there (BetterInventoryAccessoriesMixin / BetterInventoryCompat).
 */
@Pseudo
@Mixin(targets = "dev.fallingcloud.betterinventory.menu.BetterInventoryMenu", remap = false)
public abstract class BetterInventoryMenuMixin extends ScreenHandler {
    protected BetterInventoryMenuMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gallifrey$addAccessorySlots(int syncId, PlayerInventory inventory, CallbackInfo ci) {
        if (BetterInventoryCompat.isV2()) return;
        // Better Inventory's top-left panel is at (82, 20); its cells start 4 pixels in.
        this.addSlot(AccessorySlot.shades(inventory.player, 86, 42));
        this.addSlot(AccessorySlot.fez(inventory.player, 104, 42));
    }
}
