package com.cerulixteam.gallifrey.compat.farmersdelight;

import net.minecraft.block.Block;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * A Farmer's Delight knife made of a Gallifrey metal. Farmer's Delight recognises knives by the
 * c:tools/knives and farmersdelight:tools/knives item tags (cutting board, straw, cake slicing),
 * so this only has to be a tool that mines what FD's knives mine, with FD's knife stats.
 */
public class GallifreyKnifeItem extends MiningToolItem {
    public static final TagKey<Block> MINEABLE_WITH_KNIFE =
            TagKey.of(RegistryKeys.BLOCK, new Identifier("farmersdelight", "mineable/knife"));

    public GallifreyKnifeItem(ToolMaterial material, Settings settings) {
        // Farmer's Delight's own knives: +0.5 attack damage, -2.0 attack speed.
        super(0.5F, -2.0F, material, MINEABLE_WITH_KNIFE, settings);
    }
}
