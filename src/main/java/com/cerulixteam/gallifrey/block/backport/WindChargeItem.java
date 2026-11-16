package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Wind Charge (1.21): throw it for a gust that pushes things (you included) away. Half a second's cooldown. */
public class WindChargeItem extends Item {
    public WindChargeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            WindChargeEntity charge = new WindChargeEntity(world, user);
            charge.setItem(stack);
            charge.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(charge);
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.6F);
        user.getItemCooldownManager().set(this, 10);
        if (!user.getAbilities().creativeMode) stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }
}
