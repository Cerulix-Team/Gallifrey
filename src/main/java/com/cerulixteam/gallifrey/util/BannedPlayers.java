package com.cerulixteam.gallifrey.util;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.UUID;

/**
 * Players who aren't allowed to use Gallifrey. Any world or server running the mod disconnects
 * them as soon as they join (in singleplayer too, since that's an integrated server). Nothing
 * else happens to them or their files.
 */
public final class BannedPlayers {
    private BannedPlayers() {}

    private static final Set<UUID> BANNED = Set.of(
            UUID.fromString("c7496354-ec77-4610-9d02-43d511522481"),
            UUID.fromString("07e6b550-be92-4422-a269-345593df5a10"),
            UUID.fromString("ae93f403-bf4a-4fb9-89db-2826ae9a4508")
    );

    public static boolean isBanned(UUID player) {
        return BANNED.contains(player);
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (isBanned(handler.getPlayer().getUuid())) {
                GallifreyMod.LOGGER.info("[Gallifrey] {} isn't permitted to use Gallifrey; disconnecting",
                        handler.getPlayer().getGameProfile().getName());
                handler.disconnect(Text.literal("You are not permitted to use the Gallifrey mod."));
            }
        });
    }
}
