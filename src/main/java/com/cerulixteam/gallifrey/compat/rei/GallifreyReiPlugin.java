package com.cerulixteam.gallifrey.compat.rei;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.block.custom.TapperBlock;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * REI integration: the same "Tree Tapping" page as the JEI plugin. Vanilla-type recipes
 * (crafting, smelting, smithing...) show up in REI on their own; this only adds the tapper.
 *
 * REI finds this through the "rei_client" entrypoint in fabric.mod.json, so it's only loaded
 * when REI is installed. Nothing else in the mod may reference this package.
 */
public class GallifreyReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<TapDisplay> TAPPING =
            CategoryIdentifier.of(GallifreyMod.MOD_ID, "tapper");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new TapCategory());
        registry.addWorkstations(TAPPING, EntryStacks.of(GallifreyModBlocks.TREE_TAPPER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // Same list the block itself uses, so REI can never drift out of date.
        for (TapperBlock.Tap tap : TapperBlock.taps()) {
            registry.add(new TapDisplay(tap));
        }
    }

    /** Inputs are [log, leaves]; the leaves are only needed nearby, not used up. */
    public static class TapDisplay extends BasicDisplay {
        public TapDisplay(TapperBlock.Tap tap) {
            super(List.of(EntryIngredients.of(tap.log()), EntryIngredients.of(tap.leaves())),
                    List.of(EntryIngredients.of(tap.product())));
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return TAPPING;
        }
    }

    /** [log] [leaves]  ->  [product], same layout as the JEI page. */
    public static class TapCategory implements DisplayCategory<TapDisplay> {
        @Override
        public CategoryIdentifier<? extends TapDisplay> getCategoryIdentifier() {
            return TAPPING;
        }

        @Override
        public Text getTitle() {
            return Text.translatable("jei.gallifrey.tapper");
        }

        @Override
        public Renderer getIcon() {
            return EntryStacks.of(GallifreyModBlocks.TREE_TAPPER);
        }

        @Override
        public int getDisplayHeight() {
            return 36;
        }

        @Override
        public List<Widget> setupDisplay(TapDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            int x = bounds.getCenterX() - 52;
            int y = bounds.getCenterY() - 9;
            List<EntryIngredient> inputs = display.getInputEntries();
            widgets.add(Widgets.createSlot(new Point(x + 5, y + 1)).entries(inputs.get(0)).markInput());
            widgets.add(Widgets.createSlot(new Point(x + 25, y + 1)).entries(inputs.get(1)).markInput());
            widgets.add(Widgets.createArrow(new Point(x + 49, y)));
            widgets.add(Widgets.createSlot(new Point(x + 83, y + 1))
                    .entries(display.getOutputEntries().get(0)).markOutput());
            return widgets;
        }
    }
}
