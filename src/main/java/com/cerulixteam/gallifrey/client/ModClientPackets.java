package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.networking.ModPackets;
import com.cerulixteam.gallifrey.screens.SonicWorkshopScreen;
import com.cerulixteam.gallifrey.screens.VortexManipulatorScreen;
import com.cerulixteam.gallifrey.screens.VortexManipulatorSubScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

/** Server-to-client packet receivers. Client only: call from GallifreyModClient. */
public final class ModClientPackets {
    private ModClientPackets() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OPEN_SONIC_WORKSHOP, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> client.setScreen(new SonicWorkshopScreen(pos)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.VM_STATE, (client, handler, buf, responseSender) -> {
            PacketByteBuf copy = new PacketByteBuf(buf.copy());
            client.execute(() -> {
                if (client.currentScreen instanceof VortexManipulatorSubScreen) {
                    VortexManipulatorSubScreen.applyServerState(copy);
                } else {
                    VortexManipulatorScreen.applyServerState(copy);
                }
            });
        });
    }
}
