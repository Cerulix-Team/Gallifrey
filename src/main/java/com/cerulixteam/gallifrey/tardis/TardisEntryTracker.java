package com.cerulixteam.gallifrey.tardis;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers which TARDIS exterior each player last walked in through, so things that need to
 * link the interior to the outside (voice chat relaying) know where "outside" is.
 * Server-side and in memory only; it's rebuilt as people use their TARDISes.
 */
public final class TardisEntryTracker {
    private TardisEntryTracker() {}

    public static final RegistryKey<World> INTERIOR =
            RegistryKey.of(RegistryKeys.WORLD, new Identifier("gallifrey", "tardis"));

    /** Where the interior's door/spawn point is. */
    public static final double DOOR_X = 0.5, DOOR_Y = 65.0, DOOR_Z = 0.5;

    public record Exterior(RegistryKey<World> world, BlockPos pos) {}

    private static final Map<UUID, Exterior> ENTRIES = new ConcurrentHashMap<>();

    public static void entered(UUID player, RegistryKey<World> world, BlockPos exterior) {
        ENTRIES.put(player, new Exterior(world, exterior.toImmutable()));
    }

    public static Exterior get(UUID player) {
        return ENTRIES.get(player);
    }

    public static Collection<Exterior> all() {
        return ENTRIES.values();
    }

    public static void forget(UUID player) {
        ENTRIES.remove(player);
    }
}
