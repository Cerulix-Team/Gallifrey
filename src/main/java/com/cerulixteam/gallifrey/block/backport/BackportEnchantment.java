package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Predicate;

/**
 * The 1.21 enchantments (Density, Breach and Wind Burst for the Mace, Lunge for Spears). 1.20.1
 * enchantments can only target vanilla item groups, so these say BREAKABLE and then narrow it down;
 * EnchantmentHelperMixin applies the same check to the enchanting table.
 */
public class BackportEnchantment extends Enchantment {
    private final int maxLevel;
    private final int minBase, perLevel, range;
    private final boolean treasure;
    private final Predicate<ItemStack> items;
    private final Enchantment[] incompatible;

    public BackportEnchantment(Rarity rarity, int maxLevel, int minBase, int perLevel, int range, boolean treasure,
                               Predicate<ItemStack> items, Enchantment... incompatible) {
        super(rarity, EnchantmentTarget.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.maxLevel = maxLevel;
        this.minBase = minBase;
        this.perLevel = perLevel;
        this.range = range;
        this.treasure = treasure;
        this.items = items;
        this.incompatible = incompatible;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public int getMinPower(int level) {
        return this.minBase + (level - 1) * this.perLevel;
    }

    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + this.range;
    }

    @Override
    public boolean isTreasure() {
        return this.treasure;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return this.items.test(stack);
    }

    /** Whether the enchanting table can put this on the item (books included). */
    public boolean tableAccepts(ItemStack stack) {
        return stack.isOf(Items.BOOK) || this.items.test(stack);
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        for (Enchantment e : this.incompatible) {
            if (e == other) return false;
        }
        if (other instanceof BackportEnchantment backport) {
            for (Enchantment e : backport.incompatible) {
                if (e == this) return false;
            }
        }
        return super.canAccept(other);
    }
}
