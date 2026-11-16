package com.cerulixteam.gallifrey.compat.heracles;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Heracles quest support: a "Gallifrey" questline covering steel, sonic crystal, atrium, the
 * sonic screwdriver, the Vortex Manipulator and every Gallifrey dimension.
 *
 * Heracles only reads quests from config/heracles/quests, never from mods, so the quest files
 * ship inside the jar (heracles_quests/) and are copied there once, the first time the game starts
 * with Heracles installed. After that they belong to the player or pack maker: edits are kept, and
 * deleting them doesn't bring them back (config/gallifrey/heracles_quests_installed records the copy;
 * delete that file to reinstall). Existing quest files with the same name are never overwritten.
 *
 * Runs at mod init, before any world loads, so Heracles finds the files on its first load.
 */
public final class HeraclesQuests {
    private HeraclesQuests() {}

    public static void install() {
        if (!FabricLoader.getInstance().isModLoaded("heracles")) return;

        Path config = FabricLoader.getInstance().getConfigDir();
        Path marker = config.resolve(GallifreyMod.MOD_ID).resolve("heracles_quests_installed");
        if (Files.exists(marker)) return;

        Path target = config.resolve("heracles").resolve("quests").resolve("gallifrey");
        FabricLoader.getInstance().getModContainer(GallifreyMod.MOD_ID)
                .flatMap(mod -> mod.findPath("heracles_quests"))
                .ifPresent(source -> {
                    try (Stream<Path> files = Files.list(source)) {
                        Files.createDirectories(target);
                        int copied = 0;
                        for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                            Path dest = target.resolve(file.getFileName().toString());
                            if (Files.exists(dest)) continue;
                            Files.copy(file, dest);
                            copied++;
                        }
                        Files.createDirectories(marker.getParent());
                        Files.write(marker, List.of(
                                "Gallifrey copied its Heracles quests into config/heracles/quests/gallifrey.",
                                "Delete this file to copy them again (existing quest files are never overwritten)."));
                        GallifreyMod.LOGGER.info("[Gallifrey] Heracles found: installed {} Gallifrey quests", copied);
                    } catch (IOException e) {
                        GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't install the Gallifrey quests for Heracles", e);
                    }
                });
    }
}
