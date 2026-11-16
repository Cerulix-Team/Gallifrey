package com.cerulixteam.gallifrey.networking.packets;

import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.sonic.SonicCasing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;

public final class SonicWorkshopCasingPacket {
    private SonicWorkshopCasingPacket() {}

    public static void receive(MinecraftServer server, PlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf,
                               net.fabricmc.fabric.api.networking.v1.PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        SonicCasing casing = SonicCasing.fromId(buf.readString(64));
        server.execute(() -> {
            if (player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return;
            if (player.getWorld().getBlockEntity(pos) instanceof SonicWorkshopBlockEntity workshop) {
                workshop.setSonicCasing(casing);
                player.getWorld().updateListeners(pos, workshop.getCachedState(), workshop.getCachedState(), 3);
            }
        });
    }
}
