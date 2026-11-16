package com.cerulixteam.gallifrey.item.custom;

/**
 * Where the Sonic Shades and fez slots sit in the inventory screens (x/y relative to the screen's
 * top-left corner, as Minecraft's Slot uses).
 *
 * In the survival inventory they're in the column between the player preview and the crafting
 * grid, above the offhand slot; in the creative tab, right of the armour.
 *
 * These are only used without Trinkets. With Trinkets (or Accessories' Trinkets layer) the shades
 * and fez go in Trinkets' face and hat slots and Gallifrey adds no slots of its own
 * (GallifreyAccessories), so there's nothing to keep clear of.
 */
public final class AccessorySlotLayout {
    private AccessorySlotLayout() {}

    // Survival inventory.
    public static final int SHADES_X = 77;
    public static final int SHADES_Y = 8;
    public static final int FEZ_X = 77;
    public static final int FEZ_Y = 26;

    // Creative inventory tab.
    public static final int CREATIVE_SHADES_X = 127;
    public static final int CREATIVE_SHADES_Y = 6;
    public static final int CREATIVE_FEZ_X = 127;
    public static final int CREATIVE_FEZ_Y = 33;
}
