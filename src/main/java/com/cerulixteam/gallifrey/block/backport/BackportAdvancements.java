package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Grants the backported advancements that no vanilla 1.20.1 trigger can watch for (see
 * data/gallifrey/advancements/backported). Those advancements have a single "impossible" criterion called "done".
 */
public final class BackportAdvancements {
    private BackportAdvancements() {}

    public static void grant(ServerPlayerEntity player, String name) {
        if (player.getServer() == null) return;
        Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier("gallifrey", "backported/" + name));
        if (advancement != null) player.getAdvancementTracker().grantCriterion(advancement, "done");
    }
}
