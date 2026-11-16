package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class PrehistoricToolMaterial implements ToolMaterial {
    public static final PrehistoricToolMaterial INSTANCE = new PrehistoricToolMaterial();
    private PrehistoricToolMaterial() {}
    @Override public int getDurability() { return 1950; }
    @Override public float getMiningSpeedMultiplier() { return 8.75F; }
    @Override public float getAttackDamage() { return 4.25F; }
    @Override public int getMiningLevel() { return 3; }
    @Override public int getEnchantability() { return 12; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(com.cerulixteam.gallifrey.item.GallifreyModItems.PREHISTORIC_INGOT); }
}
