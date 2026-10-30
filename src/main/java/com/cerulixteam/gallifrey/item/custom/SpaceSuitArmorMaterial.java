package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/** Standalone spacesuit armour material; does not depend on another space mod. */
public final class SpaceSuitArmorMaterial implements ArmorMaterial {
    public static final SpaceSuitArmorMaterial INSTANCE = new SpaceSuitArmorMaterial();
    private SpaceSuitArmorMaterial() {}
    @Override public int getDurability(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 260; case LEGGINGS -> 300; case CHESTPLATE -> 320; case HELMET -> 220; };
    }
    @Override public int getProtection(ArmorItem.Type type) {
        return switch (type) { case BOOTS -> 2; case LEGGINGS -> 5; case CHESTPLATE -> 6; case HELMET -> 2; };
    }
    @Override public int getEnchantability() { return 12; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    @Override public String getName() { return "spacesuit"; }
    @Override public float getToughness() { return 1.0F; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
