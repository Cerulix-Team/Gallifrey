package com.cerulixteam.gallifrey.compat.tardisrefined;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Set;

/**
 * Lets Gallifrey's sonics (screwdriver and Sonic Shades in hand) do everything TARDIS Refined's
 * Amethyst Screwdriver does, when TARDIS Refined is installed. All of Gallifrey's own sonic
 * features keep working.
 *
 * TARDIS Refined only recognises its own screwdriver item, so on the server this briefly hands
 * TARDIS Refined a stand-in Amethyst Screwdriver carrying the sonic's data, lets it do its thing,
 * then copies the results (mode, drawn points, linked manipulator) back onto the sonic. That covers:
 *  - Astral Manipulator: start drawing, finish and build
 *  - while drawing: clicking blocks to mark the corners (sneak-click to stop, as with TR's own)
 *  - Terraformer and Gravity Well
 *
 * No compile-time dependency: everything is looked up by ID, and none of it runs without TARDIS
 * Refined. Turn it off with /gamerule sonicsWorkWithTardisRefined false.
 *
 * Not covered: the Astral Manipulator's outline preview while you hold the sonic. TARDIS
 * Refined only draws that for its own screwdriver.
 */
public final class TardisRefinedSonics {
    private TardisRefinedSonics() {}

    private static final String MOD_ID = "tardis_refined";
    private static final Identifier SCREWDRIVER_ID = new Identifier(MOD_ID, "amethyst_screwdriver");
    /** TARDIS Refined blocks that react to its screwdriver. */
    private static final Set<String> SCREWDRIVER_BLOCKS = Set.of("astral_manipulator", "terraformer", "gravity_well");
    /** The item data TARDIS Refined's screwdriver keeps. */
    private static final String[] SCREWDRIVER_KEYS = {
            "screwdriver_mode", "linked_manipulator_pos", "screwdriver_point_a", "screwdriver_point_b",
            "screwdriver_b_was_last_updated_pos"
    };

    public static GameRules.Key<GameRules.BooleanRule> ENABLED;

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        ENABLED = GameRuleRegistry.register("sonicsWorkWithTardisRefined", GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(true));
        UseBlockCallback.EVENT.register(TardisRefinedSonics::onUseBlock);
        GallifreyMod.LOGGER.info("[Gallifrey] TARDIS Refined found: sonics work as TARDIS Refined screwdrivers");
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (hand != Hand.MAIN_HAND || player.isSpectator()) return ActionResult.PASS;
        ItemStack sonic = player.getStackInHand(hand);
        if (!(sonic.getItem() instanceof SonicScrewdriver)) return ActionResult.PASS;

        Item screwdriver = Registries.ITEM.get(SCREWDRIVER_ID);
        if (screwdriver == Items.AIR) return ActionResult.PASS;

        BlockState state = world.getBlockState(hit.getBlockPos());
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        boolean trDevice = MOD_ID.equals(blockId.getNamespace()) && SCREWDRIVER_BLOCKS.contains(blockId.getPath());
        boolean drawing = sonic.hasNbt() && "DRAWING".equals(sonic.getNbt().getString("screwdriver_mode"));
        if (!trDevice && !drawing) return ActionResult.PASS; // a normal sonic click

        // The game rule lives on the server; the client just swings the arm and lets the server decide.
        if (world.isClient) return ActionResult.SUCCESS;
        if (ENABLED != null && !world.getGameRules().getBoolean(ENABLED)) return ActionResult.PASS;

        ItemStack standIn = new ItemStack(screwdriver);
        if (sonic.hasNbt()) standIn.setNbt(sonic.getNbt().copy());
        player.setStackInHand(hand, standIn);
        ActionResult result;
        try {
            result = trDevice
                    ? state.onUse(world, player, hand, hit)
                    : standIn.useOnBlock(new ItemUsageContext(player, hand, hit));
        } finally {
            ItemStack after = player.getStackInHand(hand).isOf(screwdriver) ? player.getStackInHand(hand) : standIn;
            copyScrewdriverData(after, sonic);
            player.setStackInHand(hand, sonic);
        }
        return result.isAccepted() ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    /** Copy TARDIS Refined's screwdriver data from one stack to the other, leaving everything else alone. */
    private static void copyScrewdriverData(ItemStack from, ItemStack to) {
        NbtCompound source = from.getNbt();
        NbtCompound target = to.getOrCreateNbt();
        for (String key : SCREWDRIVER_KEYS) {
            if (source != null && source.contains(key)) target.put(key, source.get(key).copy());
            else target.remove(key);
        }
    }
}
