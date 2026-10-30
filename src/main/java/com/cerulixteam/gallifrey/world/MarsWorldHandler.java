package com.cerulixteam.gallifrey.world;

/**
 * Retained for compatibility with older initializers. Gravity is now applied
 * directly by MoonGravityMixin to living entities in both Mars and the Moon.
 */
public final class MarsWorldHandler {
    private MarsWorldHandler() {}
    public static void register() {
        // Intentionally empty: per-tick velocity compensation fought the gravity mixin.
    }
}
