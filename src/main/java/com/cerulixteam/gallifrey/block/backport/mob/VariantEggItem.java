package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Blue and Brown Eggs (1.21.5): laid by cold and warm chickens, and hatch chicks of that kind. */
public class VariantEggItem extends Item {
    /** 1 = warm (brown), 2 = cold (blue). See AnimalVariants. */
    public final int variant;

    public VariantEggItem(int variant, Settings settings) {
        super(settings);
        this.variant = variant;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F);
        if (!world.isClient) {
            VariantEggEntity egg = new VariantEggEntity(world, user, this.variant);
            egg.setItem(stack);
            egg.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(egg);
        }
        if (!user.getAbilities().creativeMode) stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient());
    }
}
