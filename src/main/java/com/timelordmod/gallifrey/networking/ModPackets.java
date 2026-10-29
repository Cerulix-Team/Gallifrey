package com.timelordmod.gallifrey.networking;

import com.timelordmod.gallifrey.GallifreyMod;
import com.timelordmod.gallifrey.networking.packets.VMPacket;
import com.timelordmod.gallifrey.networking.packets.SonicShadesPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import com.timelordmod.gallifrey.networking.packets.SonicShadesSlotPacket;

/**
 * Packet IDs and server-side receivers.
 *
 * This class is loaded on dedicated servers too (SonicWorkshopBlock uses
 * OPEN_SONIC_WORKSHOP), so it must never mention client-only classes such as
 * ClientPlayNetworking, MinecraftClient or screens. Client receivers live in
 * client/ModClientPackets.
 */
public class ModPackets {
    public static final Identifier VM_PACKET = new Identifier(GallifreyMod.MOD_ID, "vm_packet");
    public static final Identifier VM_STATE = new Identifier(GallifreyMod.MOD_ID, "vm_state");
    public static final Identifier OPEN_SONIC_WORKSHOP = new Identifier(GallifreyMod.MOD_ID, "open_sonic_workshop");
    public static final Identifier SONIC_SHADES_USE = new Identifier(GallifreyMod.MOD_ID, "sonic_shades_use");
    public static final Identifier SPACE_SUIT_JETPACK = new Identifier(GallifreyMod.MOD_ID, "space_suit_jetpack");
    public static final Identifier SONIC_SHADES_SLOT_SWAP =
            new Identifier(GallifreyMod.MOD_ID, "sonic_shades_slot_swap");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VM_PACKET, VMPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(SONIC_SHADES_USE, SonicShadesPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(
                SONIC_SHADES_SLOT_SWAP,
                SonicShadesSlotPacket::receive
        );
    }
}
