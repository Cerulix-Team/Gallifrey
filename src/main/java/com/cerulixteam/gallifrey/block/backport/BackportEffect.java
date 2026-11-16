package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** The 1.21 status effects (Infested, Oozing, Weaving, Wind Charged). What they do is in BackportExtras. */
public class BackportEffect extends StatusEffect {
    public BackportEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
