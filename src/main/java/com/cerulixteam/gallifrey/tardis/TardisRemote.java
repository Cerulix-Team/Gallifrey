package com.cerulixteam.gallifrey.tardis;

import com.cerulixteam.gallifrey.block.TardisExteriorBlock;
import com.cerulixteam.gallifrey.block.TardisExteriorTopBlock;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity.FlightPhase;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Linking items to a TARDIS, and what linked items do.
 *
 *  - Link: sneak + right-click your own TARDIS with a Stattenheim remote or a sonic screwdriver.
 *  - Stattenheim remote: right-click to summon the TARDIS. It takes off (releasing the handbrake), flies to
 *    just in front of you, facing you, and lands by itself, with the full dematerialise/materialise.
 *  - Sonic screwdriver, TARDIS mode: aimed at its TARDIS, locks or unlocks it (locking closes the doors);
 *    aimed anywhere else, says where the TARDIS is and how much fuel it has.
 */
public final class TardisRemote {
    public static final String LINK_KEY = "GallifreyTardis";

    private TardisRemote() {}

    @Nullable
    public static UUID linkedTardis(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.containsUuid(LINK_KEY) ? nbt.getUuid(LINK_KEY) : null;
    }

    /** The exterior at (or under) this position, if it's a TARDIS. */
    @Nullable
    public static TardisExteriorBlockEntity tardisAt(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() instanceof TardisExteriorTopBlock) pos = pos.down();
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof TardisExteriorBlockEntity tardis ? tardis : null;
    }

    /** Sneak + right-click on a TARDIS: link this item to it. Returns PASS if it isn't a TARDIS. */
    public static ActionResult tryLink(ItemUsageContext context, String itemName) {
        if (context.getPlayer() == null || !context.getPlayer().isSneaking()) return ActionResult.PASS;
        TardisExteriorBlockEntity tardis = tardisAt(context.getWorld(), context.getBlockPos());
        if (tardis == null) return ActionResult.PASS;
        if (context.getWorld().isClient) return ActionResult.SUCCESS;
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (tardis.getTardisId() == null || !tardis.canPilot(player.getUuid())) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.link_not_yours"), true);
            return ActionResult.CONSUME;
        }
        context.getStack().getOrCreateNbt().putUuid(LINK_KEY, tardis.getTardisId());
        context.getWorld().playSound(null, context.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
                SoundCategory.PLAYERS, 0.8F, 1.6F);
        player.sendMessage(Text.translatable("tardis.gallifrey.msg.linked", Text.translatable(itemName)), true);
        return ActionResult.CONSUME;
    }

    /** The linked TARDIS, wherever it is (its chunk is loaded if needed), or null. */
    @Nullable
    public static TardisExteriorBlockEntity find(MinecraftServer server, @Nullable UUID id) {
        if (id == null) return null;
        TardisRegistryState.Record record = TardisRegistryState.get(server).get(id);
        if (record == null) return null;
        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (world == null) return null;
        BlockPos pos = BlockPos.fromLong(record.pos());
        world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis && id.equals(tardis.getTardisId())
                ? tardis : null;
    }

    @Nullable
    private static TardisExteriorBlockEntity linkedOrSay(ServerPlayerEntity player, ItemStack stack) {
        UUID id = linkedTardis(stack);
        if (id == null) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.not_linked"), true);
            return null;
        }
        TardisExteriorBlockEntity tardis = find(player.getServer(), id);
        if (tardis == null) player.sendMessage(Text.translatable("tardis.gallifrey.msg.link_lost"), true);
        return tardis;
    }

    // ------------------------------------------------------------------ Stattenheim remote

    /** Calls the linked TARDIS to land just in front of the player, facing them. */
    public static boolean summon(ServerPlayerEntity player, ItemStack remote) {
        TardisExteriorBlockEntity tardis = linkedOrSay(player, remote);
        if (tardis == null) return false;
        if (!tardis.canPilot(player.getUuid())) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.not_pilot"), true);
            return false;
        }
        ServerWorld here = player.getServerWorld();
        if (TardisDimensionManager.isInteriorWorld(here)) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.summon_inside"), true);
            return false;
        }

        BlockPos spot = player.getBlockPos().offset(player.getHorizontalFacing(), 2);
        // The doors face the player: the box's front points back the way they're looking.
        int rotation = MathHelper.floor((player.getYaw() + 180.0F) * 8.0F / 360.0F + 0.5F) & 7;

        if (tardis.getPhase() == FlightPhase.REMAT) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.materialising"), true);
            return false;
        }
        tardis.setDestination(here.getRegistryKey().getValue(), spot);
        tardis.setDestRotation(rotation);
        tardis.setAutoLand(true);
        here.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.8F, 1.4F);

        if (tardis.getPhase() != FlightPhase.LANDED) {
            // Already in flight: it'll come here instead.
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.summon_rerouted"), true);
            return true;
        }
        tardis.setHandbrake(false);
        if (TardisFlight.takeOff(player, tardis)) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.summoned"), true);
            return true;
        }
        tardis.setAutoLand(false);
        return false;
    }

    // ------------------------------------------------------------------ sonic screwdriver

    /** TARDIS mode on a sonic linked to a Gallifrey TARDIS. Returns true if it did something. */
    public static boolean useSonic(ServerPlayerEntity player, ItemStack sonic, HitResult target) {
        TardisExteriorBlockEntity tardis = linkedOrSay(player, sonic);
        if (tardis == null) return false;

        if (target instanceof BlockHitResult hit && target.getType() == HitResult.Type.BLOCK) {
            TardisExteriorBlockEntity aimed = tardisAt(player.getWorld(), hit.getBlockPos());
            if (aimed == tardis) {
                if (tardis.isFlightPending()) {
                    player.sendMessage(Text.translatable("tardis.gallifrey.msg.doors_in_flight"), true);
                    return false;
                }
                tardis.setLocked(!tardis.isLocked());
                if (tardis.isLocked()) TardisFlight.setDoors(tardis, false, true);
                player.sendMessage(Text.translatable(tardis.isLocked() ? "tardis.gallifrey.msg.locked"
                        : "tardis.gallifrey.msg.unlocked"), true);
                return true;
            }
        }

        // Anywhere else: where is it, and how's it doing?
        World world = tardis.getWorld();
        BlockPos pos = tardis.getPos();
        String dimension = world == null ? "?" : world.getRegistryKey().getValue().toString();
        Text status = Text.translatable(tardis.isFlightPending() ? "tardis.gallifrey.msg.sonic_status_flying"
                : "tardis.gallifrey.msg.sonic_status_landed");
        player.sendMessage(Text.translatable("tardis.gallifrey.msg.sonic_status", status,
                pos.getX(), pos.getY(), pos.getZ(), dimension, tardis.getFuel(), TardisExteriorBlockEntity.MAX_FUEL), false);
        return true;
    }

    /** Tooltip line for a linkable item. */
    public static void appendLinkTooltip(ItemStack stack, List<Text> tooltip) {
        tooltip.add(Text.translatable(linkedTardis(stack) != null ? "tooltip.gallifrey.tardis_linked"
                : "tooltip.gallifrey.tardis_not_linked").formatted(Formatting.GRAY));
    }
}
