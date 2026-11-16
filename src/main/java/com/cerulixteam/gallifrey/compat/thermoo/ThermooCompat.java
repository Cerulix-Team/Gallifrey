package com.cerulixteam.gallifrey.compat.thermoo;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SpaceSuits;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Frostiful and Scorchful support (both run on the Thermoo library).
 *
 * Those mods heat or chill players from the biome they're in. Gallifrey's airless worlds give
 * their biomes a high temperature (which is what keeps rain and snow away), so Scorchful treated
 * the Moon, Mars and space as baking hot deserts. With this:
 *  - Moon, Mars, Mondas and space: the sun doesn't heat you (Mondas and space can still chill
 *    you; the cold is real).
 *  - Wearing a full airtight space suit in those places: no passive heating or chilling at all;
 *    the suit keeps you at a steady temperature.
 *  - Inside the TARDIS: no passive heating or chilling; it's climate controlled.
 *  - Everywhere else (Gallifrey, Skaro's deserts, the Overworld...): Frostiful and Scorchful work
 *    as normal.
 *
 * Separately, Frostiful already counts the space suit as very warm armour: its warm-armour tag
 * includes #ad_astra:freeze_resistant_armor, which Gallifrey fills in itself
 * (data/ad_astra/tags/items/freeze_resistant_armor.json), so that works even without Ad Astra.
 *
 * No compile-time dependency: Thermoo's event is found by name and subscribed to at runtime, and
 * none of this runs without Thermoo. Written against Thermoo for 1.20.1. If it changes, this
 * logs a warning and switches itself off.
 */
public final class ThermooCompat {
    private ThermooCompat() {}

    private static final Set<String> AIRLESS = Set.of("moon", "mars", "mondas", "gallifrey_orbit", "skaro_orbit", "mondas_orbit");
    private static final String TARDIS = "tardis";

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("thermoo")) return;
        try {
            Class<?> events = Class.forName("com.github.thedeathlycow.thermoo.api.temperature.event.PlayerEnvironmentEvents");
            Class<?> callback = Class.forName("com.github.thedeathlycow.thermoo.api.temperature.event.PlayerEnvironmentEvents$TemperatureChangeEventCallback");
            Event event = (Event) events.getField("CAN_APPLY_PASSIVE_TEMPERATURE_CHANGE").get(null);

            // boolean canApplyChange(int change, PlayerEntity player): false blocks that change.
            Object listener = Proxy.newProxyInstance(ThermooCompat.class.getClassLoader(), new Class<?>[]{callback},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "canApplyChange" -> canApplyChange((int) args[0], (PlayerEntity) args[1]);
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        case "toString" -> "Gallifrey temperature rules";
                        default -> null;
                    });
            event.register(listener);
            GallifreyMod.LOGGER.info("[Gallifrey] Thermoo found: Frostiful/Scorchful follow Gallifrey's space and TARDIS rules");
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Thermoo is installed but wasn't what Gallifrey expected; temperature compat is off", e);
        }
    }

    /** Positive changes warm the player up, negative ones chill them. */
    private static boolean canApplyChange(int change, PlayerEntity player) {
        Identifier world = player.getWorld().getRegistryKey().getValue();
        if (!GallifreyMod.MOD_ID.equals(world.getNamespace())) return true;
        String path = world.getPath();

        if (path.equals(TARDIS)) return false;
        if (AIRLESS.contains(path)) {
            if (SpaceSuits.hasFullSuit(player)) return false;
            return change <= 0; // no sunbathing on the Moon
        }
        return true;
    }
}
