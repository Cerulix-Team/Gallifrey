package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.compat.adastra.AdAstraCompat;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Lightweight standalone oxygen system for the Gallifrey mod's airless worlds.
 * A full suit (Gallifrey's own, or Ad Astra's when installed) stores 10 minutes of oxygen
 * in the chestplate's NBT. Oxygen refills
 * in the Overworld and the breathable Gallifrey/Skaro/prehistoric worlds.
 *
 * With Ad Astra installed, the same supply also covers Ad Astra's airless planets (see
 * AdAstraCompat). There it isn't used up inside an oxygen distributor's bubble, and Ad Astra's
 * own suits are left to Ad Astra.
 */
public final class SpaceSuitOxygenHandler {
    private static final String OXYGEN_KEY = "GallifreyOxygenTicks";
    private static final int MAX_OXYGEN_TICKS = 20 * 60 * 10;
    private SpaceSuitOxygenHandler() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SpaceSuitOxygenHandler::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        boolean adAstraPlanet = AdAstraCompat.isAirlessPlanet(world);
        for (PlayerEntity player : world.getPlayers()) {
            if (isMoonWorld(world) && !hasFullSuit(player)) {
                if (world.getTime() % 20L == 0L) {
                    player.damage(world.getDamageSources().drown(), 4.0F);
                }
                continue;
            }
            if (!hasFullSuit(player)) continue;
            NbtCompound nbt = player.getEquippedStack(EquipmentSlot.CHEST).getOrCreateNbt();

            if (adAstraPlanet) {
                // Ad Astra's own suits run on Ad Astra's oxygen system there.
                if (AdAstraCompat.hasFullAdAstraSuit(player)) continue;
                // Inside an oxygen distributor's bubble: breathable, so don't use the tank.
                if (AdAstraCompat.canBreatheHere(player)) continue;
            } else if (!isVacuumWorld(world)) {
                // Refill the suit automatically whenever the player is outside a vacuum world.
                nbt.putInt(OXYGEN_KEY, MAX_OXYGEN_TICKS);
                continue;
            }

            int oxygen = getOxygen(nbt);
            if (oxygen > 0) player.setAir(Math.max(player.getAir(), 300));
            if (world.getTime() % 20L != 0L) continue;
            if (oxygen > 0) {
                oxygen--;
                nbt.putInt(OXYGEN_KEY, oxygen);
                player.setAir(300);
            } else if (!adAstraPlanet) {
                // No oxygen remains: the sealed suit no longer protects its wearer.
                // (On Ad Astra planets, Ad Astra's own suffocation damage takes over instead.)
                player.setAir(0);
                player.damage(world.getDamageSources().drown(), 1.0F);
            }
        }
    }

    /**
     * True when the player is breathing from a Gallifrey suit tank that still has oxygen.
     * Ad Astra compat uses this to skip Ad Astra's suffocation.
     */
    public static boolean isBreathingFromGallifreySuit(PlayerEntity player) {
        if (!hasFullSuit(player) || AdAstraCompat.hasFullAdAstraSuit(player)) return false;
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        NbtCompound nbt = chest.getNbt();
        return nbt == null || getOxygen(nbt) > 0;
    }

    private static int getOxygen(NbtCompound nbt) {
        return nbt.contains(OXYGEN_KEY) ? nbt.getInt(OXYGEN_KEY) : MAX_OXYGEN_TICKS;
    }

    private static boolean hasFullSuit(PlayerEntity p) {
        // The Gallifrey suit, or any Ad Astra suit (see SpaceSuits).
        return SpaceSuits.hasFullSuit(p);
    }

    private static boolean isMoonWorld(ServerWorld world) {
        Identifier id = world.getRegistryKey().getValue();
        return "gallifrey".equals(id.getNamespace()) && id.getPath().equals("moon");
    }

    private static boolean isVacuumWorld(ServerWorld world) {
        Identifier id = world.getRegistryKey().getValue();
        if (!"gallifrey".equals(id.getNamespace())) return false;
        return id.getPath().equals("mars") || id.getPath().equals("mondas") || id.getPath().equals("moon");
    }
}
