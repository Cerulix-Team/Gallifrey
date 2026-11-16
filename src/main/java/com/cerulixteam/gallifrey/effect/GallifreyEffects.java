package com.cerulixteam.gallifrey.effect;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class GallifreyEffects {

public static final RegistryEntry<StatusEffect> ANTIRADIATION = registerStatusEffect("anti-radiation",
        new AntiRadiation(StatusEffectCategory.BENEFICIAL,5635925 ));

    public static RegistryEntry<StatusEffect> registerStatusEffect(String name, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(GallifreyMod.MOD_ID, name), statusEffect);
    }

    public static void registerEffects() {
        GallifreyMod.LOGGER.info("Registering Mod Effects for " + GallifreyMod.MOD_ID);
    }
}
