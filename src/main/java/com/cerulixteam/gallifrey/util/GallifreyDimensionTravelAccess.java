package com.cerulixteam.gallifrey.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lost Reality can only be entered three ways:
 *  - the Lost Dirt portal (CustomPortalReturnMixin authorizes it)
 *  - the Vortex Manipulator's 1-in-40 dimensional drift (VMPacket authorizes it)
 *  - the /tp and /execute in ... run tp commands, for operators (TeleportCommandTravelMixin)
 *
 * Every other move into it is refused: other mods' TARDISes, portal guns, teleporters, and the
 * vanilla teleport methods they call. That covers players (GallifreyDimensionTravelMixin) and every
 * other entity (EntityDimensionTravelMixin). Moving around inside Lost Reality, and leaving it, are
 * never blocked.
 *
 * Authorization is a short window around one allowed move, counted per entity, on the server thread.
 */
public final class GallifreyDimensionTravelAccess {
    public static final Identifier LOST_REALITY = new Identifier("gallifrey", "lost_reality");

    private static final ThreadLocal<Map<UUID, Integer>> AUTHORIZED =
            ThreadLocal.withInitial(HashMap::new);
    /** Last refusal message per player, so a portal retrying every tick doesn't spam chat. */
    private static final Map<UUID, Long> LAST_MESSAGE = new HashMap<>();

    private GallifreyDimensionTravelAccess() {}

    public static boolean isAuthorized(Entity entity) {
        return AUTHORIZED.get().containsKey(entity.getUuid());
    }

    public static void beginAuthorization(Entity entity) {
        AUTHORIZED.get().merge(entity.getUuid(), 1, Integer::sum);
    }

    public static void endAuthorization(Entity entity) {
        Map<UUID, Integer> authorized = AUTHORIZED.get();
        UUID id = entity.getUuid();
        int depth = authorized.getOrDefault(id, 0);
        if (depth <= 1) authorized.remove(id);
        else authorized.put(id, depth - 1);
        if (authorized.isEmpty()) AUTHORIZED.remove();
    }

    public static void runAuthorized(Entity entity, Runnable action) {
        beginAuthorization(entity);
        try {
            action.run();
        } finally {
            endAuthorization(entity);
        }
    }

    /**
     * True if this move should be refused: it goes into Lost Reality from another dimension and
     * wasn't authorized. Tells a refused player why (at most once every few seconds).
     */
    public static boolean shouldBlock(Entity entity, ServerWorld destination) {
        if (destination == null || !destination.getRegistryKey().getValue().equals(LOST_REALITY)) return false;
        if (entity.getWorld().getRegistryKey().equals(destination.getRegistryKey())) return false; // already there
        if (isAuthorized(entity)) return false;

        if (entity instanceof ServerPlayerEntity player) {
            long now = player.getServer() != null ? player.getServer().getTicks() : 0;
            Long last = LAST_MESSAGE.get(player.getUuid());
            if (last == null || now - last > 100 || now < last) {
                LAST_MESSAGE.put(player.getUuid(), now);
                player.sendMessage(Text.literal("§5Unknown Data ERROR Entry Failed."), true);
            }
        }
        return true;
    }
}
