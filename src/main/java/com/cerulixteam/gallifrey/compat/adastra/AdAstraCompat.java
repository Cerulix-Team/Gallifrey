package com.cerulixteam.gallifrey.compat.adastra;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SpaceSuitOxygenHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Lets the Gallifrey space suit work on Ad Astra's planets (Moon, Mars, Venus, Mercury, Glacio,
 * orbit), when Ad Astra is installed.
 *
 *  - Oxygen: on an airless Ad Astra planet the suit uses its own 10-minute Gallifrey oxygen supply
 *    (see SpaceSuitOxygenHandler). While it has oxygen, Ad Astra's suffocation is skipped. Inside
 *    an oxygen distributor's bubble the supply is not used up. When it runs out, Ad Astra's normal
 *    suffocation damage applies.
 *  - Cold: handled by data, not code. The suit is in #ad_astra:freeze_resistant_armor
 *    (data/ad_astra/tags/items/freeze_resistant_armor.json), like Ad Astra's own basic suit.
 *  - Ad Astra's own suits are left entirely to Ad Astra on its planets.
 *
 * No compile-time dependency, same as the TARDIS Refined compat: Ad Astra's API is looked up by
 * name, and none of this runs unless Ad Astra is installed. Written against Ad Astra 1.15.x
 * (the 1.20.1 line). If its API changes, this logs a warning and switches itself off rather than
 * crashing.
 */
public final class AdAstraCompat {
    private AdAstraCompat() {}

    public static final TagKey<Item> AD_ASTRA_SUITS =
            TagKey.of(RegistryKeys.ITEM, new Identifier("ad_astra", "space_suit_items"));

    private static boolean active = false;
    private static Object oxygenApi;
    private static Method worldHasOxygen;   // OxygenApi#hasOxygen(Level)
    private static Method entityHasOxygen;  // OxygenApi#hasOxygen(Entity)

    public static boolean isActive() {
        return active;
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("ad_astra")) return;

        // Gallifrey, Skaro and Mondas as rocket destinations, each with an orbit dimension
        // (Ad Astra only lists planets that have one). Shipped as a built-in datapack so the
        // orbit dimensions only exist when Ad Astra is installed.
        FabricLoader.getInstance().getModContainer(GallifreyMod.MOD_ID).ifPresent(container ->
                net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier(GallifreyMod.MOD_ID, "ad_astra_planets"), container,
                        net.minecraft.text.Text.literal("Gallifrey planets for Ad Astra"),
                        net.fabricmc.fabric.api.resource.ResourcePackActivationType.ALWAYS_ENABLED));

        try {
            Class<?> apiClass = Class.forName("earth.terrarium.adastra.api.systems.OxygenApi");
            oxygenApi = apiClass.getField("API").get(null);
            worldHasOxygen = apiClass.getMethod("hasOxygen", World.class);
            entityHasOxygen = apiClass.getMethod("hasOxygen", Entity.class);

            // AdAstraEvents.OxygenTickEvent: boolean tick(ServerLevel, LivingEntity);
            // returning false skips Ad Astra's oxygen check for that entity this tick.
            Class<?> listenerClass = Class.forName("earth.terrarium.adastra.api.events.AdAstraEvents$OxygenTickEvent");
            Object listener = Proxy.newProxyInstance(AdAstraCompat.class.getClassLoader(), new Class<?>[]{listenerClass},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "tick" -> !(args[1] instanceof PlayerEntity player
                                && SpaceSuitOxygenHandler.isBreathingFromGallifreySuit(player));
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        case "toString" -> "Gallifrey space suit oxygen listener";
                        default -> null;
                    });
            listenerClass.getMethod("register", listenerClass).invoke(null, listener);

            active = true;
            GallifreyMod.LOGGER.info("Ad Astra found: Gallifrey space suit now works on Ad Astra planets");
        } catch (ReflectiveOperationException | RuntimeException e) {
            active = false;
            GallifreyMod.LOGGER.warn("Ad Astra is installed but its API wasn't what Gallifrey expected; space suit compat is off", e);
        }
    }

    /** True in an Ad Astra dimension with no natural air (Moon, Mars, orbit...). */
    public static boolean isAirlessPlanet(World world) {
        if (!active) return false;
        try {
            return !(boolean) worldHasOxygen.invoke(oxygenApi, world);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /** True where this entity can breathe by Ad Astra's rules, e.g. inside an oxygen distributor's bubble. */
    public static boolean canBreatheHere(Entity entity) {
        if (!active) return true;
        try {
            return (boolean) entityHasOxygen.invoke(oxygenApi, entity);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    /** True when every armour slot holds a piece of an Ad Astra suit; Ad Astra looks after those itself. */
    public static boolean hasFullAdAstraSuit(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            if (!stack.isIn(AD_ASTRA_SUITS)) return false;
        }
        return true;
    }
}
