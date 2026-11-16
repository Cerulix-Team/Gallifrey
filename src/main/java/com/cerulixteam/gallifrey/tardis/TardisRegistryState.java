package com.cerulixteam.gallifrey.tardis;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent registry for Gallifrey TARDISes.
 *
 * The physical exterior stores its authoritative configuration; this registry
 * exists so players can reconnect while inside the pocket dimension and the
 * server can find the corresponding exterior even when its chunk is unloaded.
 */
public final class TardisRegistryState extends PersistentState {
    private static final String STATE_ID = "gallifrey_tardises";
    private final Map<UUID, Record> tardises = new HashMap<>();
    private final Map<UUID, UUID> activeTardises = new HashMap<>();
    private int nextInteriorSlot = 0;

    public static TardisRegistryState get(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        return overworld.getPersistentStateManager().getOrCreate(
                TardisRegistryState::fromNbt,
                TardisRegistryState::new,
                STATE_ID
        );
    }

    public static TardisRegistryState fromNbt(NbtCompound nbt) {
        TardisRegistryState state = new TardisRegistryState();
        state.nextInteriorSlot = nbt.getInt("NextInteriorSlot");
        NbtList list = nbt.getList("Tardises", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound t = list.getCompound(i);
            try {
                UUID id = t.getUuid("Id");
                String interiorDimension = t.contains("InteriorDimension")
                        ? t.getString("InteriorDimension")
                        : TardisDimensionManager.INTERIOR_DIMENSION_ID.toString();
                Record record = new Record(
                        id,
                        t.containsUuid("Owner") ? t.getUuid("Owner") : null,
                        t.getString("World"),
                        t.getLong("Pos"),
                        t.getLong("Origin"),
                        t.getBoolean("Locked"),
                        interiorDimension
                );
                state.tardises.put(id, record);
            } catch (IllegalArgumentException ignored) {
            }
        }
        NbtList players = nbt.getList("ActivePlayers", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < players.size(); i++) {
            NbtCompound p = players.getCompound(i);
            if (p.containsUuid("Player") && p.containsUuid("Tardis")) {
                state.activeTardises.put(p.getUuid("Player"), p.getUuid("Tardis"));
            }
        }
        return state;
    }

    public Record get(UUID id) {
        return tardises.get(id);
    }

    public java.util.Collection<Record> records() {
        return java.util.List.copyOf(tardises.values());
    }

    public UUID getActiveTardis(UUID player) {
        return activeTardises.get(player);
    }

    public int allocateInteriorSlot() {
        return nextInteriorSlot++;
    }

    public void register(UUID id, UUID owner, ServerWorld world, long pos, long origin, boolean locked, String interiorDimension) {
        tardises.put(id, new Record(id, owner, world.getRegistryKey().getValue().toString(), pos, origin, locked, interiorDimension));
        markDirty();
    }

    public void updateLocation(UUID id, ServerWorld world, long pos) {
        Record old = tardises.get(id);
        if (old != null) {
            tardises.put(id, new Record(id, old.owner(), world.getRegistryKey().getValue().toString(), pos, old.origin(), old.locked(), old.interiorDimension()));
            markDirty();
        }
    }

    public void updateLock(UUID id, boolean locked) {
        Record old = tardises.get(id);
        if (old != null) {
            tardises.put(id, new Record(id, old.owner(), old.world(), old.pos(), old.origin(), locked, old.interiorDimension()));
            markDirty();
        }
    }

    /** Removes a TARDIS permanently from the registry and clears all player links to it. */
    public boolean remove(UUID id) {
        boolean removed = tardises.remove(id) != null;
        activeTardises.entrySet().removeIf(entry -> id.equals(entry.getValue()));
        if (removed) markDirty();
        return removed;
    }

    public void setActive(ServerPlayerEntity player, UUID tardis) {
        activeTardises.put(player.getUuid(), tardis);
        markDirty();
    }

    public void clearActive(ServerPlayerEntity player) {
        activeTardises.remove(player.getUuid());
        markDirty();
    }

    public boolean isInside(ServerPlayerEntity player, UUID tardisId) {
        UUID active = activeTardises.get(player.getUuid());
        return tardisId.equals(active);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("NextInteriorSlot", nextInteriorSlot);
        NbtList list = new NbtList();
        for (Record r : tardises.values()) {
            NbtCompound t = new NbtCompound();
            t.putUuid("Id", r.id());
            if (r.owner() != null) t.putUuid("Owner", r.owner());
            t.putString("World", r.world());
            t.putLong("Pos", r.pos());
            t.putLong("Origin", r.origin());
            t.putBoolean("Locked", r.locked());
            t.putString("InteriorDimension", r.interiorDimension());
            list.add(t);
        }
        nbt.put("Tardises", list);

        NbtList players = new NbtList();
        for (Map.Entry<UUID, UUID> e : activeTardises.entrySet()) {
            NbtCompound p = new NbtCompound();
            p.putUuid("Player", e.getKey());
            p.putUuid("Tardis", e.getValue());
            players.add(p);
        }
        nbt.put("ActivePlayers", players);
        return nbt;
    }

    public void updateInteriorDimension(UUID id, String interiorDimension, long origin) {
        Record old = tardises.get(id);
        if (old != null) {
            tardises.put(id, new Record(id, old.owner(), old.world(), old.pos(), origin, old.locked(), interiorDimension));
            markDirty();
        }
    }

    public record Record(UUID id, UUID owner, String world, long pos, long origin, boolean locked, String interiorDimension) {}
}
