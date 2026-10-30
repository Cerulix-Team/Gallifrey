package com.cerulixteam.gallifrey.compat.angels;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;

import java.util.Set;

/**
 * Weeping Angels (Jeryn99's "Doctor Who: Weeping Angels", Fabric 1.20.1) support.
 *
 *  - Spawning: Weeping Angels only spawns its angels in biomes tagged as Overworld or Nether, so
 *    none of Gallifrey's biomes ever got them. This adds them to Gallifrey, Skaro, Classic and
 *    Prehistoric biomes as normal night-time monsters. Never in Lost Reality, on Mars, the Moon
 *    or Mondas, or in the TARDIS.
 *  - Teleporting: an angel's touch sends you to a random dimension. It will no longer pick the
 *    TARDIS interior, Lost Reality, or somewhere airless (Mars, the Moon, Mondas, the Ad Astra
 *    orbits); you're sent somewhere in your current dimension instead.
 *    (mixin/compat/WeepingAngelsTeleporterMixin)
 *  - The TARDIS exterior is in #weeping_angels:no_breaking, so angels don't smash its lamp.
 *
 * No compile-time dependency; nothing here runs unless Weeping Angels is installed.
 * Checked against Weeping Angels 46.1.6 for 1.20.1.
 */
public final class WeepingAngelsCompat {
    private WeepingAngelsCompat() {}

    public static final String MOD_ID = "weeping_angels";
    private static final Identifier ANGEL = new Identifier(MOD_ID, "weeping_angel");

    /** Gallifrey dimensions an angel must never send anyone to. */
    private static final Set<String> NO_TELEPORT = Set.of(
            "tardis", "lost_reality", "mars", "moon", "mondas",
            "gallifrey_orbit", "skaro_orbit", "mondas_orbit");

    /** Gallifrey biomes angels don't spawn in. */
    private static final Set<String> NO_SPAWN_BIOMES = Set.of("mars", "moon", "mondas");

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;

        BiomeModifications.create(GallifreyMod.id("weeping_angel_spawns"))
                .add(ModificationPhase.ADDITIONS,
                        context -> {
                            Identifier biome = context.getBiomeKey().getValue();
                            return biome.getNamespace().equals(GallifreyMod.MOD_ID)
                                    && !biome.getPath().startsWith("lost_")
                                    && !NO_SPAWN_BIOMES.contains(biome.getPath());
                        },
                        (selection, modification) -> {
                            // Looked up here, not at startup: by now every mod's entities are registered.
                            Registries.ENTITY_TYPE.getOrEmpty(ANGEL).ifPresent(angel ->
                                    modification.getSpawnSettings().addSpawn(SpawnGroup.MONSTER,
                                            new SpawnSettings.SpawnEntry(angel, 5, 1, 2)));
                        });

        GallifreyMod.LOGGER.info("[Gallifrey] Weeping Angels found: angels now haunt Gallifrey, and keep out of the TARDIS");
    }

    /** True for dimensions an angel's teleport must not choose. */
    public static boolean isOffLimits(RegistryKey<World> dimension) {
        Identifier id = dimension.getValue();
        return id.getNamespace().equals(GallifreyMod.MOD_ID) && NO_TELEPORT.contains(id.getPath());
    }
}
