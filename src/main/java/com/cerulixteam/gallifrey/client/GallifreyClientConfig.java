package com.cerulixteam.gallifrey.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Tiny client-side settings file: config/gallifrey-client.properties.
 * Only holds things the player chooses on their own machine.
 */
public final class GallifreyClientConfig {
    private static final String FILE_NAME = "gallifrey-client.properties";
    private static final String KEY_MENU_BACKGROUND = "menuBackground";
    /** Older versions saved only true/false (vortex or panorama); still read so the choice carries over. */
    private static final String KEY_OLD_VORTEX_MENU = "vortexMenuBackground";

    private static boolean loaded = false;
    /**
     * The chosen background: a built-in one by name (VORTEX, TIME_WAR...), or a custom one from a
     * resource pack or addon by "namespace:name" (see CustomMenuBackgrounds). A custom one that's
     * no longer installed shows the default (VORTEX) but stays saved, so it comes back with its pack.
     */
    private static String selected = MenuBackground.VORTEX.name();

    private GallifreyClientConfig() {}

    private static Path file() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static void load() {
        loaded = true;
        Path path = file();
        if (!Files.exists(path)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            String saved = properties.getProperty(KEY_MENU_BACKGROUND);
            if (saved != null) {
                saved = saved.trim();
                selected = saved.contains(":") ? saved : MenuBackground.byName(saved, MenuBackground.VORTEX).name();
            } else if (properties.containsKey(KEY_OLD_VORTEX_MENU)) {
                selected = (Boolean.parseBoolean(properties.getProperty(KEY_OLD_VORTEX_MENU))
                        ? MenuBackground.VORTEX : MenuBackground.PANORAMA).name();
            }
        } catch (IOException | RuntimeException ignored) {
            // A broken settings file must never stop the game from starting; keep the default.
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(KEY_MENU_BACKGROUND, selected);
        try {
            Path path = file();
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Gallifrey Mod client settings");
            }
        } catch (IOException | RuntimeException ignored) {
            // Not being able to save only means the choice is forgotten next launch.
        }
    }

    private static String selectedId() {
        if (!loaded) {
            load();
        }
        return selected;
    }

    /** The custom background in use, or null if a built-in one is (or the chosen custom one isn't installed). */
    @Nullable
    public static CustomMenuBackgrounds.Entry getCustomMenuBackground() {
        String id = selectedId();
        return id.contains(":") ? CustomMenuBackgrounds.get(id) : null;
    }

    /** The built-in background in use, or null if a custom one is (see getCustomMenuBackground). */
    @Nullable
    public static MenuBackground getMenuBackground() {
        String id = selectedId();
        if (id.contains(":")) {
            return CustomMenuBackgrounds.get(id) != null ? null : MenuBackground.VORTEX;
        }
        return MenuBackground.byName(id, MenuBackground.VORTEX);
    }

    /** What the background button says. */
    public static Text getMenuBackgroundLabel() {
        CustomMenuBackgrounds.Entry custom = getCustomMenuBackground();
        if (custom != null) return custom.name();
        MenuBackground builtIn = getMenuBackground();
        return Text.translatable((builtIn != null ? builtIn : MenuBackground.VORTEX).translationKey());
    }

    public static void setMenuBackground(MenuBackground value) {
        select(value.name());
    }

    private static void select(String id) {
        String previous = selectedId();
        selected = id;
        save();
        // Free the animation we just switched away from.
        if (!previous.equals(id)) {
            VideoVortexRenderer.unload();
        }
    }

    /**
     * Moves on to the next background (title screen button and Mod Menu settings): the built-in
     * ones in order, then any from resource packs and addons.
     */
    public static MenuBackground cycleMenuBackground() {
        List<String> order = new ArrayList<>();
        for (MenuBackground builtIn : MenuBackground.values()) order.add(builtIn.name());
        for (CustomMenuBackgrounds.Entry custom : CustomMenuBackgrounds.all()) order.add(custom.id());

        CustomMenuBackgrounds.Entry custom = getCustomMenuBackground();
        MenuBackground builtIn = getMenuBackground();
        String current = custom != null ? custom.id() : (builtIn != null ? builtIn : MenuBackground.VORTEX).name();
        int index = order.indexOf(current);
        select(order.get((index + 1) % order.size()));
        return getMenuBackground();
    }
}
