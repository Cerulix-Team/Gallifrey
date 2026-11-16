package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.function.Supplier;

/**
 * Tool tiers for the mod's metals and classic equipment.
 * STEEL is a small step above iron while remaining well below diamond.
 * DALEKANIUM copies vanilla IRON exactly.
 * METALERTANIUM copies vanilla NETHERITE exactly.
 * WHITE_POINT_STAR is the top tier: 2.5x netherite durability, faster mining and roughly double
 *   netherite damage (a White Point Star sword hits for 16, netherite for 8).
 * MARS_STONE and SKARO_STONE copy vanilla STONE exactly.
 *
 * Order of the numbers: mining level, durability, mining speed, attack damage bonus, enchantability.
 */
public enum ModToolMaterials implements ToolMaterial {
    STEEL(2, 300, 6.5F, 2.25F, 14, () -> Ingredient.ofItems(GallifreyModItems.STEEL_INGOT)),
    EMERALD(3, 1561, 8.0F, 3.0F, 10, () -> Ingredient.fromTag(net.minecraft.registry.tag.TagKey.of(net.minecraft.registry.RegistryKeys.ITEM, new net.minecraft.util.Identifier("gallifrey", "emerald_crafting_materials")))),
    RUBY(2, 250, 6.0F, 2.0F, 14, () -> Ingredient.ofItems(GallifreyModItems.RUBY)),
    SAPPHIRE(2, 250, 6.0F, 2.0F, 14, () -> Ingredient.ofItems(GallifreyModItems.SAPPHIRE)),
    DALEKANIUM(2, 250, 6.0F, 2.0F, 14, () -> Ingredient.ofItems(GallifreyModItems.DALEKANIUM_INGOT)),
    METALERTANIUM(4, 2031, 9.0F, 4.0F, 15, () -> Ingredient.ofItems(GallifreyModItems.METALERTANIUM_INGOT)),
    WHITE_POINT_STAR(5, 5078, 15.0F, 12.0F, 20, () -> Ingredient.ofItems(GallifreyModItems.WHITE_POINT_STAR_RESIDUE)),

    // Planet stone tools: the same stats as vanilla stone tools, repaired with their own cobblestone.
    MARS_STONE(1, 131, 4.0F, 1.0F, 5, () -> Ingredient.ofItems(GallifreyModBlocks.MARS_COBBLESTONE)),
    SKARO_STONE(1, 131, 4.0F, 1.0F, 5, () -> Ingredient.ofItems(GallifreyModBlocks.SKARO_COBBLESTONE)),

    // Classic Tool Materials (matching vanilla stats)
    CLASSIC_WOOD(0, 59, 2.0F, 0.0F, 15, () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_STICK)),
    CLASSIC_STONE(1, 131, 4.0F, 1.0F, 5, () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_FLINT)), // or cobblestone if registered
    CLASSIC_IRON(2, 250, 6.0F, 2.0F, 14, () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_IRON_INGOT)),
    CLASSIC_GOLD(0, 32, 12.0F, 0.0F, 22, () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_GOLD_INGOT)),
    CLASSIC_DIAMOND(3, 1561, 8.0F, 3.0F, 10, () -> Ingredient.ofItems(GallifreyModItems.CLASSIC_DIAMOND));

    private final int miningLevel;
    private final int durability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    // A supplier, so the ingot is only looked up when something is actually repaired
    // (the ingot items do not exist yet while this enum is being created).
    private final Supplier<Ingredient> repairIngredient;

    ModToolMaterials(int miningLevel, int durability, float miningSpeed, float attackDamage,
                     int enchantability, Supplier<Ingredient> repairIngredient) {
        this.miningLevel = miningLevel;
        this.durability = durability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    @Override public int getDurability() { return durability; }
    @Override public float getMiningSpeedMultiplier() { return miningSpeed; }
    @Override public float getAttackDamage() { return attackDamage; }
    @Override public int getMiningLevel() { return miningLevel; }
    @Override public int getEnchantability() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
}