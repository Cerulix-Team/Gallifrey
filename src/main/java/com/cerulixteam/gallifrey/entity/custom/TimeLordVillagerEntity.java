package com.cerulixteam.gallifrey.entity.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import net.minecraft.entity.passive.VillagerEntity;

/** Separate modern-model Time Lord villager with its own stable merchant offers. */
public class TimeLordVillagerEntity extends VillagerEntity {
    public TimeLordVillagerEntity(EntityType<? extends VillagerEntity> type, World world) {
        super(type, world);
    }

    /** Explicitly open the merchant screen on the server, matching the project's working custom merchant. */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }
            TradeOfferList offers = this.getOffers();
            if (offers.isEmpty()) {
                this.setHeadRollingTimeLeft(40);
                if (!this.getWorld().isClient) {
                    this.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0F, this.getSoundPitch());
                }
                return ActionResult.success(this.getWorld().isClient);
            }
            if (!this.getWorld().isClient) {
                this.setCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    /** MerchantEntity/VillagerEntity calls this once when its offer list is first requested. */
    @Override
    protected void fillRecipes() {
        TradeOfferList offers = this.getOffers();
        if (!offers.isEmpty()) return;

        offers.add(new TradeOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(GallifreyModItems.BLANK_CIRCUIT, 1), 12, 2, 0.05F));
        offers.add(new TradeOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(GallifreyModItems.REFINED_SONIC_CRYSTAL, 1), 12, 3, 0.05F));
        offers.add(new TradeOffer(new ItemStack(GallifreyModItems.CLASSIC_EMERALD, 4), new ItemStack(GallifreyModItems.BLANK_CIRCUIT, 1), 12, 2, 0.05F));
        offers.add(new TradeOffer(new ItemStack(GallifreyModItems.CLASSIC_EMERALD, 3), new ItemStack(GallifreyModItems.REFINED_SONIC_CRYSTAL, 1), 12, 3, 0.05F));
        offers.add(new TradeOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(GallifreyModItems.FEZ, 1), 4, 2, 0.05F));
        offers.add(new TradeOffer(new ItemStack(GallifreyModItems.CLASSIC_EMERALD, 6), new ItemStack(GallifreyModItems.FEZ, 1), 4, 2, 0.05F));

        // The rare offer is rolled once when the villager's offer list is first generated.
        if (this.random.nextFloat() < 0.01F) {
            offers.add(new TradeOffer(new ItemStack(Items.EMERALD, 20), new ItemStack(GallifreyModItems.OMEGA_HELMET, 1), 1, 10, 0.0F));
            offers.add(new TradeOffer(new ItemStack(GallifreyModItems.CLASSIC_EMERALD, 20), new ItemStack(GallifreyModItems.OMEGA_HELMET, 1), 1, 10, 0.0F));
        }
    }
}
