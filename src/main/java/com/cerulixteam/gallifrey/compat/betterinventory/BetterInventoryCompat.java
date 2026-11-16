package com.cerulixteam.gallifrey.compat.betterinventory;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.AccessorySlot;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Better Inventory 2.x compat: puts the Sonic Shades and fez slots in a "Gallifrey" tab of
 * Better Inventory's Accessories side panel.
 *
 * Better Inventory 2.0 builds its accessories panel from a list of "accessory providers"
 * (one per mod: Accessories, Trinkets...). BetterInventoryAccessoriesMixin adds Gallifrey's
 * provider to that list. The provider is made at runtime with java.lang.reflect.Proxy against
 * Better Inventory's AccessoryProvider interface, so Gallifrey doesn't need Better Inventory to
 * compile and is unaffected when it isn't installed.
 *
 * Better Inventory 1.x had no accessories panel; BetterInventoryMenuMixin still covers that.
 */
public final class BetterInventoryCompat {
    private BetterInventoryCompat() {}

    public static final String PROVIDER_ID = "gallifrey";
    private static final String PROVIDER_INTERFACE = "dev.fallingcloud.betterinventory.compat.accessory.AccessoryProvider";

    private static Boolean v2;
    private static Object provider;

    /** True when Better Inventory 2.0 or newer is installed. */
    public static boolean isV2() {
        if (v2 == null) {
            v2 = FabricLoader.getInstance().getModContainer("betterinventory").map(mod -> {
                try {
                    return mod.getMetadata().getVersion().compareTo(Version.parse("2.0.0")) >= 0;
                } catch (VersionParsingException e) {
                    return true;
                }
            }).orElse(false);
        }
        return v2;
    }

    /** Whether this object is Gallifrey's provider (so it's only added once). */
    public static boolean isOurs(Object maybeProvider) {
        return maybeProvider != null && maybeProvider == provider;
    }

    /** Gallifrey's AccessoryProvider, or null if Better Inventory's interface can't be found. */
    public static synchronized Object provider() {
        if (provider != null) return provider;
        try {
            Class<?> type = Class.forName(PROVIDER_INTERFACE, false, BetterInventoryCompat.class.getClassLoader());
            InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
                case "id" -> PROVIDER_ID;
                case "title" -> Text.translatable("gui.gallifrey.betterinventory.tab");
                case "slots" -> slots((PlayerEntity) args[0]);
                case "canToggleRender" -> false;
                case "rendered" -> true;
                case "toggleRender", "emptyIcon" -> null; // the slots draw their own empty icons
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "Gallifrey accessory provider";
                default -> null;
            };
            provider = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        } catch (Throwable t) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't add the Gallifrey tab to Better Inventory's accessories panel", t);
        }
        return provider;
    }

    /** The two slots, in the order they appear in the panel. Positions are set by Better Inventory. */
    private static List<AccessorySlot> slots(PlayerEntity player) {
        return List.of(AccessorySlot.shades(player, 0, 0), AccessorySlot.fez(player, 0, 0));
    }
}
