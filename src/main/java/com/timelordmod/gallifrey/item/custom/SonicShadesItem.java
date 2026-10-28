package com.timelordmod.gallifrey.item.custom;

import net.minecraft.item.ItemStack;
import com.timelordmod.gallifrey.item.custom.SonicShadesSlotAccess;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * Wearable sonic sunglasses. The actual sonic logic is inherited from the
 * Gallifrey Sonic Screwdriver so the shades use the same power, modes, casing
 * data and SonicHandler behaviour without pulling in AWT/AI code.
 *
 * While worn, the client-side "Use Sonic Shades" keybind invokes the same
 * SonicScrewdriver activation logic against the player's look target.
 */
public class SonicShadesItem extends SonicScrewdriver {

    public SonicShadesItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack held = user.getStackInHand(hand);
        if (!world.isClient) {
            // Through GallifreyAccessories, so it's the Trinkets face slot when Trinkets is installed.
            ItemStack worn = GallifreyAccessories.getShades(user).copy();
            ItemStack replacement = held.copyWithCount(1);
            if (!worn.isEmpty()) {
                GallifreyAccessories.setShades(user, ItemStack.EMPTY);
                if (!user.getInventory().insertStack(worn)) {
                    user.dropItem(worn, false);
                }
            }
            GallifreyAccessories.setShades(user, replacement);
            held.decrement(1);
            user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
        }
        return TypedActionResult.success(held, world.isClient);
    }
}
