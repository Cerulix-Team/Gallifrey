package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Thirty-second space-suit jetpack with a 120-second recharge, followed by controlled gliding. */
public final class SpaceSuitJetpackHandler {
    private static final int FLIGHT_TICKS = 30 * 20;
    private static final int COOLDOWN_TICKS = 120 * 20;
    private static final Map<UUID, Integer> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> GLIDE_TICKS = new ConcurrentHashMap<>();

    private SpaceSuitJetpackHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SpaceSuitJetpackHandler::tickWorld);
    }

    public static void activate(PlayerEntity player) {
        UUID id = player.getUuid();
        if (!hasFullSuit(player)) {
            player.sendMessage(Text.literal("§cYou need the full Space Suit to use the jetpack."), true);
            return;
        }
        if (ACTIVE.containsKey(id)) return;
        int remaining = COOLDOWN.getOrDefault(id, 0);
        if (remaining > 0) {
            player.sendMessage(Text.literal("§eJetpack recharging: " + ((remaining + 19) / 20) + "s"), true);
            return;
        }
        ACTIVE.put(id, FLIGHT_TICKS);
        GLIDE_TICKS.remove(id);
        COOLDOWN.put(id, COOLDOWN_TICKS);
        player.sendMessage(Text.literal("§bJetpack activated!"), true);
    }

    private static void tickWorld(ServerWorld world) {
        for (PlayerEntity player : world.getPlayers()) {
            UUID id = player.getUuid();
            int cooldown = COOLDOWN.getOrDefault(id, 0);
            if (cooldown > 0) COOLDOWN.put(id, cooldown - 1);
            else COOLDOWN.remove(id);

            Integer active = ACTIVE.get(id);
            if (active == null) {
                Integer glide = GLIDE_TICKS.get(id);
                if (glide != null) {
                    if (!hasFullSuit(player) || player.isOnGround() || player.isSpectator()) {
                        GLIDE_TICKS.remove(id);
                    } else {
                        var v = player.getVelocity();
                        double yaw = Math.toRadians(player.getYaw());
                        player.setVelocity(v.x * 0.98 - Math.sin(yaw) * 0.025,
                                Math.max(v.y, -0.10), v.z * 0.98 + Math.cos(yaw) * 0.025);
                        player.velocityDirty = true;
                        // Gliding down is safe: don't let a minute of slow descent add up to a deadly fall.
                        player.fallDistance = 0.0F;
                        if (glide <= 1) GLIDE_TICKS.remove(id);
                        else GLIDE_TICKS.put(id, glide - 1);
                    }
                }
                continue;
            }
            if (!hasFullSuit(player) || player.isSpectator() || player.isCreative()) {
                ACTIVE.remove(id);
                continue;
            }

            // Sustain upward thrust while active; horizontal movement remains player-controlled.
            var velocity = player.getVelocity();
            player.setVelocity(velocity.x, Math.max(velocity.y, 0.42), velocity.z);
            player.velocityDirty = true;
            player.fallDistance = 0.0F;
            if (active <= 1) {
                ACTIVE.remove(id);
                GLIDE_TICKS.put(id, 20 * 60);
                player.sendMessage(Text.literal("§eJetpack depleted. Gliding enabled; recharge takes 120 seconds."), true);
            } else {
                ACTIVE.put(id, active - 1);
            }
        }
    }

    private static boolean hasFullSuit(PlayerEntity p) {
        return p.getEquippedStack(EquipmentSlot.HEAD).isOf(GallifreyModItems.SPACE_SUIT_HELMET)
                && p.getEquippedStack(EquipmentSlot.CHEST).isOf(GallifreyModItems.SPACE_SUIT_CHESTPLATE)
                && p.getEquippedStack(EquipmentSlot.LEGS).isOf(GallifreyModItems.SPACE_SUIT_LEGGINGS)
                && p.getEquippedStack(EquipmentSlot.FEET).isOf(GallifreyModItems.SPACE_SUIT_BOOTS);
    }
}
