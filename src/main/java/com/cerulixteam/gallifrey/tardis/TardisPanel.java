package com.cerulixteam.gallifrey.tardis;

/**
 * The TARDIS control panels, Dalek Mod style: each is its own block that goes on a floor, wall or ceiling
 * inside a TARDIS, and some hold several buttons (see TardisPanelLayout for where each button is).
 *
 *  - Flight lever: pull to take off; once you've arrived, pull it again to land.
 *  - Coordinate panel: X / Y / Z  - and +, increment dial, rotation dial, find-ground switch.
 *  - Dimension selector: previous / show / next.
 *  - Fast return: sets the destination back to where you last took off from.
 *  - Lock panel: door button (open/close) and key (lock/unlock).
 *  - Handbrake, refueller and randomiser: Gallifrey's own extras.
 */
public enum TardisPanel {
    FLIGHT_LEVER("flight_lever"),
    COORDINATE_PANEL("coordinate_panel"),
    DIMENSION_SELECTOR("dimension_selector"),
    FAST_RETURN("fast_return"),
    LOCK_PANEL("lock_panel"),
    HANDBRAKE("handbrake"),
    REFUELER("refueler"),
    RANDOMISER("randomiser");

    private final String id;
    private TardisPanelLayout.Layout layout;

    TardisPanel(String id) {
        this.id = id;
    }

    /** Block/registry name without the namespace, e.g. "tardis_flight_lever". */
    public String blockId() { return "tardis_" + id; }

    public TardisPanelLayout.Layout layout() {
        if (layout == null) layout = TardisPanelLayout.of(this);
        return layout;
    }

    /** Whether the panel shows an on/off position that follows the TARDIS (levers, the lock key, find-ground). */
    public boolean hasOnState() {
        return this == FLIGHT_LEVER || this == COORDINATE_PANEL || this == LOCK_PANEL || this == HANDBRAKE || this == REFUELER;
    }

    /** Whether using it shows a button (or the fast return lever) being pressed for a moment. */
    public boolean showsPress() {
        return !layout().lever() || this == FAST_RETURN;
    }
}
