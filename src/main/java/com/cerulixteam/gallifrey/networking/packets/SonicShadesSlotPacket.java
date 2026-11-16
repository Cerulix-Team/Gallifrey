package com.cerulixteam.gallifrey.networking.packets;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import com.cerulixteam.gallifrey.item.custom.SonicShadesSlotAccess;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class SonicShadesSlotPacket {

    private SonicShadesSlotPacket() {}

    public static void receive(
            MinecraftServer server,
            PlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        server.execute(() -> {
            if (!player.isCreative()) {
                return;
            }

            SonicShadesSlotAccess access =
                    (SonicShadesSlotAccess) player;

            ItemStack equipped = com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.getShades(player);
            ItemStack cursor =
                    player.currentScreenHandler.getCursorStack();

            // Remove the equipped shades onto an empty cursor.
            if (cursor.isEmpty()
                    && equipped.isOf(GallifreyModItems.SONIC_SHADES)) {

                player.currentScreenHandler.setCursorStack(equipped.copy());
                com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.setShades(player, ItemStack.EMPTY);
            }
            // Equip shades from the cursor into an empty slot.
            else if (equipped.isEmpty()
                    && cursor.isOf(GallifreyModItems.SONIC_SHADES)) {

                com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.setShades(player, cursor.copy());
                player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }

            player.currentScreenHandler.sendContentUpdates();
        });
    }
}
