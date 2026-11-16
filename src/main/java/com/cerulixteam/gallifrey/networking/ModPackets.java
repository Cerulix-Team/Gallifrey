package com.cerulixteam.gallifrey.networking;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.networking.packets.VMPacket;
import com.cerulixteam.gallifrey.networking.packets.SonicShadesPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import com.cerulixteam.gallifrey.networking.packets.SonicShadesSlotPacket;

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
    public static final Identifier TARDIS_MONITOR_ACTION = new Identifier(GallifreyMod.MOD_ID, "tardis_monitor_action");
    public static final Identifier TARDIS_MONITOR_STATE = new Identifier(GallifreyMod.MOD_ID, "tardis_monitor_state");
    public static final Identifier TARDIS_MONITOR_REFRESH = new Identifier(GallifreyMod.MOD_ID, "tardis_monitor_refresh");
    /** Server -> client: shake the camera inside a TARDIS (float intensity, varint ticks). */
    public static final Identifier TARDIS_SHAKE = new Identifier(GallifreyMod.MOD_ID, "tardis_shake");
    public static final Identifier TARDIS_POWER = new Identifier(GallifreyMod.MOD_ID, "tardis_power");
    public static final Identifier TARDIS_CLOSE_CONSOLE = new Identifier(GallifreyMod.MOD_ID, "tardis_close_console");
    public static final Identifier TARDIS_REGISTER_DIMENSION = new Identifier(GallifreyMod.MOD_ID, "tardis_register_dimension");
    public static final Identifier TARDIS_RWF_INPUT = new Identifier(GallifreyMod.MOD_ID, "tardis_rwf_input");
    public static final Identifier TARDIS_RWF_STATE = new Identifier(GallifreyMod.MOD_ID, "tardis_rwf_state");
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
