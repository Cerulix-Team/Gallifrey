package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.BackportEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * The enchanting table only knows vanilla's item groups, so it would offer the backported Mace and Spear
 * enchantments on any tool. This keeps them to the items they're for (and books), and adds them for those.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void gallifrey$backportEnchantments(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> entries = cir.getReturnValue();
        entries.removeIf(entry -> entry.enchantment instanceof BackportEnchantment backport && !backport.tableAccepts(stack));
        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            if (!(enchantment instanceof BackportEnchantment backport) || !backport.tableAccepts(stack)) continue;
            if (backport.isTreasure() && !treasureAllowed) continue;
            if (entries.stream().anyMatch(e -> e.enchantment == enchantment)) continue;
            for (int level = backport.getMaxLevel(); level >= backport.getMinLevel(); level--) {
                if (power >= backport.getMinPower(level) && power <= backport.getMaxPower(level)) {
                    entries.add(new EnchantmentLevelEntry(backport, level));
                    break;
                }
            }
        }
    }
}
