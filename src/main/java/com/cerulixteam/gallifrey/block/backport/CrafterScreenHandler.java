package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/** The Crafter's screen: 9 grid slots, then the player's inventory. Clicking an empty grid slot (button id = slot) switches it off or on. */
public class CrafterScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate properties;

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9), new ArrayPropertyDelegate(10));
    }

    public CrafterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        super(BackportExtras.CRAFTER_SCREEN, syncId);
        this.inventory = inventory;
        this.properties = properties;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                this.addSlot(new Slot(inventory, index, 26 + col * 18, 17 + row * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return !CrafterScreenHandler.this.isDisabled(index);
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        this.addProperties(properties);
    }

    public boolean isDisabled(int slot) {
        return this.properties.get(slot) == 1;
    }

    public boolean isPowered() {
        return this.properties.get(9) == 1;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id >= 9 || !this.inventory.getStack(id).isEmpty() || !this.getCursorStack().isEmpty()) return false;
        if (this.inventory instanceof CrafterBlockEntity crafter) {
            crafter.toggle(id);
        } else {
            this.properties.set(id, this.isDisabled(id) ? 0 : 1);
        }
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        if (index < 9) {
            if (!this.insertItem(stack, 9, 45, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 9 && !stack.isEmpty(); i++) {
                if (!this.isDisabled(i) && this.insertItem(stack, i, i + 1, false)) moved = true;
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
