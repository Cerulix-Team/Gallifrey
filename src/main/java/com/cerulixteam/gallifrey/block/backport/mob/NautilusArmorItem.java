package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.item.Item;

/** Nautilus Armor (1.21.11), in copper, iron, gold, diamond and netherite. */
public class NautilusArmorItem extends Item {
    public static final String[] MATERIALS = {"copper", "iron", "golden", "diamond", "netherite"};
    /** Armour points, like horse armour. */
    public static final int[] PROTECTION = {4, 5, 7, 11, 13};
    public final int material;

    public NautilusArmorItem(int material, Settings settings) {
        super(settings);
        this.material = material;
    }
}
