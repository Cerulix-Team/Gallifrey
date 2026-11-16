package com.cerulixteam.gallifrey.sonic;

import com.cerulixteam.gallifrey.item.custom.SonicScrewdriver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.EnumSet;
import java.util.Set;

/**
 * Sonic casings have to be unlocked, per sonic, with experience.
 *
 * - The Troughton casing (SECOND_DOCTOR) is always free.
 * - Every other casing costs COST_XP experience points (not levels) to unlock in a Sonic
 *   Workshop. Once unlocked it stays unlocked on that sonic, so you can swap back and forth.
 * - Creative players can use every casing for free.
 * - Sonics made before this existed keep the casing they already had: a sonic with no unlock
 *   list yet counts its current casing as unlocked, and that's saved the first time it changes.
 *
 * Stored on the sonic itself, in its NBT, as a list of casing ids ("UnlockedCasings").
 */
public final class SonicCasingUnlocks {
    private SonicCasingUnlocks() {}

    /** Experience points (not levels) to unlock one casing. */
    public static final int COST_XP = 100;
    /** The one casing every sonic starts with. */
    public static final SonicCasing FREE_CASING = SonicCasing.SECOND_DOCTOR;

    private static final String KEY = "UnlockedCasings";

    /** Whether this casing can be picked on this sonic without paying. */
    public static boolean isUnlocked(ItemStack sonic, SonicCasing casing) {
        return casing == FREE_CASING || unlocked(sonic).contains(casing);
    }

    /** Whether this player can pick the casing right now without paying (unlocked, or in creative). */
    public static boolean isFreeFor(PlayerEntity player, ItemStack sonic, SonicCasing casing) {
        return player.isCreative() || isUnlocked(sonic, casing);
    }

    /** Every unlocked casing on this sonic, including FREE_CASING. */
    public static Set<SonicCasing> unlocked(ItemStack sonic) {
        Set<SonicCasing> set = EnumSet.of(FREE_CASING);
        NbtCompound nbt = sonic.getNbt();
        if (nbt != null && nbt.contains(KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(KEY, NbtElement.STRING_TYPE);
            for (int i = 0; i < list.size(); i++) {
                SonicCasing casing = byExactId(list.getString(i));
                if (casing != null) set.add(casing);
            }
        } else {
            // Made before casings had to be unlocked: keep the one it already has.
            set.add(SonicScrewdriver.getCasing(sonic));
        }
        return set;
    }

    /** Adds a casing to this sonic's unlocked list (and saves the list if it wasn't saved yet). */
    public static void unlock(ItemStack sonic, SonicCasing casing) {
        Set<SonicCasing> set = unlocked(sonic);
        set.add(casing);
        NbtList list = new NbtList();
        for (SonicCasing each : set) list.add(NbtString.of(each.getId()));
        sonic.getOrCreateNbt().put(KEY, list);
    }

    /** Saves the unlocked list as it is now, so an older sonic keeps its current casing after switching. */
    public static void save(ItemStack sonic) {
        NbtCompound nbt = sonic.getNbt();
        if (nbt == null || !nbt.contains(KEY, NbtElement.LIST_TYPE)) {
            unlock(sonic, FREE_CASING);
        }
    }

    /** The player's experience in points (works out the real total from level and progress). */
    public static int points(PlayerEntity player) {
        int level = player.experienceLevel;
        double base;
        if (level <= 16) {
            base = level * level + 6.0 * level;
        } else if (level <= 31) {
            base = 2.5 * level * level - 40.5 * level + 360.0;
        } else {
            base = 4.5 * level * level - 162.5 * level + 2220.0;
        }
        return (int) Math.round(base + player.experienceProgress * player.getNextLevelExperience());
    }

    private static SonicCasing byExactId(String id) {
        for (SonicCasing casing : SonicCasing.values()) {
            if (casing.getId().equals(id)) return casing;
        }
        return null;
    }
}
