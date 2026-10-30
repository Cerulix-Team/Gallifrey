package com.cerulixteam.gallifrey.item.custom;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A one-item slot for one of the player's accessory stacks (Sonic Shades, fez).
 *
 * Its empty-slot icon comes from the block atlas (added in assets/minecraft/atlases/blocks.json),
 * the same way vanilla's empty armour slots work, so it shows on any inventory screen: vanilla,
 * creative, or Better Inventory's.
 */
public class AccessorySlot extends Slot {
    public static final Identifier SHADES_ICON = new Identifier("gallifrey", "gui/empty_glasses_slot");
    public static final Identifier FEZ_ICON = new Identifier("gallifrey", "gui/empty_fez_slot");

    private final Predicate<ItemStack> allowed;
    private final Identifier icon;

    public AccessorySlot(Inventory inventory, int x, int y, Predicate<ItemStack> allowed, Identifier icon) {
        super(inventory, 0, x, y);
        this.allowed = allowed;
        this.icon = icon;
    }

    /** The Sonic Shades slot for this player, at the given menu position. */
    public static AccessorySlot shades(PlayerEntity player, int x, int y) {
        return new AccessorySlot(new AccessoryInventory(player, GallifreyAccessories::getShades, GallifreyAccessories::setShades),
                x, y, GallifreyAccessories::fitsShadesSlot, SHADES_ICON);
    }

    /** The fez slot for this player, at the given menu position. */
    public static AccessorySlot fez(PlayerEntity player, int x, int y) {
        return new AccessorySlot(new AccessoryInventory(player, GallifreyAccessories::getFez, GallifreyAccessories::setFez),
                x, y, GallifreyAccessories::fitsFezSlot, FEZ_ICON);
    }

    @Override public boolean canInsert(ItemStack stack) { return allowed.test(stack); }
    @Override public int getMaxItemCount() { return 1; }

    @Override
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, icon);
    }

    /** A one-item inventory backed by one of the player's accessory stacks. */
    public static final class AccessoryInventory implements Inventory {
        private final PlayerEntity owner;
        private final Function<PlayerEntity, ItemStack> getter;
        private final BiConsumer<PlayerEntity, ItemStack> setter;

        public AccessoryInventory(PlayerEntity owner, Function<PlayerEntity, ItemStack> getter,
                                  BiConsumer<PlayerEntity, ItemStack> setter) {
            this.owner = owner;
            this.getter = getter;
            this.setter = setter;
        }

        @Override public int size() { return 1; }
        @Override public boolean isEmpty() { return getter.apply(owner).isEmpty(); }
        @Override public ItemStack getStack(int slot) { return slot == 0 ? getter.apply(owner) : ItemStack.EMPTY; }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            if (slot != 0 || amount <= 0) return ItemStack.EMPTY;
            ItemStack current = getter.apply(owner).copy();
            if (current.isEmpty()) return ItemStack.EMPTY;
            ItemStack removed = current.split(amount);
            setter.accept(owner, current);
            return removed;
        }

        @Override
        public ItemStack removeStack(int slot) {
            if (slot != 0) return ItemStack.EMPTY;
            ItemStack current = getter.apply(owner).copy();
            setter.accept(owner, ItemStack.EMPTY);
            return current;
        }

        @Override public void setStack(int slot, ItemStack stack) { if (slot == 0) setter.accept(owner, stack); }
        // Re-store the stack so in-place changes (count, NBT) are synced too.
        @Override public void markDirty() { setter.accept(owner, getter.apply(owner)); }
        @Override public boolean canPlayerUse(PlayerEntity player) { return player == owner; }
        @Override public void clear() { setter.accept(owner, ItemStack.EMPTY); }
    }
}
