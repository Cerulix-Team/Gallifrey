package com.timelordmod.gallifrey.client;

import org.jetbrains.annotations.Nullable;

/**
 * The title screen backgrounds you can pick from. The title screen button and the Mod Menu
 * settings both cycle through these in order.
 *
 *  - PANORAMA: Minecraft's normal rotating panorama.
 *  - VORTEX: the code-drawn time vortex (VortexBackgroundRenderer).
 *  - TRUSTABLE: the code-drawn Trustable tunnel (TrustableVortexRenderer).
 *  - The rest are looping vortex animations, played from frames in
 *    menu_vortex/<folder>/ (VideoVortexRenderer).
 */
public enum MenuBackground {
    PANORAMA(null),
    VORTEX(null),
    TIME_WINDS("time_winds"),
    TIME_WAR("time_war"),
    THE_VOID("the_void"),
    UNTEMPERED_SCHISM("untempered_schism"),
    EYE_OF_HARMONY("eye_of_harmony"),
    /** Code-drawn too (TrustableVortexRenderer): the Trustable pattern as a tunnel. */
    TRUSTABLE(null);

    @Nullable private final String frameFolder;

    MenuBackground(@Nullable String frameFolder) {
        this.frameFolder = frameFolder;
    }

    /** Folder of animation frames, or null for the panorama and the code-drawn vortex. */
    @Nullable
    public String frameFolder() {
        return frameFolder;
    }

    public String translationKey() {
        return "gui.gallifrey.menu_background." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public MenuBackground next() {
        MenuBackground[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static MenuBackground byName(String name, MenuBackground fallback) {
        for (MenuBackground background : values()) {
            if (background.name().equalsIgnoreCase(name)) return background;
        }
        return fallback;
    }
}
