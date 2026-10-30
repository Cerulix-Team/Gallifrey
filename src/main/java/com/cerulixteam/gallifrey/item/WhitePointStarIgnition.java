package com.cerulixteam.gallifrey.item;

import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Uses up one White Point Star when it lights a portal.
 *
 * Registered on each White Point Star portal with CustomPortalBuilder.registerIgniteEvent,
 * which Custom Portal API only calls on the server, and only after the portal has
 * actually been created. So a star is never used up on a frame that isn't valid.
 *
 * Creative-mode players keep their star.
 */
public final class WhitePointStarIgnition {
    private WhitePointStarIgnition() {}

    public static void consume(PlayerEntity player, World world, BlockPos portalPos, BlockPos framePos,
                               PortalIgnitionSource source) {
        if (player == null || world.isClient) return;
        if (source == null || source.sourceType != PortalIgnitionSource.SourceType.USEITEM) return;
        if (player.getAbilities().creativeMode) return;

        // Custom Portal API lights from whichever hand was used, so check the main hand first.
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(GallifreyModItems.WHITE_POINT_STAR)) {
                stack.decrement(1);
                return;
            }
        }
    }
}
