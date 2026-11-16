package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/** Dyeing a bundle (1.21.2): any bundle plus a dye gives that colour of bundle, keeping what's inside it. */
public class BundleDyeRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<BundleDyeRecipe> SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
            new Identifier("gallifrey", "bundle_dye"), new SpecialRecipeSerializer<>(BundleDyeRecipe::new));

    public BundleDyeRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    private static ItemStack[] find(RecipeInputInventory inventory) {
        ItemStack bundle = null, dye = null;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BundleItem && bundle == null) bundle = stack;
            else if (stack.getItem() instanceof DyeItem && dye == null) dye = stack;
            else return null;
        }
        return bundle != null && dye != null ? new ItemStack[]{bundle, dye} : null;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        ItemStack[] found = find(inventory);
        return found != null && !found[0].isOf(BackportExtras.DYED_BUNDLES[((DyeItem) found[1].getItem()).getColor().getId()]);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack[] found = find(inventory);
        if (found == null) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(BackportExtras.DYED_BUNDLES[((DyeItem) found[1].getItem()).getColor().getId()]);
        if (found[0].hasNbt()) out.setNbt(found[0].getNbt().copy());
        return out;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    static void register() {
    }
}
