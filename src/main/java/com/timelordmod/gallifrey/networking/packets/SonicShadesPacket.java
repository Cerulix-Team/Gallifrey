package com.timelordmod.gallifrey.networking.packets;

import com.timelordmod.gallifrey.item.GallifreyModItems;
import com.timelordmod.gallifrey.item.custom.SonicScrewdriver;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import com.timelordmod.gallifrey.item.custom.SonicShadesSlotAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public final class SonicShadesPacket {
    private SonicShadesPacket() {}

    public static void receive(
            MinecraftServer server,
            PlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender) {

        boolean toggleMode = buf.readBoolean();

        server.execute(() -> {
            var stack = com.timelordmod.gallifrey.item.custom.GallifreyAccessories.getShades(player);
            if (!stack.isOf(GallifreyModItems.SONIC_SHADES)) {
                return;
            }

            if (toggleMode) {
                SonicScrewdriver.toggleMode(stack);
                player.sendMessage(
                        Text.literal("§bSONIC SHADES MODE: §e" + SonicScrewdriver.getMode(stack).getDisplayName()),
                        true
                );
            } else {
                SonicScrewdriver.activate(player, player.getWorld(), stack);
            }
            // The stack was changed in place: store it again so every client sees the change.
            com.timelordmod.gallifrey.item.custom.GallifreyAccessories.resyncShades(player);
        });
    }
}
