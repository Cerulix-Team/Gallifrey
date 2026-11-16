package com.cerulixteam.gallifrey.compat.graves;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.GallifreyAccessories;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Universal Graves support: worn Sonic Shades and fezzes go into the grave with everything else,
 * and back into their own slots when the grave is collected (or into the inventory if the slot's
 * already taken).
 *
 * Needed because Universal Graves replaces Minecraft's death drop entirely, so Gallifrey's own
 * "drop the shades and fez on death" never ran. (GallifreyAccessories also has a safety net so
 * they can never just vanish, whatever mod is installed.) You're in Grave Danger doesn't need
 * this: it lets the normal death drop run, so they land next to its grave.
 *
 * No compile-time dependency: Universal Graves' "inventory mask" interface is implemented at
 * runtime, and none of this runs without the mod. Written against Universal Graves 3.0.3 for
 * 1.20.1. If it changes, this logs a warning and switches itself off.
 */
public final class UniversalGravesCompat {
    private UniversalGravesCompat() {}

    private static final int SHADES_SLOT = 0;
    private static final int FEZ_SLOT = 1;

    private static Method canAddItem;   // static GravesApi#canAddItem(ServerPlayerEntity, ItemStack)
    private static Method addItem;      // GraveInventoryMask.ItemConsumer#addItem(ItemStack, int)

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("universal-graves")) return;
        try {
            Class<?> api = Class.forName("eu.pb4.graves.GravesApi");
            Class<?> mask = Class.forName("eu.pb4.graves.grave.GraveInventoryMask");
            Class<?> consumer = Class.forName("eu.pb4.graves.grave.GraveInventoryMask$ItemConsumer");
            canAddItem = api.getMethod("canAddItem", ServerPlayerEntity.class, ItemStack.class);
            addItem = consumer.getMethod("addItem", ItemStack.class, int.class);

            Identifier id = new Identifier(GallifreyMod.MOD_ID, "accessories");
            Object proxy = Proxy.newProxyInstance(UniversalGravesCompat.class.getClassLoader(), new Class<?>[]{mask},
                    (self, method, args) -> switch (method.getName()) {
                        case "addToGrave" -> {
                            addToGrave((ServerPlayerEntity) args[0], args[1]);
                            yield null;
                        }
                        case "moveToPlayerExactly" -> moveExactly((ServerPlayerEntity) args[0], (ItemStack) args[1], (int) args[2]);
                        case "moveToPlayerClosest" -> moveClosest((ServerPlayerEntity) args[0], (ItemStack) args[1]);
                        case "getId" -> id;
                        case "hashCode" -> System.identityHashCode(self);
                        case "equals" -> self == args[0];
                        case "toString" -> "Gallifrey accessories grave mask";
                        default -> null;
                    });
            api.getMethod("registerInventoryMask", Identifier.class, mask).invoke(null, id, proxy);
            GallifreyMod.LOGGER.info("[Gallifrey] Universal Graves found: Sonic Shades and fezzes go into graves");
        } catch (ReflectiveOperationException | RuntimeException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Universal Graves is installed but wasn't what Gallifrey expected; grave compat is off", e);
        }
    }

    // Gallifrey's own slots only: with Trinkets the shades and fez are in Trinkets slots, which
    // Universal Graves handles itself.
    private static void addToGrave(ServerPlayerEntity player, Object consumer) throws ReflectiveOperationException {
        ItemStack shades = GallifreyAccessories.getStoredShades(player);
        if ((boolean) canAddItem.invoke(null, player, shades)) {
            addItem.invoke(consumer, shades.copy(), SHADES_SLOT);
            GallifreyAccessories.setStoredShades(player, ItemStack.EMPTY);
        }
        ItemStack fez = GallifreyAccessories.getStoredFez(player);
        if ((boolean) canAddItem.invoke(null, player, fez)) {
            addItem.invoke(consumer, fez.copy(), FEZ_SLOT);
            GallifreyAccessories.setStoredFez(player, ItemStack.EMPTY);
        }
    }

    /** Back into the slot it came from, if that slot's free. */
    private static boolean moveExactly(ServerPlayerEntity player, ItemStack stack, int slot) {
        if (slot == SHADES_SLOT && GallifreyAccessories.getStoredShades(player).isEmpty() && GallifreyAccessories.fitsShadesSlot(stack)) {
            GallifreyAccessories.setStoredShades(player, stack.copy());
            stack.setCount(0); // Universal Graves checks the stack went empty
            return true;
        }
        if (slot == FEZ_SLOT && GallifreyAccessories.getStoredFez(player).isEmpty() && GallifreyAccessories.fitsFezSlot(stack)) {
            GallifreyAccessories.setStoredFez(player, stack.copy());
            stack.setCount(0);
            return true;
        }
        return false;
    }

    /** Otherwise into the inventory; if that's full, Universal Graves drops it. */
    private static boolean moveClosest(ServerPlayerEntity player, ItemStack stack) {
        // insertStack shrinks the stack by what fitted; Universal Graves drops whatever's left.
        return player.getInventory().insertStack(stack);
    }
}
