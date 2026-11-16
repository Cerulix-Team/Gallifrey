package com.cerulixteam.gallifrey.networking;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.sonic.SonicCasing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class SonicCasingClientNetworking {
    public static final Identifier CHANGE_CASING = new Identifier(GallifreyMod.MOD_ID, "change_sonic_workshop_casing");

    private SonicCasingClientNetworking() {}

    public static void sendCasingChange(BlockPos pos, SonicCasing casing) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeString(casing.getId());
        ClientPlayNetworking.send(CHANGE_CASING, buf);
    }
}
