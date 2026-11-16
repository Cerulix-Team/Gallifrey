package com.cerulixteam.gallifrey.tardis;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers, for each player, the TARDIS they last walked into: where its exterior is and which
 * interior dimension (and door position) it leads to. Used by things that link the inside of a
 * TARDIS to the outside, such as Simple Voice Chat relaying voices through the doors.
 * Server-side and in memory only; it's rebuilt as people use their TARDISes.
 */
public final class TardisEntryTracker {
    private TardisEntryTracker() {}

    public record Entry(RegistryKey<World> exteriorWorld, BlockPos exteriorPos,
                        RegistryKey<World> interiorWorld, Vec3d interiorDoor) {}

    private static final Map<UUID, Entry> ENTRIES = new ConcurrentHashMap<>();

    public static void entered(UUID player, RegistryKey<World> exteriorWorld, BlockPos exteriorPos,
                               RegistryKey<World> interiorWorld, Vec3d interiorDoor) {
        ENTRIES.put(player, new Entry(exteriorWorld, exteriorPos.toImmutable(), interiorWorld, interiorDoor));
    }

    public static Entry get(UUID player) {
        return ENTRIES.get(player);
    }

    public static Collection<Entry> all() {
        return ENTRIES.values();
    }

    public static void forget(UUID player) {
        ENTRIES.remove(player);
    }
}
