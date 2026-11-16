package com.cerulixteam.gallifrey.tardis;

/** What a button on a TARDIS panel does (see TardisFlight.usePanel). */
public enum TardisPanelAction {
    FLIGHT, HANDBRAKE, REFUEL, DOORS, LOCK,
    X_UP, X_DOWN, Y_UP, Y_DOWN, Z_UP, Z_DOWN,
    INCREMENT, ROTATION, FIND_GROUND,
    DIM_PREV, DIM_SHOW, DIM_NEXT,
    FAST_RETURN, RANDOMISE;

    /** Buttons that step through a list: sneak to go the other way. */
    public boolean cycles() {
        return this == INCREMENT || this == ROTATION;
    }
}
