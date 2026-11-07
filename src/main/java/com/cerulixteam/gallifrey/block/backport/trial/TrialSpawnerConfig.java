package com.cerulixteam.gallifrey.block.backport.trial;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Trial Spawner's settings (1.21): what it spawns, how many, how fast, and what it gives back. Loaded from
 * data/&lt;namespace&gt;/trial_spawner/*.json, in vanilla 26.3's format (converted to Gallifrey ids).
 */
public record TrialSpawnerConfig(int spawnRange, float totalMobs, float simultaneousMobs, float totalMobsAddedPerPlayer,
                                 float simultaneousMobsAddedPerPlayer, int ticksBetweenSpawn, List<Weighted<Potential>> spawnPotentials,
                                 List<Weighted<Identifier>> lootTablesToEject, Identifier itemsToDropWhenOminous) {

    public record Weighted<T>(T value, int weight) {}

    /** One mob it can spawn: its entity data, and an optional loot table for its gear. */
    public record Potential(NbtCompound entity, @Nullable Identifier equipment, float dropChance) {}

    public static final Identifier DEFAULT_CONSUMABLES = new Identifier("gallifrey", "spawners/trial_chamber/consumables");
    public static final Identifier DEFAULT_KEY = new Identifier("gallifrey", "spawners/trial_chamber/key");
    public static final Identifier DEFAULT_OMINOUS_ITEMS = new Identifier("gallifrey", "spawners/trial_chamber/items_to_drop_when_ominous");

    public int totalFor(int players) {
        return (int) Math.floor(this.totalMobs + this.totalMobsAddedPerPlayer * Math.max(0, players - 1));
    }

    public int simultaneousFor(int players) {
        return (int) Math.floor(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * Math.max(0, players - 1));
    }

    @Nullable
    public static <T> T pick(List<Weighted<T>> list, Random random) {
        int total = 0;
        for (Weighted<T> w : list) total += w.weight();
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (Weighted<T> w : list) {
            roll -= w.weight();
            if (roll < 0) return w.value();
        }
        return null;
    }

    static TrialSpawnerConfig parse(JsonObject o) {
        List<Weighted<Potential>> potentials = new ArrayList<>();
        for (JsonElement e : array(o, "spawn_potentials")) {
            JsonObject entry = e.getAsJsonObject();
            JsonObject data = entry.getAsJsonObject("data");
            NbtCompound entity;
            try {
                entity = StringNbtReader.parse(data.getAsJsonObject("entity").toString());
            } catch (Exception ex) {
                continue;
            }
            Identifier equipment = null;
            float drop = 0.0F;
            if (data.has("equipment")) {
                JsonObject eq = data.getAsJsonObject("equipment");
                equipment = new Identifier(eq.get("loot_table").getAsString());
                if (eq.has("slot_drop_chances") && eq.get("slot_drop_chances").isJsonPrimitive()) drop = eq.get("slot_drop_chances").getAsFloat();
            }
            potentials.add(new Weighted<>(new Potential(entity, equipment, drop), entry.has("weight") ? entry.get("weight").getAsInt() : 1));
        }
        List<Weighted<Identifier>> eject = new ArrayList<>();
        for (JsonElement e : array(o, "loot_tables_to_eject")) {
            JsonObject entry = e.getAsJsonObject();
            eject.add(new Weighted<>(new Identifier(entry.get("data").getAsString()), entry.has("weight") ? entry.get("weight").getAsInt() : 1));
        }
        if (eject.isEmpty()) {
            eject.add(new Weighted<>(DEFAULT_CONSUMABLES, 1));
            eject.add(new Weighted<>(DEFAULT_KEY, 1));
        }
        return new TrialSpawnerConfig(i(o, "spawn_range", 4), f(o, "total_mobs", 6), f(o, "simultaneous_mobs", 2), f(o, "total_mobs_added_per_player", 2),
                f(o, "simultaneous_mobs_added_per_player", 1), i(o, "ticks_between_spawn", 40), potentials, eject,
                o.has("items_to_drop_when_ominous") ? new Identifier(o.get("items_to_drop_when_ominous").getAsString()) : DEFAULT_OMINOUS_ITEMS);
    }

    private static JsonArray array(JsonObject o, String key) {
        return o.has(key) ? o.getAsJsonArray(key) : new JsonArray();
    }

    private static int i(JsonObject o, String key, int fallback) {
        return o.has(key) ? o.get(key).getAsInt() : fallback;
    }

    private static float f(JsonObject o, String key, float fallback) {
        return o.has(key) ? o.get(key).getAsFloat() : fallback;
    }

    // =========================================================
    // LOADING
    // =========================================================

    private static final Map<Identifier, TrialSpawnerConfig> CONFIGS = new HashMap<>();

    @Nullable
    public static TrialSpawnerConfig get(@Nullable Identifier id) {
        return id == null ? null : CONFIGS.get(id);
    }

    public static final class Loader implements SimpleSynchronousResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return new Identifier("gallifrey", "trial_spawner");
        }

        @Override
        public void reload(ResourceManager manager) {
            CONFIGS.clear();
            for (Map.Entry<Identifier, Resource> e : manager.findResources("trial_spawner", p -> p.getPath().endsWith(".json")).entrySet()) {
                Identifier file = e.getKey();
                String path = file.getPath().substring("trial_spawner/".length(), file.getPath().length() - ".json".length());
                try (Reader reader = e.getValue().getReader()) {
                    CONFIGS.put(new Identifier(file.getNamespace(), path), parse(JsonParser.parseReader(reader).getAsJsonObject()));
                } catch (Exception ex) {
                    com.cerulixteam.gallifrey.GallifreyMod.LOGGER.warn("Couldn't read trial spawner config {}: {}", file, ex.toString());
                }
            }
        }
    }
}
