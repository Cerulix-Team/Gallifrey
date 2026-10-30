package com.cerulixteam.gallifrey.compat.emi;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * EMI integration: the same "Tree Tapping" page as the JEI plugin. Vanilla-type recipes
 * (crafting, smelting, smithing...) show up in EMI on their own; this only adds the tapper.
 *
 * EMI finds this through the "emi" entrypoint in fabric.mod.json, so it's only loaded when EMI
 * is installed. Nothing else in the mod may reference this package.
 */
public class GallifreyEmiPlugin implements EmiPlugin {
    public static final EmiStack TAPPER = EmiStack.of(GallifreyModBlocks.TREE_TAPPER);
    // Title comes from the lang key emi.category.gallifrey.tapper
    public static final EmiRecipeCategory TAPPING =
            new EmiRecipeCategory(new Identifier(GallifreyMod.MOD_ID, "tapper"), TAPPER);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(TAPPING);
        registry.addWorkstation(TAPPING, TAPPER);
        // Same list the block itself uses, so EMI can never drift out of date.
        for (TapperBlock.Tap tap : TapperBlock.taps()) {
            registry.addRecipe(new TapRecipe(tap));
        }
    }

    /** [log] [leaves]  ->  [product], same layout as the JEI page. */
    private static class TapRecipe extends BasicEmiRecipe {
        TapRecipe(TapperBlock.Tap tap) {
            super(TAPPING, new Identifier(GallifreyMod.MOD_ID,
                    "/tapper/" + Registries.BLOCK.getId(tap.log()).getPath()), 104, 26);
            inputs.add(EmiStack.of(tap.log()));
            // The leaves the tree needs. Shown for information; they're not used up.
            catalysts.add(EmiStack.of(tap.leaves()));
            outputs.add(EmiStack.of(tap.product()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(inputs.get(0), 4, 4);
            widgets.addSlot(catalysts.get(0), 24, 4).catalyst(true);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 48, 5);
            widgets.addSlot(outputs.get(0), 82, 4).recipeContext(this);
        }
    }
}
