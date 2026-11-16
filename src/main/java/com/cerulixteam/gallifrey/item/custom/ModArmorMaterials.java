package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

/**
 * Armour tiers for the mod's metals.
 *
 * STEEL is a small step above iron while remaining well below diamond.
 * DALEKANIUM copies vanilla IRON exactly.
 * METALERTANIUM copies vanilla NETHERITE exactly.
 * WHITE_POINT_STAR is the top tier, roughly two and a half times netherite: 2.5x durability,
 *   20% more armour points per piece (the game caps worn armour at 30), 2.5x toughness and
 *   2.5x knockback resistance. Its textures are white_point_star_layer_1/2.
 *
 * The name is also the texture name. In 1.20.1 the game always looks for worn armour at
 *   assets/minecraft/textures/models/armor/<name>_layer_1.png  (helmet, chestplate, boots)
 *   assets/minecraft/textures/models/armor/<name>_layer_2.png  (leggings)
 */
public enum ModArmorMaterials implements ArmorMaterial {
    //            name            durability  boots legs chest helmet  ench  equip sound                          toughness  knockback
    STEEL(        "steel",         18,         2,    5,   7,    2,      10,   SoundEvents.ITEM_ARMOR_EQUIP_IRON,      0.5F,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.STEEL_INGOT)),
    EMERALD(      "emerald",        33,         3,    6,   8,    3,      10,   SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,    2.0F,      0.0F,
            () -> Ingredient.fromTag(net.minecraft.registry.tag.TagKey.of(net.minecraft.registry.RegistryKeys.ITEM, new net.minecraft.util.Identifier("gallifrey", "emerald_crafting_materials")))),
    RUBY(         "ruby",          15,         2,    5,   6,    2,      14,   SoundEvents.ITEM_ARMOR_EQUIP_IRON,      0.0F,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.RUBY)),
    SAPPHIRE(     "sapphire",      15,         2,    5,   6,    2,      14,   SoundEvents.ITEM_ARMOR_EQUIP_IRON,      0.0F,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.SAPPHIRE)),
    DALEKANIUM(   "dalekanium",    15,         2,    5,   6,    2,      9,    SoundEvents.ITEM_ARMOR_EQUIP_IRON,      0.0F,      0.0F,
            () -> Ingredient.ofItems(GallifreyModItems.DALEKANIUM_INGOT)),
    METALERTANIUM("metalertanium", 37,         3,    6,   8,    3,      15,   SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F,      0.1F,
            () -> Ingredient.ofItems(GallifreyModItems.METALERTANIUM_INGOT)),
    WHITE_POINT_STAR("white_point_star", 92,   4,    8,   10,   4,      20,   SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 7.5F,      0.25F,
            () -> Ingredient.ofItems(GallifreyModItems.WHITE_POINT_STAR_RESIDUE));

    private final String name;
    private final int durabilityMultiplier;
    private final int bootsProtection;
    private final int leggingsProtection;
    private final int chestplateProtection;
    private final int helmetProtection;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, int bootsProtection, int leggingsProtection,
                      int chestplateProtection, int helmetProtection, int enchantability, SoundEvent equipSound,
                      float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.bootsProtection = bootsProtection;
        this.leggingsProtection = leggingsProtection;
        this.chestplateProtection = chestplateProtection;
        this.helmetProtection = helmetProtection;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        // Same base numbers vanilla uses for every armour material.
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
            case BOOTS -> bootsProtection;
            case LEGGINGS -> leggingsProtection;
            case CHESTPLATE -> chestplateProtection;
            case HELMET -> helmetProtection;
        };
    }

    @Override public int getEnchantability() { return enchantability; }
    @Override public SoundEvent getEquipSound() { return equipSound; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    @Override public String getName() { return name; }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return knockbackResistance; }
}
