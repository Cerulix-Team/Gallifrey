package com.cerulixteam.gallifrey.item.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * White Point Star armour glows with a faint white light, for everyone, with no other mods needed.
 *
 * The light is a vanilla invisible light block that follows the wearer: it sits in the air at
 * head height (or in water, waterlogged), moves with them, and is removed as soon as they move
 * on, take the armour off, change dimension, log out, or the server stops. Only air and water
 * are ever replaced, and only light blocks this placed are ever removed.
 *
 * Brightness rises with the pieces worn: 4 with one piece up to 7 with the full set (a torch is
 * 14), and every second or so a single white sparkle drifts off the wearer.
 */
public final class WhitePointStarGlow {
    private WhitePointStarGlow() {}

    private static final int BASE_LIGHT = 3;           // + 1 per piece worn
    private static final int SPARKLE_INTERVAL = 20;    // ticks between sparkles
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private record Placed(RegistryKey<World> world, BlockPos pos, int level) {}

    private static final Map<UUID, Placed> PLACED = new HashMap<>();

    public static int piecesWorn(ServerPlayerEntity player) {
        int count = 0;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ModArmorMaterials.WHITE_POINT_STAR) {
                count++;
            }
        }
        return count;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WhitePointStarGlow::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> remove(server, handler.getPlayer().getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (UUID id : PLACED.keySet().toArray(new UUID[0])) remove(server, id);
        });
    }

    private static void tick(MinecraftServer server) {
        // Players who left without a disconnect event (shouldn't happen, but never leave a light behind).
        Iterator<Map.Entry<UUID, Placed>> it = PLACED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Placed> e = it.next();
            if (server.getPlayerManager().getPlayer(e.getKey()) == null) {
                clear(server, e.getValue());
                it.remove();
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            int pieces = player.isSpectator() || !player.isAlive() ? 0 : piecesWorn(player);
            if (pieces == 0) {
                remove(server, player.getUuid());
                continue;
            }
            ServerWorld world = player.getServerWorld();
            int level = BASE_LIGHT + pieces;

            Placed old = PLACED.get(player.getUuid());
            BlockPos target = BlockPos.ofFloored(player.getEyePos());
            if (!canHoldLight(world, target, old)) {
                target = player.getBlockPos();
                if (!canHoldLight(world, target, old)) target = null;
            }

            if (old != null && target != null && old.world() == world.getRegistryKey()
                    && old.pos().equals(target) && old.level() == level && isOurLight(world, target)) {
                // Still in place.
            } else {
                remove(server, player.getUuid());
                if (target != null && place(world, target, level)) {
                    PLACED.put(player.getUuid(), new Placed(world.getRegistryKey(), target.toImmutable(), level));
                }
            }

            if (player.age % SPARKLE_INTERVAL == 0) {
                world.spawnParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + player.getHeight() * 0.6, player.getZ(),
                        1, 0.3, 0.4, 0.3, 0.0);
            }
        }
    }

    /** Air, still water, or the light this player's armour already put there (never someone else's light block). */
    private static boolean canHoldLight(ServerWorld world, BlockPos pos, Placed old) {
        if (!world.isInBuildLimit(pos)) return false;
        BlockState state = world.getBlockState(pos);
        if (state.isAir() || isStillWater(state)) return true;
        return old != null && old.world() == world.getRegistryKey() && old.pos().equals(pos) && isOurLightState(state);
    }

    private static boolean isStillWater(BlockState state) {
        return state.isOf(Blocks.WATER) && state.getFluidState().isOf(Fluids.WATER) && state.getFluidState().isStill();
    }

    private static boolean isOurLightState(BlockState state) {
        return state.isOf(Blocks.LIGHT);
    }

    private static boolean isOurLight(ServerWorld world, BlockPos pos) {
        return isOurLightState(world.getBlockState(pos));
    }

    private static boolean place(ServerWorld world, BlockPos pos, int level) {
        BlockState current = world.getBlockState(pos);
        boolean water = isStillWater(current)
                || (current.isOf(Blocks.LIGHT) && current.get(Properties.WATERLOGGED));
        if (!current.isAir() && !water && !current.isOf(Blocks.LIGHT)) return false;
        return world.setBlockState(pos, Blocks.LIGHT.getDefaultState()
                .with(LightBlock.LEVEL_15, level)
                .with(Properties.WATERLOGGED, water));
    }

    private static void remove(MinecraftServer server, UUID player) {
        Placed placed = PLACED.remove(player);
        if (placed != null) clear(server, placed);
    }

    private static void clear(MinecraftServer server, Placed placed) {
        ServerWorld world = server.getWorld(placed.world());
        if (world == null) return;
        BlockState state = world.getBlockState(placed.pos());
        if (!state.isOf(Blocks.LIGHT)) return;   // something else is there now; leave it alone
        world.setBlockState(placed.pos(), state.get(Properties.WATERLOGGED)
                ? Blocks.WATER.getDefaultState()
                : Blocks.AIR.getDefaultState());
    }
}
