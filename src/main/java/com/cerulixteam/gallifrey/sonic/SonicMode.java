package com.cerulixteam.gallifrey.sonic;

public enum SonicMode {
    INTERACTION("Interaction"),
    OVERLOAD("Overload"),
    SCAN("Scanning"),
    /** Only offered when Adventures in Time is installed (see compat.ait.AitSonics). */
    TARDIS("TARDIS");

    private final String displayName;

    SonicMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
