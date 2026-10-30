package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Which armour keeps you alive on Mars, Mondas and the Moon.
 *
 * Any full set of armour in the gallifrey:airtight_suits item tag counts. The tag holds the
 * Gallifrey space suit, plus every Ad Astra space suit (#ad_astra:space_suit_items, which also
 * covers the netherite and jet suits) when Ad Astra is installed. Without Ad Astra that part of
 * the tag is simply skipped. Datapacks can add other mods' suits to the tag too.
 *
 * The oxygen supply works the same for every suit (see SpaceSuitOxygenHandler). The jetpack is
 * still a Gallifrey suit feature only.
 */
public final class SpaceSuits {
    private SpaceSuits() {}

    public static final TagKey<Item> AIRTIGHT_SUITS =
            TagKey.of(RegistryKeys.ITEM, new Identifier("gallifrey", "airtight_suits"));

    /** True when every armour slot holds a piece of an airtight suit. */
    public static boolean hasFullSuit(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (!stack.isIn(AIRTIGHT_SUITS)) return false;
        }
        return true;
    }
}
