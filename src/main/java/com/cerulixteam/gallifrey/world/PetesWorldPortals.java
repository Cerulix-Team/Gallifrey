package com.cerulixteam.gallifrey.world;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

/**
 * Portals that lead back where you came from. Nether portals can be lit in Pete's World
 * (ClassicPortalLightingMixin) and lead back there from the Nether (NetherPortalReturnMixin), and
 * White Point Star portals lead back to whichever dimension you built them in
 * (CustomPortalReturnMixin). This carries the remembered dimensions over when a player respawns,
 * so dying on the other side doesn't make the portal home lead to the Overworld.
 */
public final class PetesWorldPortals {
    private PetesWorldPortals() {}

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            NetherOriginHolder from = (NetherOriginHolder) oldPlayer;
            NetherOriginHolder to = (NetherOriginHolder) newPlayer;
            to.gallifrey$setNetherOrigin(from.gallifrey$getNetherOrigin());
            to.gallifrey$getPortalOrigins().putAll(from.gallifrey$getPortalOrigins());
        });
    }
}
