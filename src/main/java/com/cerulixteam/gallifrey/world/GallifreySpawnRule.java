package com.cerulixteam.gallifrey.world;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


public final class GallifreySpawnRule {
    private GallifreySpawnRule() {}

    public static final GameRules.Key<GameRules.BooleanRule> SPAWN_ON_GALLIFREY =
            GameRuleRegistry.register("spawnOnGallifrey", GameRules.Category.SPAWNING,
                    GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> SPAWN_ON_PETES_WORLD =
            GameRuleRegistry.register("spawnOnPetesWorld", GameRules.Category.SPAWNING,
                    GameRuleFactory.createBooleanRule(false));

    private static final int SEARCH_RADIUS = 256;
    private static final int SEARCH_STEP = 8;
    private static final Map<RegistryKey<World>, BlockPos> cachedSpawns = new HashMap<>();
    private static final Map<UUID, Integer> pendingMoves = new HashMap<>();
    private static final int JOIN_DELAY_TICKS = 20;     // 1 second
    private static final int RESPAWN_DELAY_TICKS = 10;  // half a second

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED
                .register(server -> {
                    cachedSpawns.clear();
                    pendingMoves.clear();
                });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK
                .register(GallifreySpawnRule::tickPendingMoves);

        // First join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            RegistryKey<World> target = spawnDimension(server);
            if (target == null) return;

            int playTime = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
            boolean firstJoin = playTime == 0 && player.getSpawnPointPosition() == null;
            if (!firstJoin) return;

            pendingMoves.put(player.getUuid(), JOIN_DELAY_TICKS);
        });

        // Respawn after death with no bed
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return; // returning from the End, not a death
            if (newPlayer.getServer() == null) return;
            RegistryKey<World> target = spawnDimension(newPlayer.getServer());
            if (target == null) return;
            if (newPlayer.getSpawnPointPosition() != null) return; // has a working bed/anchor/spawn

            pendingMoves.put(newPlayer.getUuid(), RESPAWN_DELAY_TICKS);
        });
    }

    private static void tickPendingMoves(MinecraftServer server) {
        if (pendingMoves.isEmpty()) return;
        Iterator<Map.Entry<UUID, Integer>> it = pendingMoves.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) { it.remove(); continue; } // left the game
            if (entry.getValue() > 0) { entry.setValue(entry.getValue() - 1); continue; }
            it.remove();
            RegistryKey<World> target = spawnDimension(server);
            if (target != null && player.isAlive()) sendTo(player, target, true);
        }
    }

    private static RegistryKey<World> spawnDimension(MinecraftServer server) {
        GameRules rules = server.getGameRules();
        if (rules.getBoolean(SPAWN_ON_GALLIFREY)) return ModDimensions.GALL_LEVEL_KEY;
        if (rules.getBoolean(SPAWN_ON_PETES_WORLD)) return ModDimensions.PETES_WORLD_LEVEL_KEY;
        return null;
    }

    private static void sendTo(ServerPlayerEntity player, RegistryKey<World> dimension, boolean setSpawn) {
        if (player.getServer() == null) return;
        ServerWorld world = player.getServer().getWorld(dimension);
        if (world == null) return;

        BlockPos pos = getSpawnPos(world);

        if (setSpawn) {
            // forced = true so it works without a bed; sendMessage = false
            player.setSpawnPoint(dimension, pos, 0.0F, true, false);
        }
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        player.fallDistance = 0;
    }

    private static BlockPos getSpawnPos(ServerWorld world) {
        BlockPos cached = cachedSpawns.get(world.getRegistryKey());
        if (cached != null) return cached;

        // Spiral outward from 0,0 looking for dry, solid ground
        for (int r = 0; r <= SEARCH_RADIUS; r += SEARCH_STEP) {
            for (int dx = -r; dx <= r; dx += SEARCH_STEP) {
                for (int dz = -r; dz <= r; dz += SEARCH_STEP) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // only the ring edge
                    BlockPos found = safeSurface(world, dx, dz);
                    if (found != null) {
                        cachedSpawns.put(world.getRegistryKey(), found);
                        return found;
                    }
                }
            }
        }

        // Fallback: top block at 0,0 even if it's wet
        int y = surfaceY(world, Heightmap.Type.MOTION_BLOCKING, 0, 0);
        BlockPos fallback = new BlockPos(0, y, 0);
        cachedSpawns.put(world.getRegistryKey(), fallback);
        return fallback;
    }

    private static int surfaceY(ServerWorld world, Heightmap.Type type, int x, int z) {
        Chunk chunk = world.getChunk(x >> 4, z >> 4);
        return chunk.sampleHeightmap(type, x & 15, z & 15) + 1;
    }

    private static BlockPos safeSurface(ServerWorld world, int x, int z) {
        int y = surfaceY(world, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= world.getBottomY() + 1) return null;

        BlockPos feet = new BlockPos(x, y, z);
        BlockPos ground = feet.down();
        BlockState groundState = world.getBlockState(ground);

        if (!groundState.getFluidState().isEmpty()) return null;            // water / lava
        if (!groundState.isSolidBlock(world, ground)) return null;          // must stand on something
        if (!world.getBlockState(feet).isAir()) return null;                // room for feet
        if (!world.getBlockState(feet.up()).isAir()) return null;           // room for head
        return feet;
    }
}