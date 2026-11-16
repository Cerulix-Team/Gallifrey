package com.cerulixteam.gallifrey.compat.voicechat;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.tardis.TardisEntryTracker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Voice Chat plugin. Fabric only loads this through the "voicechat" entrypoint, so
 * nothing here is touched unless Simple Voice Chat is installed.
 *
 * - TARDIS doors carry sound: talking inside the TARDIS is heard around the exterior you came
 *   in through, and talking near an exterior someone used is heard just inside the doors.
 * - Vortex manipulator radio: hold a vortex manipulator and everyone else carrying one hears
 *   you, wherever they are, unless they're already close enough to hear you normally.
 */
public final class GallifreyVoicechatPlugin implements VoicechatPlugin {

    /** How close to a TARDIS exterior you need to be for the interior to hear you. */
    private static final double EXTERIOR_PICKUP = 8.0;
    /** Closer than this and the receiver hears you anyway, so the radio stays quiet. */
    private static final double RADIO_SKIP_RANGE = 32.0;

    private final Map<UUID, LocationalAudioChannel> locational = new ConcurrentHashMap<>();
    private final Map<UUID, StaticAudioChannel> radio = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return "gallifrey";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatConnection sender = event.getSenderConnection();
        if (sender == null) return;
        if (!(sender.getPlayer().getPlayer() instanceof ServerPlayerEntity player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        MicrophonePacket packet = event.getPacket();
        if (packet.isWhispering()) return;
        VoicechatServerApi api = event.getVoicechat();

        // Voice packets arrive on the voice thread; below only reads positions and inventories,
        // which is fine for a best-effort relay.
        try {
            tardisDoors(api, server, player, packet);
            vortexRadio(api, server, player, packet);
        } catch (RuntimeException ignored) {
            // A relay hiccup should never break anyone's voice chat.
        }
    }

    private void tardisDoors(VoicechatServerApi api, MinecraftServer server, ServerPlayerEntity player, MicrophonePacket packet) {
        RegistryKey<World> here = player.getWorld().getRegistryKey();
        if (TardisDimensionManager.isInteriorKey(here)) {
            // Inside: heard outside the exterior of the TARDIS this player is in.
            TardisEntryTracker.Entry entry = TardisEntryTracker.get(player.getUuid());
            if (entry == null || !entry.interiorWorld().equals(here)) return;
            ServerWorld world = server.getWorld(entry.exteriorWorld());
            if (world == null) return;
            BlockPos p = entry.exteriorPos();
            send(api, id("out-" + entry.exteriorWorld().getValue(), player.getUuid()), world,
                    p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, packet);
            return;
        }
        // Outside: if standing by an exterior someone went in through, heard just inside its doors.
        Vec3d pos = player.getPos();
        for (TardisEntryTracker.Entry entry : TardisEntryTracker.all()) {
            if (!entry.exteriorWorld().equals(here)) continue;
            if (pos.squaredDistanceTo(Vec3d.ofCenter(entry.exteriorPos())) > EXTERIOR_PICKUP * EXTERIOR_PICKUP) continue;
            ServerWorld interior = server.getWorld(entry.interiorWorld());
            if (interior == null || interior.getPlayers().isEmpty()) continue;
            Vec3d door = entry.interiorDoor();
            send(api, id("in-" + entry.interiorWorld().getValue(), player.getUuid()), interior,
                    door.x, door.y + 1.0, door.z, packet);
            return; // one relay per packet is plenty
        }
    }

    private void send(VoicechatServerApi api, UUID channelId, ServerWorld world, double x, double y, double z, MicrophonePacket packet) {
        LocationalAudioChannel channel = locational.get(channelId);
        if (channel == null || channel.isClosed()) {
            channel = api.createLocationalAudioChannel(channelId, api.fromServerLevel(world), api.createPosition(x, y, z));
            if (channel == null) return;
            locational.put(channelId, channel);
        } else {
            channel.updateLocation(api.createPosition(x, y, z));
        }
        channel.send(packet);
    }

    private void vortexRadio(VoicechatServerApi api, MinecraftServer server, ServerPlayerEntity player, MicrophonePacket packet) {
        if (!holdsManipulator(player)) return;
        for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
            if (other == player || !carriesManipulator(other)) continue;
            if (other.getWorld() == player.getWorld()
                    && other.squaredDistanceTo(player) < RADIO_SKIP_RANGE * RADIO_SKIP_RANGE) continue;
            VoicechatConnection to = api.getConnectionOf(other.getUuid());
            if (to == null || to.isDisabled()) continue;
            UUID channelId = id("radio-" + other.getUuid() + "-" + other.getWorld().getRegistryKey().getValue(), player.getUuid());
            StaticAudioChannel channel = radio.get(channelId);
            if (channel == null || channel.isClosed()) {
                channel = api.createStaticAudioChannel(channelId, api.fromServerLevel(other.getWorld()), to);
                if (channel == null) continue;
                radio.put(channelId, channel);
            }
            channel.send(packet);
        }
    }

    private static boolean holdsManipulator(ServerPlayerEntity p) {
        return p.getMainHandStack().isOf(GallifreyModItems.VORTEX_MANIPULATOR)
                || p.getOffHandStack().isOf(GallifreyModItems.VORTEX_MANIPULATOR);
    }

    private static boolean carriesManipulator(ServerPlayerEntity p) {
        var inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStack(i).isOf(GallifreyModItems.VORTEX_MANIPULATOR)) return true;
        }
        return false;
    }

    /** Stable channel ids, so each relay reuses its channel instead of making a new one per packet. */
    private static UUID id(String kind, UUID player) {
        return UUID.nameUUIDFromBytes(("gallifrey:" + kind + ":" + player).getBytes(StandardCharsets.UTF_8));
    }
}
