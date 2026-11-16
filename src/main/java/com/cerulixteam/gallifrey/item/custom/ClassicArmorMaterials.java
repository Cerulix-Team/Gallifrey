package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

/**
 * Classic armour. Same stats as the vanilla tiers, but with their own names so the
 * classic worn textures are used instead of the modern ones.
 *
 * Worn textures (1.20.1 always looks in the minecraft namespace):
 *   assets/minecraft/textures/models/armor/gallifrey_classic_<tier>_layer_1.png
 *   assets/minecraft/textures/models/armor/gallifrey_classic_<tier>_layer_2.png
 * Leather also uses the _overlay versions.
 */
public enum ClassicArmorMaterials implements ArmorMaterial {
    //                                     durability boots legs chest helmet ench  sound                                   tough
    LEATHER(  "gallifrey_classic_leather",   5,        1,    2,   3,    1,     15,  SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,   0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_LEATHER, Items.LEATHER)),
    CHAINMAIL("gallifrey_classic_chainmail", 15,       1,    4,   5,    2,     12,  SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,     0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_IRON_INGOT, Items.IRON_INGOT)),
    IRON(     "gallifrey_classic_iron",      15,       2,    5,   6,    2,     9,   SoundEvents.ITEM_ARMOR_EQUIP_IRON,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_IRON_INGOT, Items.IRON_INGOT)),
    GOLD(     "gallifrey_classic_gold",      7,        1,    3,   5,    2,     25,  SoundEvents.ITEM_ARMOR_EQUIP_GOLD,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_GOLD_INGOT, Items.GOLD_INGOT)),
    DIAMOND(  "gallifrey_classic_diamond",   33,       3,    6,   8,    3,     10,  SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,   2.0F,
            () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_DIAMOND, Items.DIAMOND));

    private final String name;
    private final int durabilityMultiplier;
    private final int boots, legs, chest, helmet;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final Supplier<Ingredient> repair;

    ClassicArmorMaterials(String name, int durabilityMultiplier, int boots, int legs, int chest, int helmet,
                          int enchantability, SoundEvent equipSound, float toughness, Supplier<Ingredient> repair) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.boots = boots;
        this.legs = legs;
        this.chest = chest;
        this.helmet = helmet;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.repair = repair;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        int base = switch (type) {
            case BOOTS -> 13;
            case LEGGINGS -> 15;
            case CHESTPLATE -> 16;
            case HELMET -> 11;
        };
        return base * durabilityMultiplier;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> boots;
            case LEGGINGS -> legs;
            case CHESTPLATE -> chest;
            case HELMET -> helmet;
        };
    }

    @Override public int getEnchantability() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
    @Override public String getName() { return name; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return 0.0F; }
}
