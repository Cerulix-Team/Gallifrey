package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** Iron-tier armour stats for the Timelord robes. These robes do not provide oxygen. Repaired with red wool. */
public final class TimelordRobeArmorMaterial implements ArmorMaterial {
    public static final TimelordRobeArmorMaterial INSTANCE = new TimelordRobeArmorMaterial();
    private TimelordRobeArmorMaterial() {}
    @Override public int getDurability(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 195; case LEGGINGS -> 225; case CHESTPLATE -> 240; case HELMET -> 165; };
    }
    @Override public int getProtection(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 2; case LEGGINGS -> 5; case CHESTPLATE -> 6; case HELMET -> 2; };
    }
    @Override public int getEnchantability() { return 9; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.RED_WOOL); }
    @Override public String getName() { return "timelord_robes"; }
    @Override public float getToughness() { return 0.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
