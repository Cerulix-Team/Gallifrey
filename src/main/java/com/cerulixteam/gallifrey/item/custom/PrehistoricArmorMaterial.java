package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class PrehistoricArmorMaterial implements ArmorMaterial {
    public static final PrehistoricArmorMaterial INSTANCE = new PrehistoricArmorMaterial();
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};

    private PrehistoricArmorMaterial() {}

    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getEquipmentSlot().getEntitySlotId()] * 36;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 3;
            case LEGGINGS -> 6;
            case CHESTPLATE -> 8;
            case HELMET -> 3;
        };
    }

    @Override
    public int getEnchantability() {
        return 12;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(GallifreyModItems.PREHISTORIC_INGOT);
    }

    @Override
    public String getName() {
        return "prehistoric";
    }

    @Override
    public float getToughness() {
        return 2.5F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.08F;
    }
}
