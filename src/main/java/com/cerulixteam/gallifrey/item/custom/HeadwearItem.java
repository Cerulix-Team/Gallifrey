package com.cerulixteam.gallifrey.item.custom;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HeadwearItem extends Item implements Equipment {
    public HeadwearItem(FabricItemSettings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);
        ItemStack head = user.getEquippedStack(EquipmentSlot.HEAD);

        // Fezzes go in the fez slot first, when it's empty.
        if (GallifreyAccessories.fitsFezSlot(held) && GallifreyAccessories.getFez(user).isEmpty()) {
            if (!world.isClient) {
                GallifreyAccessories.setFez(user, held.copyWithCount(1));
                held.decrement(1);
                user.playSound(getEquipSound(), 1.0F, 1.0F);
            }
            return TypedActionResult.success(held, world.isClient());
        }

        if (head.isEmpty()) {
            if (!world.isClient) {
                user.equipStack(EquipmentSlot.HEAD, held.copyWithCount(1));
                held.decrement(1);
            }
            return TypedActionResult.success(held, world.isClient());
        }

        return TypedActionResult.pass(held);
    }

    public static boolean isWorn(LivingEntity entity, Item item) {
        return entity.getEquippedStack(EquipmentSlot.HEAD).isOf(item);
    }
}
