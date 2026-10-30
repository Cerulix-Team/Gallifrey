package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Tooltip lines for abilities that aren't otherwise visible:
 *  - dalekanium, metalertanium and White Point Star boots: slow falling, no fall damage, and
 *    sneaking to drop (see HoverBoots);
 *  - White Point Star armour: the faint white glow when worn (see WhitePointStarGlow).
 */
public final class GearTooltips {
    private GearTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            Item item = stack.getItem();
            if (item == GallifreyModItems.DALEKANIUM_BOOTS
                    || item == GallifreyModItems.METALERTANIUM_BOOTS
                    || item == GallifreyModItems.WHITE_POINT_STAR_BOOTS) {
                add(lines, "tooltip.gallifrey.hover_boots", Formatting.BLUE);
                add(lines, "tooltip.gallifrey.hover_boots.sneak", Formatting.GRAY);
            }
            if (item instanceof ArmorItem armor
                    && armor.getMaterial() == com.cerulixteam.gallifrey.item.custom.ModArmorMaterials.WHITE_POINT_STAR) {
                add(lines, "tooltip.gallifrey.white_point_star_glow", Formatting.AQUA);
            }
        });
    }

    /** Inserted straight after the item name, above enchantments and attributes. */
    private static void add(List<Text> lines, String key, Formatting colour) {
        int at = Math.min(1, lines.size());
        // Keep our lines in the order they're added.
        while (at < lines.size() && lines.get(at).getContent() instanceof net.minecraft.text.TranslatableTextContent t
                && t.getKey().startsWith("tooltip.gallifrey.")) {
            at++;
        }
        lines.add(at, Text.translatable(key).formatted(colour));
    }
}
