package com.cerulixteam.gallifrey.item;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.section.CreativeSection;
import com.cerulixteam.gallifrey.item.section.GallifreyTabSections;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;


public class GallifreyCreativeTab {
    public static final ItemGroup TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(GallifreyModBlocks.TARDIS_SAPLING))
            .displayName(Text.translatable("itemGroup.gallifrey.gallifrey"))
            .entries((context, entries) -> {
                // Several old section lists can overlap. Minecraft 1.20.1 throws
                // if the same ItemStack/item is added twice to a creative tab.
                Set<Item> seenItems = new HashSet<>();
                for (CreativeSection section : GallifreyTabSections.ALL) {
                    for (ItemStack stack : section.getStacks()) {
                        if (seenItems.add(stack.getItem())) {
                            entries.add(stack.copy());
                        }
                    }
                }
            })
            .build();

    public static void register() {
        Registry.register(
                Registries.ITEM_GROUP,
                GallifreyMod.id("gallifrey"),
                TAB
        );
        GallifreyMod.LOGGER.debug("[Gallifrey] Creative tab registered.");
    }
}
