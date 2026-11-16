package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.tardis.TardisRemote;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stattenheim remote: calls your TARDIS to you.
 *
 * Sneak + right-click your TARDIS to link the remote to it, then right-click anywhere outside a TARDIS to
 * summon it: it dematerialises wherever it is and materialises just in front of you, doors facing you.
 */
public class StattenheimRemoteItem extends Item {
    private static final int COOLDOWN_TICKS = 100;

    public StattenheimRemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult linked = TardisRemote.tryLink(context, "item.gallifrey.stattenheim_remote");
        if (linked != ActionResult.PASS) return linked;
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking()) return TypedActionResult.pass(stack);
        if (world.isClient) return TypedActionResult.success(stack);
        if (TardisRemote.summon((ServerPlayerEntity) player, stack)) {
            player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        TardisRemote.appendLinkTooltip(stack, tooltip);
        tooltip.add(Text.translatable("tooltip.gallifrey.stattenheim_remote").formatted(Formatting.DARK_GRAY));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }
}
