package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ominous Bottle (1.21): drink it for Bad Omen (level I to V, stored on the bottle as "Amplifier"), lasting
 * 100 minutes. Pillagers sometimes drop one.
 */
public class OminousBottleItem extends Item {
    public OminousBottleItem(Settings settings) {
        super(settings);
    }

    public static int amplifier(ItemStack stack) {
        return stack.hasNbt() ? Math.max(0, Math.min(4, stack.getNbt().getInt("Amplifier"))) : 0;
    }

    public static ItemStack withAmplifier(ItemStack stack, int amplifier) {
        stack.getOrCreateNbt().putInt("Amplifier", amplifier);
        return stack;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, amplifier(stack), false, false, true));
            world.playSound(null, user.getBlockPos(), com.cerulixteam.gallifrey.block.backport.BackportSounds.EVENT_MOB_EFFECT_BAD_OMEN, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }
        if (!(user instanceof PlayerEntity player && player.getAbilities().creativeMode)) stack.decrement(1);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("effect.minecraft.bad_omen").append(" ")
                .append(Text.translatable("enchantment.level." + (amplifier(stack) + 1))).formatted(Formatting.BLUE));
    }
}
