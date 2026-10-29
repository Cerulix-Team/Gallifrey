package com.timelordmod.gallifrey.networking.packets;

import com.timelordmod.gallifrey.world.SpaceSuitJetpackHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class SpaceSuitJetpackPacket {
    private SpaceSuitJetpackPacket() {}

    public static void receive(MinecraftServer server, PlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf,
                               PacketSender responseSender) {
        server.execute(() -> SpaceSuitJetpackHandler.activate(player));
    }
}
