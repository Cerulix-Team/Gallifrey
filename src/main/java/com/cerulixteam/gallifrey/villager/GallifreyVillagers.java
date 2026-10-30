package com.cerulixteam.gallifrey.villager;

import com.google.common.collect.ImmutableSet;
import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

/**
 * The Time Lord Trader villager profession. Any unemployed villager near a Gallifreyan Workbench
 * takes the job (the workbench is also listed in
 * data/minecraft/tags/point_of_interest_type/acquirable_job_site.json, which vanilla needs before
 * a villager will claim it). The outfit is
 * assets/gallifrey/textures/entity/villager/profession/time_lord_trader.png.
 *
 * Call {@link #register()} from GallifreyMod.onInitialize, after the items and blocks are registered.
 */
public final class GallifreyVillagers {
    private GallifreyVillagers() {}

    public static final Identifier WORKBENCH_POI_ID = new Identifier(GallifreyMod.MOD_ID, "gallifreyan_workbench");
    public static final RegistryKey<PointOfInterestType> WORKBENCH_POI_KEY =
            RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, WORKBENCH_POI_ID);

    public static VillagerProfession TIME_LORD_TRADER;

    public static void register() {
        // One villager per workbench, found from up to 1 block away (the same as vanilla job sites).
        PointOfInterestHelper.register(WORKBENCH_POI_ID, 1, 1, GallifreyModBlocks.GALLIFREYAN_WORKBENCH);

        TIME_LORD_TRADER = Registry.register(Registries.VILLAGER_PROFESSION,
                new Identifier(GallifreyMod.MOD_ID, "time_lord_trader"),
                new VillagerProfession("time_lord_trader",
                        entry -> entry.matchesKey(WORKBENCH_POI_KEY),
                        entry -> entry.matchesKey(WORKBENCH_POI_KEY),
                        ImmutableSet.of(), ImmutableSet.of(),
                        SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER));

        registerTrades();
        GallifreyMod.LOGGER.info("[Gallifrey] Registered the Time Lord Trader");
    }

    // ---------------- trades ----------------
    // Each level has several trades in its pool; like vanilla, a villager picks 2 of them at random
    // when it reaches that level, so two Time Lord Traders won't always offer the same things.
    // Numbers in each line: counts, emeralds, uses before restocking, and the villager XP it gives.

    private static void registerTrades() {
        // Novice
        TradeOfferHelper.registerVillagerOffers(TIME_LORD_TRADER, 1, trades -> {
            trades.add(buy(GallifreyModItems.TREEBORG_PASTE, 6, 1, 16, 2));
            trades.add(sell(GallifreyModItems.BLANK_CIRCUIT, 1, 3, 12, 1));
            // Classic emeralds, brought back from the past.
            trades.add(sell(GallifreyModItems.CLASSIC_EMERALD, 1, 2, 16, 1));
            trades.add(buy(GallifreyModItems.CLASSIC_EMERALD, 1, 1, 16, 2));
        });
        // Apprentice
        TradeOfferHelper.registerVillagerOffers(TIME_LORD_TRADER, 2, trades -> {
            trades.add(buy(GallifreyModItems.RAW_SONIC_CRYSTAL, 4, 1, 16, 10));
            trades.add(sell(GallifreyModItems.FEZ, 1, 5, 12, 5));
            trades.add(swap(GallifreyModItems.CLASSIC_EMERALD, 3, GallifreyModItems.CLASSIC_GOLD_INGOT, 2, 12, 5));
        });
        // Journeyman
        TradeOfferHelper.registerVillagerOffers(TIME_LORD_TRADER, 3, trades -> {
            trades.add(sell(GallifreyModItems.LOCATION_CIRCUIT, 1, 8, 8, 10));
            trades.add(sell(GallifreyModItems.INTERFACE_CIRCUIT, 1, 8, 8, 10));
            trades.add(swap(GallifreyModItems.CLASSIC_EMERALD, 6, GallifreyModItems.CLASSIC_DIAMOND, 1, 8, 10));
        });
        // Expert
        TradeOfferHelper.registerVillagerOffers(TIME_LORD_TRADER, 4, trades -> {
            trades.add(buy(GallifreyModItems.REFINED_SONIC_CRYSTAL, 2, 1, 12, 15));
            trades.add(sell(GallifreyModItems.DIMENSION_CIRCUIT, 1, 12, 6, 15));
            trades.add(swap(GallifreyModItems.CLASSIC_EMERALD, 12, GallifreyModItems.CLASSIC_DIAMOND_PICKAXE, 1, 3, 15));
        });
        // Master
        TradeOfferHelper.registerVillagerOffers(TIME_LORD_TRADER, 5, trades -> {
            trades.add(sell(GallifreyModItems.WHITE_POINT_STAR, 1, 20, 4, 30));
            trades.add(sellFor(GallifreyModItems.REFINED_SONIC_CRYSTAL, 1, 24, GallifreyModItems.SONIC_SCREWDRIVER, 1, 3, 30));
            trades.add(swap(GallifreyModItems.CLASSIC_EMERALD, 16, GallifreyModItems.CLASSIC_APPLE_GOLDEN, 1, 3, 30));
        });
    }

    /** The villager buys {@code count} of {@code item} for {@code emeralds} emeralds. */
    private static TradeOffers.Factory buy(ItemConvertible item, int count, int emeralds, int maxUses, int xp) {
        return (entity, random) -> new TradeOffer(new ItemStack(item, count), new ItemStack(Items.EMERALD, emeralds),
                maxUses, xp, 0.05F);
    }

    /** The villager sells {@code count} of {@code item} for {@code emeralds} emeralds. */
    private static TradeOffers.Factory sell(ItemConvertible item, int count, int emeralds, int maxUses, int xp) {
        return (entity, random) -> new TradeOffer(new ItemStack(Items.EMERALD, emeralds), new ItemStack(item, count),
                maxUses, xp, 0.05F);
    }

    /** The villager takes {@code inCount} of {@code in} and gives {@code outCount} of {@code out}, no emeralds involved. */
    private static TradeOffers.Factory swap(ItemConvertible in, int inCount, ItemConvertible out, int outCount, int maxUses, int xp) {
        return (entity, random) -> new TradeOffer(new ItemStack(in, inCount), new ItemStack(out, outCount),
                maxUses, xp, 0.05F);
    }

    /** The villager sells {@code item} for emeralds plus another item. */
    private static TradeOffers.Factory sellFor(ItemConvertible extra, int extraCount, int emeralds,
                                               ItemConvertible item, int count, int maxUses, int xp) {
        return (entity, random) -> new TradeOffer(new ItemStack(Items.EMERALD, emeralds), new ItemStack(extra, extraCount),
                new ItemStack(item, count), maxUses, xp, 0.05F);
    }
}