package com.cerulixteam.gallifrey.tardis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The complete set of TARDIS shell styles supplied with Gallifrey.
 * The old four-number tint system is intentionally gone: every shell is
 * a real texture/emission pair and the selected style is persisted by name.
 */
public final class TardisExteriorCatalog {
    public record Style(String id, String displayName) {}

    private static final List<Style> STYLES = List.of(
            new Style("policebox", "Police Box"),
            new Style("policebox_alt", "Police Box — Alternate"),
            new Style("policebox_alt2", "Police Box — Alternate II"),
            new Style("policebox_badwolf", "Bad Wolf"),
            new Style("policebox_coral", "Coral"),
            new Style("policebox_dino", "Dino"),
            new Style("policebox_purple", "Purple"),
            new Style("policebox_tokomak", "Tokomak"),
            new Style("gamblebox", "Gamblebox")
    );

    private TardisExteriorCatalog() {}

    public static List<Style> styles() {
        return Collections.unmodifiableList(STYLES);
    }

    public static List<String> names() {
        List<String> out = new ArrayList<>();
        for (Style style : STYLES) out.add(style.id());
        return out;
    }

    public static Style get(String id) {
        if (id == null) return STYLES.get(0);
        String wanted = id.toLowerCase(Locale.ROOT);
        for (Style style : STYLES) {
            if (style.id().equals(wanted)) return style;
        }
        return STYLES.get(0);
    }

    public static boolean contains(String id) {
        if (id == null) return false;
        String wanted = id.toLowerCase(Locale.ROOT);
        for (Style style : STYLES) if (style.id().equals(wanted)) return true;
        return false;
    }

    /** Silent migration for the old four-style saves. */
    public static String migrateLegacyIndex(int index) {
        return switch (Math.max(0, Math.min(3, index))) {
            case 1 -> "policebox_alt";
            case 2 -> "policebox_alt2";
            case 3 -> "policebox_badwolf";
            default -> "policebox";
        };
    }
}
