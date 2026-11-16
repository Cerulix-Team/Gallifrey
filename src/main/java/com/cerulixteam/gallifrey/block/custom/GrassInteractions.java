package com.cerulixteam.gallifrey.block.custom;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;

/** Lets hoes and shovels work on the classic and wasted grass and dirt. Call register() from GallifreyMod.onInitialize(). */
public final class GrassInteractions {
    private GrassInteractions() {}

    public static void register() {
        Block[] soils = {
                GallifreyModBlocks.CLASSIC_GRASS, GallifreyModBlocks.CLASSIC_DIRT,
                GallifreyModBlocks.WASTED_GRASS, GallifreyModBlocks.WASTED_DIRT
        };
        for (Block soil : soils) {
            // Hoe: turns into farmland (only when there is air above, same as vanilla)
            TillableBlockRegistry.register(soil, HoeItem::canTillFarmland, Blocks.FARMLAND.getDefaultState());
            // Shovel: turns into a dirt path
            FlattenableBlockRegistry.register(soil, Blocks.DIRT_PATH.getDefaultState());
        }
    }
}
