package com.cerulixteam.gallifrey.compat.jei;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * The "Tree Tapping" page in JEI:  [log] [leaves]  ->  [product]
 */
public class TapperCategory implements IRecipeCategory<TapperBlock.Tap> {

   public static final RecipeType<TapperBlock.Tap> TYPE =
           RecipeType.create(GallifreyMod.MOD_ID, "tapper", TapperBlock.Tap.class);

    private static final int WIDTH = 104;
    private static final int HEIGHT = 26;

    private final IDrawable icon;
    private final IDrawable arrow;

    public TapperCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(GallifreyModBlocks.TREE_TAPPER));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<TapperBlock.Tap> getRecipeType() {
        return TYPE;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("jei.gallifrey.tapper");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TapperBlock.Tap recipe, IFocusGroup focuses) {
        // The log the tapper hangs on.
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
              .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.log()));

        // The leaves the tree needs. Shown for information; they are not used up.
        builder.addSlot(RecipeIngredientRole.CATALYST, 25, 5)
                .setStandardSlotBackground()
               .addItemStack(new ItemStack(recipe.leaves()));

        // What you collect from a full tapper.
        builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 5)
                .setStandardSlotBackground()
                .addItemStack(new ItemStack(recipe.product()));
    }

    @Override
    public void draw(TapperBlock.Tap recipe, IRecipeSlotsView recipeSlotsView, DrawContext context,
                     double mouseX, double mouseY) {
        arrow.draw(context, 50, 5);
    }
}
