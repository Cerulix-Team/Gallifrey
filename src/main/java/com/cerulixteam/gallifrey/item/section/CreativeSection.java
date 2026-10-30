package com.cerulixteam.gallifrey.item.section;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One section of the Gallifrey creative tab (Wood Types, Tools, ...).
 *
 * This is NOT a real creative tab - it's plain data: a title, an icon and a list of
 * items. The single real tab ({@link com.cerulixteam.gallifrey.item.GallifreyCreativeTab})
 * shows every section's items in order, and the client-side sidebar lets the player
 * filter the tab down to one section.
 *
 * Common code (no client classes), so it's safe on a dedicated server.
 * Inspired by VanillaBackport's BundledTabs (MIT, ItsBlackGear).
 */
public final class CreativeSection {

    private final String id;
    private final Supplier<ItemStack> iconSupplier;
    private final Consumer<ItemGroup.Entries> contents;

    // Built lazily on first use, then reused. Fixed size for the whole session.
    private ItemStack icon;
    private List<ItemStack> stacks;

    private CreativeSection(String id, Supplier<ItemStack> icon, Consumer<ItemGroup.Entries> contents) {
        this.id = id;
        this.iconSupplier = icon;
        this.contents = contents;
    }

    /**
     * @param id       used for the translation key {@code creative_section.gallifrey.<id>}
     * @param icon     shown on the sidebar button
     * @param contents adds the items, written exactly like a normal tab's entries:
     *                 {@code entries -> { entries.add(GallifreyModBlocks.X); }}
     */
    public static CreativeSection of(String id, Supplier<ItemStack> icon, Consumer<ItemGroup.Entries> contents) {
        return new CreativeSection(id, icon, contents);
    }

    public Text getTitle() {
        return Text.translatable("creative_section.gallifrey." + this.id);
    }

    public ItemStack getIcon() {
        if (this.icon == null) this.icon = this.iconSupplier.get();
        return this.icon;
    }

    /** Adds this section's items to the real creative tab. */
    public void addTo(ItemGroup.Entries entries) {
        this.contents.accept(entries);
    }

    /** This section's items on their own, for the sidebar filter. */
    public List<ItemStack> getStacks() {
        if (this.stacks == null) {
            List<ItemStack> collected = new ArrayList<>();
            this.contents.accept((stack, visibility) -> {
                if (!stack.isEmpty() && visibility != ItemGroup.StackVisibility.SEARCH_TAB_ONLY) {
                    collected.add(stack);
                }
            });
            this.stacks = Collections.unmodifiableList(collected);
        }
        return this.stacks;
    }
}
