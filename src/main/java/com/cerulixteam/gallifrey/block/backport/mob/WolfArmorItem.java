package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * Wolf Armor (1.20.5), made from Armadillo Scutes. Put it on your tamed wolf; it takes the damage instead
 * of the wolf, and shears take it off. Repair it with scutes. Dye it like leather armour.
 */
public class WolfArmorItem extends Item implements DyeableItem {
    public WolfArmorItem(Settings settings) {
        super(settings);
    }

    /** Wolf armour has no colour until it's dyed (leather armour defaults to brown). */
    @Override
    public int getColor(ItemStack stack) {
        NbtCompound display = stack.getSubNbt(DISPLAY_KEY);
        return display != null && display.contains(COLOR_KEY, 99) ? display.getInt(COLOR_KEY) : -1;
    }

    /** Implemented on WolfEntity by mixin/backport/WolfArmorMixin. */
    public interface Holder {
        ItemStack gallifrey$getArmor();
    }
}
