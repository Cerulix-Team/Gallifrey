package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** Leather-tier armour stats for the lab coat: clothing, not armour. Repaired with white wool. */
public final class LabCoatArmorMaterial implements ArmorMaterial {
    public static final LabCoatArmorMaterial INSTANCE = new LabCoatArmorMaterial();
    private LabCoatArmorMaterial() {}
    @Override public int getDurability(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 65; case LEGGINGS -> 75; case CHESTPLATE -> 80; case HELMET -> 55; };
    }
    @Override public int getProtection(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 1; case LEGGINGS -> 2; case CHESTPLATE -> 3; case HELMET -> 1; };
    }
    @Override public int getEnchantability() { return 15; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.WHITE_WOOL); }
    @Override public String getName() { return "lab_coat"; }
    @Override public float getToughness() { return 0.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
