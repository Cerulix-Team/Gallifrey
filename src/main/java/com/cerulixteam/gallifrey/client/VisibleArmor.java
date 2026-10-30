package com.cerulixteam.gallifrey.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

/**
 * The armour piece that's actually drawn on a player, which isn't always the one they're wearing.
 *
 * With Cosmetic Armor installed, an item in a cosmetic slot is drawn instead of the real armour
 * (unless the real piece is in #cosmetic-armor:always_visible). Gallifrey hides parts of the
 * player model under the space suit and Timelord robes, so that has to follow what's drawn:
 * otherwise a real space suit under cosmetic iron armour left see-through gaps, and a cosmetic
 * space suit over real armour had the player poking through it.
 *
 * Only for looks. Protection and the space suit's air supply still go by the real armour.
 *
 * Mirrors Cosmetic Armor 1.6.0 (1.20.1). No compile-time dependency; without the mod this is
 * just the worn armour.
 */
public final class VisibleArmor {
    private VisibleArmor() {}

    private static final TagKey<Item> ALWAYS_VISIBLE =
            TagKey.of(RegistryKeys.ITEM, new Identifier("cosmetic-armor", "always_visible"));

    private static boolean looked = false;
    private static Method getCosmeticArmor; // static CosmeticArmor#getCosmeticArmor(LivingEntity, EquipmentSlot)

    public static ItemStack get(LivingEntity entity, EquipmentSlot slot) {
        ItemStack worn = entity.getEquippedStack(slot);
        Method method = cosmeticLookup();
        if (method == null) return worn;
        try {
            ItemStack cosmetic = (ItemStack) method.invoke(null, entity, slot);
            if (cosmetic != null && !cosmetic.isEmpty() && (worn.isEmpty() || !worn.isIn(ALWAYS_VISIBLE))) {
                return cosmetic;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // fall back to the worn piece
        }
        return worn;
    }

    private static Method cosmeticLookup() {
        if (!looked) {
            looked = true;
            if (FabricLoader.getInstance().isModLoaded("cosmetic-armor")) {
                try {
                    getCosmeticArmor = Class.forName("io.github.apace100.cosmetic_armor.CosmeticArmor")
                            .getMethod("getCosmeticArmor", LivingEntity.class, EquipmentSlot.class);
                } catch (ReflectiveOperationException | RuntimeException e) {
                    getCosmeticArmor = null;
                }
            }
        }
        return getCosmeticArmor;
    }
}
