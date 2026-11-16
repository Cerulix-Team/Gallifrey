package com.cerulixteam.gallifrey.tardis;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;

/** Built-in TARDIS interior structures bundled with Gallifrey. */
public final class TardisInteriorCatalog {
    private static final List<String> NAMES = List.of(
            "fugitive", "trenzaloremissy", "70default", "bluetardis", "fnaf4", "missy",
            "medieval_int", "second", "sprucewood", "rosewood", "third_alt", "tardis_platform",
            "cavernwood", "second_alt", "capalditoyota", "toywardian", "ladylibertyinterior",
            "darkrock", "mcgann", "ravencrest", "third"
    );

    private TardisInteriorCatalog() {}

    public static List<String> names() {
        return NAMES;
    }

    public static Identifier id(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        if (!NAMES.contains(normalized)) return null;
        return new Identifier("gallifrey", "interiors/" + normalized);
    }
}
