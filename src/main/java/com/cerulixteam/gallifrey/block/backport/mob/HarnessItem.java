package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;

/** Harness (1.21.6): put it on a grown Happy Ghast to ride it. One per colour. */
public class HarnessItem extends Item {
    public final DyeColor color;

    public HarnessItem(DyeColor color, Settings settings) {
        super(settings);
        this.color = color;
    }
}
