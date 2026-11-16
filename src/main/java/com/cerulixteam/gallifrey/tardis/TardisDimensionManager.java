package com.cerulixteam.gallifrey.tardis;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.TardisExteriorBlock;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.GallifreySounds;
import com.cerulixteam.gallifrey.networking.ModPackets;
import com.cerulixteam.gallifrey.mixin.MinecraftServerAccessor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.registry.Registry;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.UUID;

public final class TardisDimensionManager {
    /** Legacy shared interior retained solely so older saves can still be migrated. */
    public static final Identifier INTERIOR_DIMENSION_ID = new Identifier(GallifreyMod.MOD_ID, "tardis");
    private static final BlockPos DEFAULT_ENTRY_OFFSET = new BlockPos(0, 1, 0);

    private TardisDimensionManager() {}

    /** Each TARDIS receives its own persistent world key. */
    public static Identifier getDimensionId(UUID tardisId) {
        return new Identifier(GallifreyMod.MOD_ID, "tardis_" + tardisId.toString().replace("-", ""));
    }

    public static Identifier getDimensionId(long tardisId) {
        return new Identifier(GallifreyMod.MOD_ID, "tardis_" + Long.toUnsignedString(tardisId));
    }

    public static RegistryKey<World> interiorKey() {
        return RegistryKey.of(RegistryKeys.WORLD, INTERIOR_DIMENSION_ID);
    }

    public static boolean isInteriorWorld(net.minecraft.world.WorldView worldView) {
        return worldView instanceof World world && isInteriorWorld(world);
    }

    public static boolean isInteriorWorld(World world) {
        return isInteriorKey(world.getRegistryKey());
    }

    /** True for any TARDIS interior: the legacy shared gallifrey:tardis or a per-TARDIS gallifrey:tardis_<id>. */
    public static boolean isInteriorKey(RegistryKey<World> key) {
        if (key == null) return false;
        Identifier id = key.getValue();
        return id.getNamespace().equals(GallifreyMod.MOD_ID)
                && (id.getPath().equals("tardis") || id.getPath().startsWith("tardis_"));
    }

    public static ServerWorld getInterior(MinecraftServer server) {
        return server.getWorld(interiorKey());
    }

    public static ServerWorld getInterior(MinecraftServer server, TardisRegistryState.Record record) {
        if (record == null) return null;
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.interiorDimension())));
    }

    /** Creates and registers an isolated TARDIS world at runtime. */
    public static ServerWorld ensureInteriorWorld(MinecraftServer server, UUID tardisId) {
        Identifier id = getDimensionId(tardisId);
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        ServerWorld existing = server.getWorld(key);
        if (existing != null) return existing;

        try {
            MinecraftServerAccessor access = (MinecraftServerAccessor) server;
            Registry<DimensionOptions> dimensions = server.getRegistryManager().get(RegistryKeys.DIMENSION);
            DimensionOptions template = dimensions.get(INTERIOR_DIMENSION_ID);
            if (template == null) {
                GallifreyMod.LOGGER.error("Unable to create TARDIS world {}: missing dimension template {}", id, INTERIOR_DIMENSION_ID);
                return null;
            }

            SaveProperties saveProperties = server.getSaveProperties();
            LevelProperties properties = new LevelProperties(
                    saveProperties.getLevelInfo(),
                    saveProperties.getGeneratorOptions(),
                    LevelProperties.SpecialProperty.NONE,
                    saveProperties.getLifecycle());
            properties.setInitialized(true);
            ServerWorld world = new ServerWorld(
                    server,
                    access.gallifrey$getWorkerExecutor(),
                    access.gallifrey$getSession(),
                    properties,
                    key,
                    template,
                    new WorldGenerationProgressLogger(0),
                    false,
                    server.getOverworld().getSeed(),
                    java.util.List.of(),
                    false,
                    null);
            access.gallifrey$getWorlds().put(key, world);
            world.setSpawnPos(new BlockPos(0, 65, 0), 0.0f);
            GallifreyMod.LOGGER.info("Created isolated TARDIS dimension {}", id);
            return world;
        } catch (Throwable t) {
            GallifreyMod.LOGGER.error("Failed to create isolated TARDIS dimension {}", id, t);
            return null;
        }
    }

    /** Restores all per-TARDIS worlds after the server has loaded. */
    public static void restoreInteriorWorlds(MinecraftServer server) {
        for (TardisRegistryState.Record record : TardisRegistryState.get(server).records()) {
            if (!INTERIOR_DIMENSION_ID.toString().equals(record.interiorDimension())) {
                ensureInteriorWorld(server, record.id());
            }
        }
    }

    /**
     * Where you appear inside: just in front of the interior door, facing into the room, so you
     * don't walk straight back out (the door lets you out by walking into it).
     */
    public static Vec3d interiorEntry(TardisExteriorBlockEntity tardis) {
        BlockPos door = tardis.getInteriorDoorPos();
        if (door != null) {
            ServerWorld interior = TardisFlight.interiorOf(tardis);
            net.minecraft.util.math.Direction front = net.minecraft.util.math.Direction.SOUTH;
            if (interior != null && interior.getBlockState(door).contains(com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.FACING)) {
                front = com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.roomSide(interior.getBlockState(door));
            }
            return new Vec3d(door.getX() + 0.5 + front.getOffsetX() * 1.3, door.getY(), door.getZ() + 0.5 + front.getOffsetZ() * 1.3);
        }
        if ("70default".equalsIgnoreCase(tardis.getInteriorStructure())) {
            return new Vec3d(0.5, 65.0, 15.8);
        }
        BlockPos o = tardis.getInteriorOrigin();
        return new Vec3d(o.getX() + 0.5, o.getY() + 1.0, o.getZ() - 4.0);
    }

    /** Facing into the room from the interior door. */
    public static float interiorEntryYaw(TardisExteriorBlockEntity tardis) {
        BlockPos door = tardis.getInteriorDoorPos();
        ServerWorld interior = TardisFlight.interiorOf(tardis);
        if (door != null && interior != null && interior.getBlockState(door).contains(com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.FACING)) {
            return com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.roomSide(interior.getBlockState(door)).asRotation();
        }
        return tardis.getCachedState().get(TardisExteriorBlock.ROTATION) * 45.0f;
    }

    /** When each player last went through a TARDIS door (server ticks), so one step doesn't bounce them straight back. */
    private static final java.util.Map<UUID, Integer> LAST_DOOR_TRIP = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int DOOR_COOLDOWN_TICKS = 20;

    /** False for about a second after the player last went in or out through the doors. */
    public static boolean canUseDoors(ServerPlayerEntity player) {
        Integer last = LAST_DOOR_TRIP.get(player.getUuid());
        int now = player.getServer() == null ? 0 : player.getServer().getTicks();
        return last == null || now - last >= DOOR_COOLDOWN_TICKS || now < last;
    }

    private static void markDoorTrip(ServerPlayerEntity player) {
        if (player.getServer() != null) LAST_DOOR_TRIP.put(player.getUuid(), player.getServer().getTicks());
    }

    public static boolean enter(ServerPlayerEntity player, TardisExteriorBlockEntity tardis) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        if (!tardis.canAccess(player.getUuid())) {
            player.sendMessage(Text.literal("The TARDIS is locked."), true);
            return false;
        }
        if (tardis.isFlightPending()) return false;

        TardisRegistryState registry = TardisRegistryState.get(server);
        tardis.ensureInitialized(player);
        if (tardis.getTardisId() == null) return false;

        UUID activeId = tardis.getTardisId();
        TardisRegistryState.Record existingRecord = registry.get(activeId);
        ServerWorld interior = existingRecord == null
                ? ensureInteriorWorld(server, activeId)
                : getInterior(server, existingRecord);
        if (interior == null) {
            player.sendMessage(Text.literal("The TARDIS interior dimension is unavailable."), true);
            return false;
        }

        if (!tardis.generateInterior(interior)) {
            player.sendMessage(Text.literal("The TARDIS interior structure could not be loaded."), true);
            return false;
        }

        registry = TardisRegistryState.get(server);
        TardisRegistryState.Record record = registry.get(tardis.getTardisId());
        if (record == null) {
            String interiorDimension = getDimensionId(tardis.getTardisId()).toString();
            registry.register(tardis.getTardisId(), tardis.getOwner(), player.getServerWorld(),
                    tardis.getPos().asLong(), tardis.getInteriorOrigin().asLong(), tardis.isLocked(), interiorDimension);
            record = registry.get(tardis.getTardisId());
        }
        registry.setActive(player, tardis.getTardisId());
        // The doors may have been opened before the interior was first built: match the inside door to them.
        TardisFlight.setDoors(tardis, tardis.getCachedState().get(com.cerulixteam.gallifrey.block.TardisExteriorBlock.OPEN), false);

        sendInteriorDimensionKey(player, interior.getRegistryKey().getValue());

        ChunkPos chunk = new ChunkPos(tardis.getInteriorOrigin());
        interior.getChunkManager().addTicket(net.minecraft.server.world.ChunkTicketType.POST_TELEPORT, chunk, 2, player.getId());
        markDoorTrip(player);
        FabricDimensions.teleport(player, interior,
                new TeleportTarget(interiorEntry(tardis), Vec3d.ZERO, interiorEntryYaw(tardis), 0.0f));
        return true;
    }

    public static boolean exit(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) {
            player.sendMessage(Text.literal("You are not inside a registered TARDIS."), true);
            return false;
        }

        TardisRegistryState.Record record = registry.get(id);
        if (record == null) {
            registry.clearActive(player);
            player.sendMessage(Text.literal("The TARDIS link could not be restored."), true);
            return false;
        }

        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world()));
        ServerWorld world = server.getWorld(key);
        if (world == null) {
            player.sendMessage(Text.literal("The TARDIS exterior world is unavailable."), true);
            return false;
        }

        BlockPos pos = BlockPos.fromLong(record.pos());
        world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis)
                || !id.equals(tardis.getTardisId())) {
            player.sendMessage(Text.literal("The TARDIS exterior could not be located."), true);
            return false;
        }

        if (tardis.isFlightPending()) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.doors_in_flight"), true);
            return false;
        }

        registry.clearActive(player);
        // Step out in front of the doors, facing away from the box.
        int rotation = tardis.getCachedState().get(TardisExteriorBlock.ROTATION);
        Vec3d front = TardisFlight.frontVector(rotation);
        Vec3d out = Vec3d.ofBottomCenter(pos).add(front.multiply(1.25));
        float yaw = (float) Math.toDegrees(Math.atan2(-front.x, front.z));
        markDoorTrip(player);
        FabricDimensions.teleport(player, world, new TeleportTarget(out, Vec3d.ZERO, yaw, 0.0f));
        return true;
    }

    public static boolean swapInterior(ServerPlayerEntity pilot, String structureName) {
        MinecraftServer server = pilot.getServer();
        if (server == null) return false;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(pilot.getUuid());
        if (id == null) {
            pilot.sendMessage(Text.literal("Enter a TARDIS before changing its interior."), true);
            return false;
        }
        TardisRegistryState.Record record = registry.get(id);
        if (record == null) return false;
        ServerWorld interior = getInterior(server, record);
        if (interior == null) {
            interior = ensureInteriorWorld(server, id);
        }
        if (interior == null) return false;

        ServerWorld exteriorWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (exteriorWorld == null) return false;
        BlockPos exteriorPos = BlockPos.fromLong(record.pos());
        if (!(exteriorWorld.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis)) return false;
        if (!tardis.canPilot(pilot.getUuid())) {
            pilot.sendMessage(Text.literal("Only the TARDIS owner can change its interior."), true);
            return false;
        }
        if (tardis.isFlightPending()) {
            pilot.sendMessage(Text.literal("You cannot change the interior during flight."), true);
            return false;
        }
        if (TardisInteriorCatalog.id(structureName) == null) {
            pilot.sendMessage(Text.literal("Unknown interior. Use /tardis interiors to list the available designs."), true);
            return false;
        }

        // Changing the room invalidates the current interior layout. Safely eject
        // everybody first, including the pilot who requested the change.
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (registry.isInside(player, id) && player.getServerWorld() == interior) {
                closeConsole(player);
                exit(player);
            }
        }

        if (!tardis.replaceInterior(interior, structureName)) {
            pilot.sendMessage(Text.literal("That interior could not be loaded."), true);
            return false;
        }
        pilot.sendMessage(Text.literal("TARDIS interior changed to " + structureName + "."), true);
        interior.playSound(null, tardis.getInteriorOrigin(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1.0f, 1.2f);
        return true;
    }

    /**
     * The TARDIS whose interior this world is, or null. Used by controls placed inside the
     * interior (the throttle), which belong to that TARDIS rather than to whoever clicks them.
     */
    public static TardisExteriorBlockEntity findTardisForInterior(ServerWorld interior) {
        MinecraftServer server = interior.getServer();
        String key = interior.getRegistryKey().getValue().toString();
        for (TardisRegistryState.Record record : TardisRegistryState.get(server).records()) {
            if (!key.equals(record.interiorDimension())) continue;
            ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
            if (world == null) continue;
            BlockPos pos = BlockPos.fromLong(record.pos());
            world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis
                    && record.id().equals(tardis.getTardisId())) {
                return tardis;
            }
        }
        return null;
    }

    /**
     * Throttle: take off for the destination set on the monitor. Uses the normal flight
     * sequence (fuel, landing checks, dematerialise and rematerialise sounds).
     */
    public static boolean launch(ServerPlayerEntity pilot, TardisExteriorBlockEntity tardis) {
        return TardisFlight.takeOff(pilot, tardis);
    }

    /** /tardis travel and the old monitor path: set the destination and take off straight away. */
    public static boolean travel(ServerPlayerEntity pilot, ServerWorld targetWorld, BlockPos targetPos, float yaw) {
        MinecraftServer server = pilot.getServer();
        if (server == null) return false;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(pilot.getUuid());
        if (id == null) {
            pilot.sendMessage(Text.literal("Enter a TARDIS before initiating flight."), true);
            return false;
        }
        TardisRegistryState.Record record = registry.get(id);
        if (record == null) return false;
        ServerWorld currentWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (currentWorld == null) return false;
        BlockPos currentPos = BlockPos.fromLong(record.pos());
        currentWorld.getChunk(currentPos.getX() >> 4, currentPos.getZ() >> 4);
        if (!(currentWorld.getBlockEntity(currentPos) instanceof TardisExteriorBlockEntity tardis)) {
            pilot.sendMessage(Text.literal("TARDIS exterior is missing."), true);
            return false;
        }
        tardis.setDestination(targetWorld.getRegistryKey().getValue(), targetPos);
        tardis.setDestRotation(rotationFromYaw(yaw));
        tardis.setHandbrake(false);
        return TardisFlight.takeOff(pilot, tardis);
    }

    public static boolean toggleRealWorldFlight(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) { player.sendMessage(Text.literal("Enter or link to your TARDIS first."), true); return false; }
        TardisRegistryState.Record record = registry.get(id);
        if (record == null || record.owner() == null || !record.owner().equals(player.getUuid())) {
            player.sendMessage(Text.literal("Only the TARDIS owner can use Real World Flight."), true); return false;
        }
        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (world == null) return false;
        BlockPos pos = BlockPos.fromLong(record.pos());
        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis)) return false;
        if (tardis.isFlightPending()) { player.sendMessage(Text.literal("Wait for the current flight to finish."), true); return false; }
        if (tardis.isRealWorldFlight()) {
            endRealWorldFlight(server, registry, tardis, world, pos, "Real World Flight disengaged.");
            player.sendMessage(Text.literal("RWF disengaged."), true);
            sendRwfState(player, false);
            return true;
        }
        if (!tardis.isPowered()) { player.sendMessage(Text.literal("The TARDIS has no power."), true); return false; }
        if (!tardis.isAntigravityEnabled()) { player.sendMessage(Text.literal("Enable antigravity before entering Real World Flight."), true); return false; }
        tardis.setRealWorldFlight(true);
        world.playSound(null, pos, GallifreySounds.TYPE70FLIGHT, SoundCategory.BLOCKS, 1.0f, 1.0f);
        player.teleport(world, pos.getX() + 0.5, pos.getY() + 2.6, pos.getZ() + 0.5, java.util.Set.of(), player.getYaw(), 0.0f);
        player.sendMessage(Text.literal("REAL WORLD FLIGHT engaged. Arrow keys to fly, Space up, Shift down, E/Q to change speed. /tardis rwf to exit."), true);
        sendRwfState(player, true);
        return true;
    }

    public static void handleRwfInput(ServerPlayerEntity player, float forward, float strafe, float vertical, float speed, float yaw, float pitch) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) return;
        TardisRegistryState.Record record = registry.get(id);
        if (record == null || record.owner() == null || !record.owner().equals(player.getUuid())) return;
        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (world == null) return;
        BlockPos pos = BlockPos.fromLong(record.pos());
        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) || !tardis.isRealWorldFlight()) return;
        speed = net.minecraft.util.math.MathHelper.clamp(speed, 1.0f, 4.0f);
        if (!tardis.isPowered() || !tardis.isAntigravityEnabled()) { endRealWorldFlight(server, registry, tardis, world, pos, "RWF ended: power or antigravity unavailable."); sendRwfState(player, false); return; }
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d flatForward = new Vec3d(look.x, 0, look.z);
        if (flatForward.lengthSquared() < 1.0E-5) flatForward = new Vec3d(0,0,1); else flatForward = flatForward.normalize();
        Vec3d right = new Vec3d(-flatForward.z, 0, flatForward.x);
        Vec3d delta = look.multiply(forward * speed).add(right.multiply(strafe * speed)).add(0, vertical * speed, 0);
        if (delta.lengthSquared() > 1.0E-5) {
            moveTardisFree(server, registry, world, pos, tardis, delta);
            tardis.consumeFuel(1);
        }
        TardisRegistryState.Record updated = registry.get(id);
        BlockPos updatedPos = updated == null ? pos : BlockPos.fromLong(updated.pos());
        player.teleport(world, updatedPos.getX()+0.5, updatedPos.getY()+2.6, updatedPos.getZ()+0.5, java.util.Set.of(), yaw, Math.max(-89, Math.min(89, pitch)));
    }

    private static boolean isRwfFootprintClear(ServerWorld world, BlockPos anchor) {
        // Treat the exterior as a 3x3x2 physical shell while it is in flight.
        // This prevents the block entity from clipping into walls or ceilings.
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos p = anchor.add(x, y, z);
                    if (!world.getBlockState(p).getCollisionShape(world, p).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    private static void moveTardisFree(MinecraftServer server, TardisRegistryState registry, ServerWorld world, BlockPos pos, TardisExteriorBlockEntity tardis, Vec3d delta) {
        int dx = (int) Math.round(delta.x);
        int dy = (int) Math.round(delta.y);
        int dz = (int) Math.round(delta.z);
        if (dx == 0 && dy == 0 && dz == 0) return;

        // RWF moves the physical shell in whole-block increments. Walk the path one
        // block at a time so higher speeds cannot tunnel through terrain.
        int steps = Math.max(1, Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))));
        int sx = Integer.signum(dx), sy = Integer.signum(dy), sz = Integer.signum(dz);
        BlockPos current = pos;
        for (int i = 0; i < steps; i++) {
            BlockPos next = current.add(sx, sy, sz);
            if (next.getY() <= world.getBottomY() || next.getY() >= world.getTopY() - 2) return;
            if (!isRwfFootprintClear(world, next)) return;
            current = next;
        }

        BlockPos target = current;
        NbtCompound nbt = tardis.createNbt();
        BlockState state = tardis.getCachedState();
        world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
        world.setBlockState(target, state, 3);
        if (world.getBlockEntity(target) instanceof TardisExteriorBlockEntity moved) {
            nbt.remove("x"); nbt.remove("y"); nbt.remove("z"); nbt.remove("id");
            moved.readNbt(nbt);
            moved.markDirty();
            registry.updateLocation(moved.getTardisId(), world, target.asLong());
        }
    }

    private static void endRealWorldFlight(MinecraftServer server, TardisRegistryState registry, TardisExteriorBlockEntity tardis, ServerWorld world, BlockPos pos, String message) {
        tardis.setRealWorldFlight(false);
        world.playSound(null, pos, GallifreySounds.TYPE70DEMAT, SoundCategory.BLOCKS, 0.8f, 1.0f);
        if (message != null) {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) if (p.getUuid().equals(tardis.getOwner())) p.sendMessage(Text.literal(message), true);
        }
    }

    public static void sendRwfState(ServerPlayerEntity player, boolean active) {
        net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeBoolean(active); ServerPlayNetworking.send(player, ModPackets.TARDIS_RWF_STATE, buf);
    }

    public static void tickFlight(MinecraftServer server) {
        TardisRegistryState registry = TardisRegistryState.get(server);
        tickInteriorSafety(server, registry);
        for (TardisRegistryState.Record record : new java.util.ArrayList<>(registryRecords(registry))) {
            ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
            if (world == null) continue;
            BlockPos pos = BlockPos.fromLong(record.pos());
            world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof TardisExteriorBlockEntity tardis) {
                TardisRegistryState.Record currentRecord = registry.get(tardis.getTardisId());
                if (currentRecord != null) {
                    ServerWorld interior = getInterior(server, currentRecord);
                    if (interior != null) tardis.syncInteriorPower(interior);
                }
                if (tardis.isSelfDestructArmed()) {
                    int countdownBefore = tardis.getSelfDestructTicks();
                    if (countdownBefore % 20 == 0) {
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            if (registry.isInside(player, tardis.getTardisId())) TardisMonitorNetworking.sendState(player);
                        }
                    }
                    if (tardis.tickSelfDestruct()) {
                        selfDestruct(server, registry, world, pos, tardis);
                        continue;
                    }
                }
                if (tardis.isRealWorldFlight()) {
                    if (!tardis.isPowered() || !tardis.isAntigravityEnabled()) {
                        endRealWorldFlight(server, registry, tardis, world, pos, "RWF ended: power or antigravity unavailable.");
                        continue;
                    }
                    continue;
                } else if (tardis.isFlightPending()) {
                    TardisFlight.tick(server, registry, world, pos, tardis);
                } else {
                    TardisFlight.tick(server, registry, world, pos, tardis);
                    if (!tardis.isAntigravityEnabled() && !isSupported(world, pos) && world.getBlockEntity(pos) == tardis) {
                    // Antigravity OFF means the physical shell obeys gravity.
                    // Drop one block per server tick until its base is supported.
                    moveTardisVertically(server, registry, world, pos, tardis, -1);
                    }
                }
            }
        }
    }

    /** Keeps players from falling out of their individual TARDIS pocket dimension. */
    private static void tickInteriorSafety(MinecraftServer server, TardisRegistryState registry) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID id = registry.getActiveTardis(player.getUuid());
            if (id == null) continue;
            TardisRegistryState.Record record = registry.get(id);
            if (record == null) continue;
            ServerWorld interior = getInterior(server, record);
            if (interior == null || player.getServerWorld() != interior) continue;

            BlockPos origin = BlockPos.fromLong(record.origin());
            // Interior templates are built around Y=64. If a player falls more
            // than eight blocks below their TARDIS floor, return them above the
            // physical console rather than letting them reach the world void.
            if (player.getY() >= origin.getY() - 8) continue;
            // Just came through the doors: the position can still read as where they were outside for a tick.
            if (!canUseDoors(player)) continue;

            BlockPos door = tardisInteriorDoorPosition(interior, record, server);
            Vec3d target = new Vec3d(origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() - 4.0);
            float yaw = player.getYaw();
            if (door != null) {
                // In front of the door, facing into the room (not on the door itself, or they'd walk straight out).
                net.minecraft.util.math.Direction room = interior.getBlockState(door)
                        .contains(com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.FACING)
                        ? com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.roomSide(interior.getBlockState(door))
                        : net.minecraft.util.math.Direction.SOUTH;
                target = new Vec3d(door.getX() + 0.5 + room.getOffsetX() * 1.3, door.getY(), door.getZ() + 0.5 + room.getOffsetZ() * 1.3);
                yaw = room.asRotation();
            }
            markDoorTrip(player);
            player.teleport(interior, target.x, target.y, target.z, yaw, player.getPitch());
            player.setVelocity(Vec3d.ZERO);
            player.sendMessage(Text.literal("The TARDIS catches you and returns you to its door."), true);
        }
    }


    /** Finds the registered interior door, falling back to the stored position. */
    private static BlockPos tardisInteriorDoorPosition(ServerWorld interior, TardisRegistryState.Record record, MinecraftServer server) {
        ServerWorld exterior = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (exterior != null) {
            BlockPos exteriorPos = BlockPos.fromLong(record.pos());
            if (exterior.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis) {
                BlockPos stored = tardis.getInteriorDoorPos();
                if (stored != null && interior.getBlockState(stored).isOf(GallifreyModBlocks.TARDIS_INTERIOR_DOOR)) {
                    return stored;
                }
            }
        }
        BlockPos origin = BlockPos.fromLong(record.origin());
        for (int x = origin.getX() - 8; x <= origin.getX() + 8; x++) {
            for (int y = origin.getY(); y <= origin.getY() + 8; y++) {
                for (int z = origin.getZ() - 8; z <= origin.getZ() + 8; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (interior.getBlockState(candidate).isOf(GallifreyModBlocks.TARDIS_INTERIOR_DOOR)) return candidate;
                }
            }
        }
        return null;
    }

    /** Finds the registered console, falling back to a modest search only when needed. */
    private static BlockPos tardisConsolePosition(ServerWorld interior, BlockPos origin, TardisRegistryState.Record record, MinecraftServer server) {
        ServerWorld exterior = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (exterior != null) {
            BlockPos exteriorPos = BlockPos.fromLong(record.pos());
            if (exterior.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis) {
                BlockPos stored = tardis.getConsolePos();
                if (stored != null && interior.getBlockState(stored).isOf(GallifreyModBlocks.TARDIS_CONSOLE)) {
                    return stored;
                }
            }
        }

        // The console may have been broken in an older save before consolePos
        // existed. Search only around the normal central console area.
        for (int x = origin.getX() - 12; x <= origin.getX() + 12; x++) {
            for (int y = Math.max(interior.getBottomY(), origin.getY() - 2); y <= origin.getY() + 8; y++) {
                for (int z = origin.getZ() - 12; z <= origin.getZ() + 12; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (interior.getBlockState(pos).isOf(GallifreyModBlocks.TARDIS_CONSOLE)) return pos;
                }
            }
        }
        return null;
    }

    /** Permanently removes a TARDIS and its pocket interior. */
    public static boolean deleteTardis(MinecraftServer server, UUID id) {
        TardisRegistryState registry = TardisRegistryState.get(server);
        TardisRegistryState.Record record = registry.get(id);
        if (record == null) return false;

        ServerWorld exteriorWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        BlockPos exteriorPos = BlockPos.fromLong(record.pos());

        // Eject linked players before deleting the record so exit() can still
        // resolve the physical exterior.
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (registry.isInside(player, id)) {
                closeConsole(player);
                if (exteriorWorld != null) {
                    player.teleport(exteriorWorld, exteriorPos.getX() + 0.5, exteriorPos.getY() + 0.15,
                            exteriorPos.getZ() + 0.5, player.getYaw(), player.getPitch());
                }
                registry.clearActive(player);
            }
        }

        ServerWorld interior = getInterior(server, record);
        if (interior != null && !INTERIOR_DIMENSION_ID.toString().equals(record.interiorDimension())) {
            try { interior.close(); } catch (Exception ignored) {}
            ((MinecraftServerAccessor) server).gallifrey$getWorlds().remove(interior.getRegistryKey());
        } else if (interior != null) {
            clearDeletedInterior(interior, BlockPos.fromLong(record.origin()));
        }

        if (exteriorWorld != null) {
            exteriorWorld.getChunk(exteriorPos.getX() >> 4, exteriorPos.getZ() >> 4);
            if (exteriorWorld.getBlockState(exteriorPos).isOf(GallifreyModBlocks.TARDIS_EXTERIOR)) {
                exteriorWorld.setBlockState(exteriorPos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
            }
        }
        registry.remove(id);
        return true;
    }

    private static void clearDeletedInterior(ServerWorld world, BlockPos origin) {
        // Current interiors occupy a modest footprint. A 96x32x96 cleanup box
        // also removes layouts whose saved footprint was larger in older builds.
        for (int x = -48; x <= 48; x++) {
            for (int y = -2; y <= 32; y++) {
                for (int z = -48; z <= 48; z++) {
                    world.setBlockState(origin.add(x, y, z), net.minecraft.block.Blocks.AIR.getDefaultState(), 2);
                }
            }
        }
    }

    public static void sendInteriorDimensionKey(ServerPlayerEntity player, Identifier id) {
        net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeIdentifier(id);
        ServerPlayNetworking.send(player, ModPackets.TARDIS_REGISTER_DIMENSION, buf);
    }

    public static void closeConsole(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, ModPackets.TARDIS_CLOSE_CONSOLE, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create());
    }

    private static java.util.Collection<TardisRegistryState.Record> registryRecords(TardisRegistryState state) {
        return state.records();
    }

    private static boolean isSupported(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSolidBlock(world, pos.down());
    }

    /** Moves the physical TARDIS while preserving its block entity state and registry location. */
    private static BlockPos moveTardisVertically(MinecraftServer server, TardisRegistryState registry, ServerWorld world, BlockPos pos, TardisExteriorBlockEntity tardis, int deltaY) {
        BlockPos target = pos.add(0, deltaY, 0);
        if (target.getY() <= world.getBottomY() || target.getY() >= world.getTopY() - 2) return null;
        if (!world.getBlockState(target).isAir() || !world.getBlockState(target.up()).isAir()) return null;
        NbtCompound nbt = tardis.createNbt();
        BlockState state = tardis.getCachedState();
        world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
        world.setBlockState(target, state, 3);
        if (world.getBlockEntity(target) instanceof TardisExteriorBlockEntity moved) {
            nbt.remove("x"); nbt.remove("y"); nbt.remove("z"); nbt.remove("id");
            moved.readNbt(nbt);
            moved.markDirty();
            registry.updateLocation(moved.getTardisId(), world, target.asLong());
            // If the pilot is standing on the shell while antigravity is disabled,
            // carry them with the TARDIS instead of leaving them suspended in mid-air.
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getServerWorld() != world || !moved.canPilot(player.getUuid())) continue;
                double dx = player.getX() - (pos.getX() + 0.5);
                double dz = player.getZ() - (pos.getZ() + 0.5);
                double relativeY = player.getY() - (pos.getY() + 2.0);
                if (dx * dx + dz * dz <= 2.25 && relativeY >= -0.5 && relativeY <= 2.0) {
                    player.teleport(world, player.getX(), player.getY() + deltaY, player.getZ(), java.util.Set.of(), player.getYaw(), player.getPitch());
                }
            }
            return target;
        }
        return null;
    }

    private static void selfDestruct(MinecraftServer server, TardisRegistryState registry, ServerWorld world, BlockPos pos, TardisExteriorBlockEntity tardis) {
        UUID id = tardis.getTardisId();
        world.playSound(null, pos, GallifreySounds.TYPE70DEMAT, SoundCategory.BLOCKS, 1.5F, 0.65F);
        // The completed countdown always produces a real world explosion at
        // the shell, then the registry/interior are removed.
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 4.0F, false, World.ExplosionSourceType.TNT);
        world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
        if (id != null) deleteTardis(server, id);
    }

    public static int rotationFromYaw(float yaw) {
        return net.minecraft.util.math.MathHelper.floor((double)((yaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7;
    }

    /** Refuels from Atrium Fuel items in the pilot inventory and returns energy added. */
    public static int refuel(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) return 0;
        TardisRegistryState.Record record = registry.get(id);
        if (record == null || record.owner() == null || !record.owner().equals(player.getUuid())) return 0;
        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (world == null) return 0;
        BlockPos pos = BlockPos.fromLong(record.pos());
        if (!(world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis)) return 0;

        int added = 0;
        for (int slot = 0; slot < player.getInventory().size()
                && tardis.getFuel() < TardisExteriorBlockEntity.MAX_FUEL; slot++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isOf(com.cerulixteam.gallifrey.item.GallifreyModItems.ATRIUM_FUEL)) continue;
            stack.decrement(1);
            added += tardis.addFuel(100);
        }
        return added;
    }

    public static void refuel(ServerPlayerEntity player, int amount) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        TardisRegistryState registry = TardisRegistryState.get(server);
        UUID id = registry.getActiveTardis(player.getUuid());
        if (id == null) return;
        TardisRegistryState.Record record = registry.get(id);
        if (record == null) return;
        ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(record.world())));
        if (world == null) return;
        BlockPos pos = BlockPos.fromLong(record.pos());
        if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) {
            int added = tardis.addFuel(amount);
            player.sendMessage(Text.literal("Artron reserves: " + tardis.getFuel() + "/" + TardisExteriorBlockEntity.MAX_FUEL +
                    " (+" + added + ")"), true);
        }
    }
}
