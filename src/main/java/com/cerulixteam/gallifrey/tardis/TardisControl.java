package com.cerulixteam.gallifrey.tardis;

/**
 * The separate TARDIS control blocks, Dalek Mod style. Each one is its own block that you place
 * anywhere inside a TARDIS interior; it controls the TARDIS whose interior it is in.
 *
 * Right-click uses a control. Sneak + right-click turns a number control the other way
 * (X/Y/Z down, previous dimension, previous facing, smaller increment).
 *
 * The throttle and monitor are separate blocks of their own (TardisThrottleBlock, TardisMonitorBlock).
 */
public enum TardisControl {
    HANDBRAKE("handbrake", Kind.LEVER),
    DOOR("door_control", Kind.LEVER),
    REFUELER("refueler", Kind.LEVER),
    LANDING_TYPE("landing_type", Kind.LEVER),
    X("x_control", Kind.BUTTON),
    Y("y_control", Kind.BUTTON),
    Z("z_control", Kind.BUTTON),
    INCREMENT("increment", Kind.BUTTON),
    FACING("facing_control", Kind.BUTTON),
    DIMENSION("dimension_control", Kind.BUTTON),
    RANDOMISER("randomiser", Kind.BUTTON),
    FAST_RETURN("fast_return", Kind.BUTTON);

    /** LEVER controls show an on/off position that follows the TARDIS; BUTTON controls press and spring back. */
    public enum Kind { LEVER, BUTTON }

    /** Coordinate step sizes the increment control cycles through. */
    public static final int[] INCREMENTS = {1, 10, 100, 1000, 10000};

    private final String id;
    private final Kind kind;

    TardisControl(String id, Kind kind) {
        this.id = id;
        this.kind = kind;
    }

    /** Block/registry name without the namespace, e.g. "tardis_handbrake". */
    public String blockId() { return "tardis_" + id; }

    public Kind kind() { return kind; }
}
