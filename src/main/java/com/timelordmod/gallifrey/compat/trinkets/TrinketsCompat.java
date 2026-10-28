package com.timelordmod.gallifrey.compat.trinkets;

import com.timelordmod.gallifrey.GallifreyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Trinkets support (Fabric 1.20.1, Trinkets 3.7).
 *
 *  - The Vortex Manipulator can be worn in either glove slot (it shows on your wrist), and still
 *    works from there: the Vortex Manipulator key (K by default) opens it, and the server finds
 *    it there for every jump. See TrinketsClientCompat for the key and the rendering.
 *  - The Sonic Screwdriver can be clipped into the belt slot (it hangs on your hip).
 *  - The Sonic Shades go in Trinkets' face slot and fezzes in its hat slot. With Trinkets
 *    installed, Gallifrey doesn't add its own shades and fez slots at all (they used to sit next
 *    to Trinkets' and the two fought over the screen and the slot numbers); GallifreyAccessories
 *    reads and writes these Trinkets slots instead, so the shades still work and both still show
 *    on your head. Anything left in the old Gallifrey slots is moved across when you join.
 *  - The slots are switched on for players in data/trinkets/entities/gallifrey.json, and the
 *    items allowed in them are in data/trinkets/tags/items/... Those files do nothing without
 *    Trinkets.
 *
 * Also covers Accessories' Trinkets compat layer (tclayer), which provides the same Trinkets API.
 *
 * No compile-time dependency: the few Trinkets calls are made by reflection, and nothing here
 * runs unless Trinkets is installed. Checked against Trinkets' 1.20.1 source (3.7.2).
 */
public final class TrinketsCompat {
    private TrinketsCompat() {}

    public static final String MOD_ID = "trinkets";

    private static boolean loaded;
    private static Method getTrinketComponent;   // TrinketsApi.getTrinketComponent(LivingEntity) -> Optional<TrinketComponent>
    private static Method getEquipped;           // TrinketComponent.getEquipped(Predicate<ItemStack>) -> List<Pair<SlotReference, ItemStack>>
    private static Method getInventory;          // TrinketComponent.getInventory() -> Map<String, Map<String, TrinketInventory>>

    public static boolean isLoaded() {
        return loaded;
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            Class<?> api = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Class<?> component = Class.forName("dev.emi.trinkets.api.TrinketComponent");
            getTrinketComponent = api.getMethod("getTrinketComponent", LivingEntity.class);
            getEquipped = component.getMethod("getEquipped", Predicate.class);
            getInventory = component.getMethod("getInventory");
            loaded = true;
            GallifreyMod.LOGGER.info("[Gallifrey] Trinkets found: the Vortex Manipulator, sonic, Sonic Shades and fezzes use trinket slots");
        } catch (ReflectiveOperationException e) {
            GallifreyMod.LOGGER.warn("[Gallifrey] Trinkets is installed but its API has changed; trinket slot support is off", e);
        }
    }

    /** The first stack of {@code item} worn in any trinket slot, or EMPTY. */
    public static ItemStack findWorn(LivingEntity entity, Item item) {
        return findWorn(entity, stack -> stack.isOf(item));
    }

    /** The first worn trinket matching {@code predicate}, or EMPTY. */
    public static ItemStack findWorn(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (!loaded || entity == null) return ItemStack.EMPTY;
        try {
            Optional<?> component = (Optional<?>) getTrinketComponent.invoke(null, entity);
            if (component.isEmpty()) return ItemStack.EMPTY;
            List<?> equipped = (List<?>) getEquipped.invoke(component.get(), predicate);
            for (Object entry : equipped) {
                if (entry instanceof Pair<?, ?> pair && pair.getRight() instanceof ItemStack stack && !stack.isEmpty()) {
                    return stack;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Treat any Trinkets hiccup as "not worn" rather than crashing.
        }
        return ItemStack.EMPTY;
    }

    /**
     * The trinket inventory for one slot type (e.g. group "head", slot "face"), or null if the
     * entity doesn't have that slot.
     */
    private static Inventory slotInventory(LivingEntity entity, String group, String slot) {
        if (!loaded || entity == null) return null;
        try {
            Optional<?> component = (Optional<?>) getTrinketComponent.invoke(null, entity);
            if (component.isEmpty()) return null;
            Map<?, ?> groups = (Map<?, ?>) getInventory.invoke(component.get());
            if (!(groups.get(group) instanceof Map<?, ?> slots)) return null;
            return slots.get(slot) instanceof Inventory inventory && inventory.size() > 0 ? inventory : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /** Whether the entity has the given trinket slot (e.g. "head", "face"). */
    public static boolean hasSlot(LivingEntity entity, String group, String slot) {
        return slotInventory(entity, group, slot) != null;
    }

    /**
     * Puts {@code stack} in the given trinket slot, replacing what's there that matches
     * {@code belongs} (or, with an empty stack, removes it). Trinkets syncs the change to clients
     * itself. Returns false, changing nothing, if the entity doesn't have that slot or every one
     * of them is taken by something else.
     */
    public static boolean setWorn(LivingEntity entity, String group, String slot, Predicate<ItemStack> belongs, ItemStack stack) {
        Inventory inventory = slotInventory(entity, group, slot);
        if (inventory == null) return false;
        if (stack.isEmpty()) {
            for (int i = 0; i < inventory.size(); i++) {
                if (belongs.test(inventory.getStack(i))) inventory.setStack(i, ItemStack.EMPTY);
            }
        } else {
            int target = -1;
            for (int i = 0; i < inventory.size() && target < 0; i++) {
                if (belongs.test(inventory.getStack(i))) target = i;
            }
            for (int i = 0; i < inventory.size() && target < 0; i++) {
                if (inventory.getStack(i).isEmpty()) target = i;
            }
            if (target < 0) return false;
            inventory.setStack(target, stack.copy());
        }
        inventory.markDirty();
        return true;
    }
}
