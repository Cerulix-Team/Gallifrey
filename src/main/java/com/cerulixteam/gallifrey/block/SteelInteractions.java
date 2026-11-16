package com.cerulixteam.gallifrey.block;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public final class SteelInteractions {
    private static final Map<Block, Block> WAX = new HashMap<>();
    private static final Map<Block, Block> UNWAX = new HashMap<>();
    private static final Map<Block, Block> SCRAPE = new HashMap<>();

    private SteelInteractions() {}

    private static void waxPair(Block normal, Block waxed) {
        WAX.put(normal, waxed);
        UNWAX.put(waxed, normal);
    }

    public static void register() {
        waxPair(GallifreyModBlocks.STEEL_BLOCK, GallifreyModBlocks.WAXED_STEEL_BLOCK);
        waxPair(GallifreyModBlocks.TARNISHED_STEEL, GallifreyModBlocks.WAXED_TARNISHED_STEEL);
        waxPair(GallifreyModBlocks.WEATHERED_STEEL_BLOCK, GallifreyModBlocks.WAXED_WEATHERED_STEEL_BLOCK);
        waxPair(GallifreyModBlocks.CORRODED_STEEL_BLOCK, GallifreyModBlocks.WAXED_CORRODED_STEEL_BLOCK);
        waxPair(GallifreyModBlocks.RUSTED_STEEL_BLOCK, GallifreyModBlocks.WAXED_RUSTED_STEEL_BLOCK);

        SCRAPE.put(GallifreyModBlocks.TARNISHED_STEEL, GallifreyModBlocks.STEEL_BLOCK);
        SCRAPE.put(GallifreyModBlocks.WEATHERED_STEEL_BLOCK, GallifreyModBlocks.TARNISHED_STEEL);
        SCRAPE.put(GallifreyModBlocks.CORRODED_STEEL_BLOCK, GallifreyModBlocks.WEATHERED_STEEL_BLOCK);
        SCRAPE.put(GallifreyModBlocks.RUSTED_STEEL_BLOCK, GallifreyModBlocks.CORRODED_STEEL_BLOCK);

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block current = state.getBlock();
            ItemStack held = player.getStackInHand(hand);
            Block target;
            boolean waxing;
            boolean removingWax;

            if (held.isOf(Items.HONEYCOMB)) {
                target = WAX.get(current);
                waxing = true;
                removingWax = false;
            } else if (held.getItem() instanceof AxeItem) {
                target = UNWAX.get(current);
                removingWax = target != null;
                if (target == null) target = SCRAPE.get(current);
                waxing = false;
            } else {
                return ActionResult.PASS;
            }

            if (target == null) return ActionResult.PASS;
            if (!world.isClient) {
                world.setBlockState(pos, target.getDefaultState(), Block.NOTIFY_ALL);
                world.playSound(null, pos,
                        waxing ? SoundEvents.ITEM_HONEYCOMB_WAX_ON
                                : removingWax ? SoundEvents.ITEM_AXE_WAX_OFF
                                : SoundEvents.ITEM_AXE_SCRAPE,
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().creativeMode) {
                    if (waxing) held.decrement(1);
                    else held.damage(1, player, p -> p.sendToolBreakStatus(hand));
                }
            }
            return ActionResult.SUCCESS;
        });
    }
}
