package com.cerulixteam.gallifrey.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class GallifreyFoodComponents {

    public static final FoodComponent MAPLE_SYRUP = new FoodComponent.Builder().hunger(3).saturationModifier(0.25f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200), 0.25f).build();

    public static final FoodComponent TREEBORG_PASTE = new FoodComponent.Builder().hunger(3).saturationModifier(0.25f)
            .statusEffect(new StatusEffectInstance(StatusEffects.POISON, 200), 1.0f).build();

}
