package com.cerulixteam.gallifrey.networking.packets;

import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.sonic.SonicCasing;
import com.cerulixteam.gallifrey.sonic.SonicCasingUnlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Sonic Workshop: the player picked a casing.
 *
 * Unlocked casings (and everything in creative) are applied straight away. A locked one is
 * unlocked first, for SonicCasingUnlocks.COST_XP experience points, if the player has enough;
 * otherwise nothing changes and they're told how much they need.
 */
public final class SonicCasingPacket {
    private SonicCasingPacket() {}

    public static void receive(
            net.minecraft.server.MinecraftServer server,
            PlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            net.fabricmc.fabric.api.networking.v1.PacketSender responseSender) {

        BlockPos pos = buf.readBlockPos();
        String casingId = buf.readString(64);

        server.execute(() -> {
            if (!(player.getWorld() instanceof ServerWorld world)) {
                return;
            }

            if (player.squaredDistanceTo(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            if (!(world.getBlockEntity(pos) instanceof SonicWorkshopBlockEntity workshop) || !workshop.hasSonic()) {
                return;
            }

            SonicCasing casing = SonicCasing.fromId(casingId);

            if (!SonicCasingUnlocks.isFreeFor(player, workshop.getSonic(), casing)) {
                int have = SonicCasingUnlocks.points(player);
                if (have < SonicCasingUnlocks.COST_XP) {
                    player.sendMessage(Text.literal("§cNeed " + SonicCasingUnlocks.COST_XP + " XP to unlock "
                            + casing.getDisplayName() + " §7(you have " + have + ")"), true);
                    return;
                }
                player.addExperience(-SonicCasingUnlocks.COST_XP);
                SonicCasingUnlocks.unlock(workshop.getSonic(), casing);
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6F, 1.6F);
                player.sendMessage(Text.literal("§6UNLOCKED: §f" + casing.getDisplayName()
                        + " §7(-" + SonicCasingUnlocks.COST_XP + " XP)"), true);
            } else {
                // Keep an older sonic's current casing unlocked once it's switched away from.
                SonicCasingUnlocks.save(workshop.getSonic());
            }

            workshop.setCasing(casing);
        });
    }
}
