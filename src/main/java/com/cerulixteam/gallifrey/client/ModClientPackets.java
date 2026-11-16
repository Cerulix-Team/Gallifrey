package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.networking.ModPackets;
import com.cerulixteam.gallifrey.screens.SonicWorkshopScreen;
import com.cerulixteam.gallifrey.screens.VortexManipulatorScreen;
import com.cerulixteam.gallifrey.screens.VortexManipulatorSubScreen;
import com.cerulixteam.gallifrey.screens.TardisMonitorScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

/** Server-to-client packet receivers. Client only: call from GallifreyModClient. */
public final class ModClientPackets {
    private static boolean rwfActive;
    public static boolean isRwfActive() { return rwfActive; }
    public static void setRwfActive(boolean active) { rwfActive = active; }
    private ModClientPackets() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OPEN_SONIC_WORKSHOP, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> client.setScreen(new SonicWorkshopScreen(pos)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_MONITOR_STATE, (client, handler, buf, responseSender) -> {
            PacketByteBuf copy = new PacketByteBuf(buf.copy());
            client.execute(() -> {
                // The physical TARDIS console sends its state as the opening
                // packet.  Create the screen here first; the old monitor UI
                // expected a screen to already be open, so the state packet
                // previously arrived and was simply discarded.
                if (!(client.currentScreen instanceof TardisMonitorScreen)) {
                    client.setScreen(new TardisMonitorScreen());
                }
                TardisMonitorScreen.applyServerState(copy);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_MONITOR_REFRESH, (client, handler, buf, responseSender) -> {
            PacketByteBuf copy = new PacketByteBuf(buf.copy());
            client.execute(() -> {
                if (client.currentScreen instanceof TardisMonitorScreen) TardisMonitorScreen.applyServerState(copy);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_SHAKE, (client, handler, buf, responseSender) -> {
            float intensity = buf.readFloat();
            int ticks = buf.readVarInt();
            client.execute(() -> TardisClientEffects.shake(intensity, ticks));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_POWER, (client, handler, buf, responseSender) -> {
            boolean powered = buf.readBoolean();
            client.execute(() -> TardisClientEffects.setInteriorPowered(powered));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_REGISTER_DIMENSION, (client, handler, buf, responseSender) -> {
            RegistryKey<net.minecraft.world.World> key = RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier());
            client.execute(() -> client.getNetworkHandler().getWorldKeys().add(key));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_RWF_STATE, (client, handler, buf, responseSender) -> {
            boolean active = buf.readBoolean();
            client.execute(() -> rwfActive = active);
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPackets.TARDIS_CLOSE_CONSOLE, (client, handler, buf, responseSender) ->
                client.execute(() -> {
                    if (client.currentScreen instanceof TardisMonitorScreen) {
                        client.setScreen(null);
                    }
                }));

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
