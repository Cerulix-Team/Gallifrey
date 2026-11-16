package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.compat.trinkets.TrinketsCompat;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.GameRules;

/**
 * Gallifrey's two wearable accessory slots:
 *  - the Sonic Shades slot (Sonic Shades only)
 *  - the fez slot (anything in the gallifrey:fezzes item tag)
 *
 * Everything that reads or changes what's worn goes through here, so it works the same wherever
 * the stacks actually live:
 *  - <b>Without Trinkets</b>: in Gallifrey's own two slots, shown in the inventory. The stacks live
 *    on the player (SonicShadesSlotMixin), are synced to every client so other players see them,
 *    are saved with the player, and drop on death (kept with keepInventory).
 *  - <b>With Trinkets</b> (or Accessories' Trinkets layer): in Trinkets' face slot (shades) and hat
 *    slot (fez). Gallifrey's own slots aren't added, so nothing clashes with Trinkets' slots or
 *    with Better Inventory's Trinkets tab; Trinkets handles saving, syncing and dropping. Anything
 *    still in the old Gallifrey slots is moved across when the player joins.
 *
 * Either way they render on the head like a helmet-slot item (AccessoryFeatureRenderer).
 */
public final class GallifreyAccessories {
    private GallifreyAccessories() {}

    public static final TagKey<Item> FEZZES = TagKey.of(RegistryKeys.ITEM, new Identifier("gallifrey", "fezzes"));

    // The Trinkets slots used when Trinkets is installed.
    private static final String TRINKET_GROUP = "head";
    private static final String SHADES_TRINKET = "face";
    private static final String FEZ_TRINKET = "hat";

    public static boolean fitsShadesSlot(ItemStack stack) {
        return stack.getItem() instanceof SonicShadesItem;
    }

    public static boolean fitsFezSlot(ItemStack stack) {
        return stack.isIn(FEZZES);
    }

    /** True when the shades and fez live in Trinkets slots, so Gallifrey shows no slots of its own. */
    public static boolean usesTrinkets() {
        return TrinketsCompat.isLoaded();
    }

    public static ItemStack getShades(PlayerEntity player) {
        if (usesTrinkets()) {
            ItemStack worn = TrinketsCompat.findWorn(player, GallifreyAccessories::fitsShadesSlot);
            return worn.isEmpty() ? getStoredShades(player) : worn;
        }
        return getStoredShades(player);
    }

    public static void setShades(PlayerEntity player, ItemStack stack) {
        if (usesTrinkets() && TrinketsCompat.setWorn(player, TRINKET_GROUP, SHADES_TRINKET, GallifreyAccessories::fitsShadesSlot, stack)) {
            if (!getStoredShades(player).isEmpty()) setStoredShades(player, ItemStack.EMPTY);
            return;
        }
        // No Trinkets, or no face slot for it: keep it in Gallifrey's own slot so it's never lost.
        setStoredShades(player, stack);
    }

    public static ItemStack getFez(PlayerEntity player) {
        if (usesTrinkets()) {
            ItemStack worn = TrinketsCompat.findWorn(player, GallifreyAccessories::fitsFezSlot);
            return worn.isEmpty() ? getStoredFez(player) : worn;
        }
        return getStoredFez(player);
    }

    public static void setFez(PlayerEntity player, ItemStack stack) {
        if (usesTrinkets() && TrinketsCompat.setWorn(player, TRINKET_GROUP, FEZ_TRINKET, GallifreyAccessories::fitsFezSlot, stack)) {
            if (!getStoredFez(player).isEmpty()) setStoredFez(player, ItemStack.EMPTY);
            return;
        }
        setStoredFez(player, stack);
    }

    /**
     * Call after changing a worn stack in place (its NBT, say), so the change is sent to clients.
     * Setting the slot again is what tells Minecraft it changed. (Trinkets notices in-place
     * changes on its own, so this is only needed for Gallifrey's own slot, but is harmless.)
     */
    public static void resyncShades(PlayerEntity player) {
        if (!usesTrinkets() || !getStoredShades(player).isEmpty()) {
            setStoredShades(player, getStoredShades(player));
        }
    }

    // Gallifrey's own slots, stored on the player. Empty when Trinkets is in use (after the move).

    public static ItemStack getStoredShades(PlayerEntity player) {
        return ((SonicShadesSlotAccess) player).gallifrey$getSonicShades();
    }

    public static void setStoredShades(PlayerEntity player, ItemStack stack) {
        ((SonicShadesSlotAccess) player).gallifrey$setSonicShades(stack);
    }

    public static ItemStack getStoredFez(PlayerEntity player) {
        return ((FezSlotAccess) player).gallifrey$getFez();
    }

    public static void setStoredFez(PlayerEntity player, ItemStack stack) {
        ((FezSlotAccess) player).gallifrey$setFez(stack);
    }

    public static void register() {
        // Keep Gallifrey's own slots across respawning (with keepInventory) and returning from the
        // End. Without keepInventory they were already dropped on death (SonicShadesSlotMixin).
        // Trinkets slots are Trinkets' business, so only the stored slots are copied here.
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive || newPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
                setStoredShades(newPlayer, getStoredShades(oldPlayer));
                setStoredFez(newPlayer, getStoredFez(oldPlayer));
                return;
            }
            // Safety net: they should already have been dropped (or put in a grave) on death. If
            // another mod skipped Minecraft's death drop, they'd otherwise vanish, so drop them
            // where the player died.
            for (ItemStack leftover : new ItemStack[]{getStoredShades(oldPlayer), getStoredFez(oldPlayer)}) {
                if (!leftover.isEmpty()) {
                    ItemScatterer.spawn(oldPlayer.getWorld(), oldPlayer.getX(), oldPlayer.getY(), oldPlayer.getZ(), leftover.copy());
                }
            }
            setStoredShades(oldPlayer, ItemStack.EMPTY);
            setStoredFez(oldPlayer, ItemStack.EMPTY);
        });

        // With Trinkets: move anything still in Gallifrey's own slots (from before Trinkets was
        // added) into the Trinkets slots, or into the inventory if those are taken.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!usesTrinkets()) return;
            PlayerEntity player = handler.player;
            ItemStack shades = getStoredShades(player);
            if (!shades.isEmpty()) {
                setStoredShades(player, ItemStack.EMPTY);
                moveToTrinketOrInventory(player, shades, SHADES_TRINKET, GallifreyAccessories::fitsShadesSlot);
            }
            ItemStack fez = getStoredFez(player);
            if (!fez.isEmpty()) {
                setStoredFez(player, ItemStack.EMPTY);
                moveToTrinketOrInventory(player, fez, FEZ_TRINKET, GallifreyAccessories::fitsFezSlot);
            }
        });
    }

    private static void moveToTrinketOrInventory(PlayerEntity player, ItemStack stack, String slot,
                                                 java.util.function.Predicate<ItemStack> belongs) {
        boolean free = TrinketsCompat.findWorn(player, belongs).isEmpty();
        if (free && TrinketsCompat.setWorn(player, TRINKET_GROUP, slot, belongs, stack)) {
            GallifreyMod.LOGGER.info("[Gallifrey] Moved {}'s {} into their Trinkets {} slot", player.getName().getString(),
                    stack.getName().getString(), slot);
            return;
        }
        player.getInventory().offerOrDrop(stack.copy());
    }
}
