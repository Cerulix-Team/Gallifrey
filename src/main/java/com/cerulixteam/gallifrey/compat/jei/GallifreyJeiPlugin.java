package com.cerulixteam.gallifrey.compat.jei;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.Identifier;

/**
 * JEI integration. JEI finds this class through the "jei_mod_plugin" entrypoint in
 * fabric.mod.json, so it is only ever loaded when JEI is installed.
 * Nothing else in the mod may reference this package.
 */
@JeiPlugin
public class GallifreyJeiPlugin implements IModPlugin {

   @Override
   public Identifier getPluginUid() {
       return new Identifier(GallifreyMod.MOD_ID, "jei_plugin");
   }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new TapperCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

   @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Same list the block itself uses, so JEI can never drift out of date.
        registration.addRecipes(TapperCategory.TYPE, TapperBlock.taps());
    }

   @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Makes "Tree Tapper" show as the workstation tab, and pressing U on the tapper lists everything it makes.
        registration.addRecipeCatalysts(TapperCategory.TYPE, GallifreyModBlocks.TREE_TAPPER);
    }
}
