package com.cerulixteam.gallifrey.tardis;

import com.cerulixteam.gallifrey.GallifreySounds;
import com.cerulixteam.gallifrey.block.TardisExteriorBlock;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity.FlightPhase;
import com.cerulixteam.gallifrey.block.entity.TardisInteriorDoorBlockEntity;
import com.cerulixteam.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TARDIS flight, Dalek Mod style to fly and like the show to look at.
 *
 * Set the destination with the panels (coordinate panel, dimension selector, fast return, randomiser) or
 * the monitor, make sure the handbrake is off, and pull the flight lever.
 *
 *  1. DEMAT: the take-off sound plays and the box fades away in pulses, in time with the wheeze, its lamp
 *     flashing. Inside, the room shakes as it lifts off.
 *  2. VORTEX: the box is gone. Inside, the flight hum plays and the room gently rocks. The trip takes longer
 *     the further you go (and a little longer between dimensions). When you've arrived you're told so, and
 *     the TARDIS waits in the vortex until you pull the flight lever again. You can still change the
 *     destination in flight; it lands wherever is set when you pull the lever.
 *  3. REMAT: the box fades back in at the destination with the landing sound, then lands with a jolt.
 *
 * While the box is mostly faded it has no collision and can't be used, so you can walk through it.
 */
public final class TardisFlight {
    private TardisFlight() {}

    /** Flight hum length (type70flight.ogg), so it can loop in the vortex. */
    private static final int HUM_TICKS = 280;

    // ------------------------------------------------------------------ take off

    /** Coordinate step sizes the increment dial turns through. */
    public static final int[] INCREMENTS = {1, 10, 100, 1000, 10000};

    /**
     * The flight lever (and the throttle): take off when landed; once you've arrived, land. Otherwise
     * says what the TARDIS is doing.
     */
    public static boolean pullFlightLever(ServerPlayerEntity pilot, TardisExteriorBlockEntity tardis) {
        switch (tardis.getPhase()) {
            case LANDED -> {
                return takeOff(pilot, tardis);
            }
            case DEMAT -> pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.dematerialising"), true);
            case REMAT -> pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.materialising"), true);
            case VORTEX -> {
                if (!tardis.canPilot(pilot.getUuid())) {
                    pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.not_pilot"), true);
                } else if (tardis.getPhaseAge() < tardis.getVortexTicks()) {
                    pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.in_flight", tardis.secondsToArrival()), true);
                } else if (tardis.isHandbrakeOn()) {
                    pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.handbrake_land"), true);
                } else if (tardis.getWorld() instanceof ServerWorld world && world.getServer() != null) {
                    return rematerialise(pilot, world.getServer(), TardisRegistryState.get(world.getServer()), world,
                            tardis.getPos(), tardis);
                }
            }
        }
        return false;
    }

    /** Take off for the destination. Says why not if it can't. */
    public static boolean takeOff(ServerPlayerEntity pilot, TardisExteriorBlockEntity tardis) {
        if (tardis.isFlightPending()) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.in_flight", tardis.secondsToArrival()), true);
            return false;
        }
        if (tardis.isRealWorldFlight()) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.rwf_active"), true);
            return false;
        }
        if (tardis.isHandbrakeOn()) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.handbrake_first"), true);
            return false;
        }
        if (!tardis.canPilot(pilot.getUuid())) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.not_pilot"), true);
            return false;
        }
        MinecraftServer server = pilot.getServer();
        World here = tardis.getWorld();
        if (server == null || !(here instanceof ServerWorld currentWorld)) return false;
        ensureDestination(tardis);

        ServerWorld target = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, tardis.getDestinationWorld()));
        if (target == null || TardisDimensionManager.isInteriorWorld(target)) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.bad_dimension"), true);
            return false;
        }
        BlockPos requested = tardis.getDestinationPos();
        if (requested.getY() < target.getBottomY() || requested.getY() > target.getTopY() - 2) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.build_height"), true);
            return false;
        }
        BlockPos current = tardis.getPos();
        boolean sameWorld = target == currentWorld;
        if (sameWorld && requested.getX() == current.getX() && requested.getZ() == current.getZ()
                && Math.abs(requested.getY() - current.getY()) <= 1) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.already_there"), true);
            return false;
        }

        double distance = sameWorld ? Math.sqrt(requested.getSquaredDistance(current)) : 0;
        int cost = TardisExteriorBlockEntity.FLIGHT_COST + (int) Math.min(200, distance / 250) + (sameWorld ? 0 : 25);
        if (tardis.getFuel() < cost) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.no_fuel", tardis.getFuel(), cost), true);
            return false;
        }
        target.getChunk(requested.getX() >> 4, requested.getZ() >> 4);
        if (findLanding(target, requested, tardis.isLandDown()) == null) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.no_landing"), true);
            return false;
        }

        // In the vortex for longer the further you go: 4 s next door, ~12 s for 1000 blocks,
        // ~29 s for 10000 blocks, plus 6 s to cross between dimensions.
        int vortexTicks = (int) (20 * (4 + Math.min(36, Math.sqrt(distance) / 4) + (sameWorld ? 0 : 6)));
        int rotation = tardis.getDestRotation() >= 0 ? tardis.getDestRotation()
                : tardis.getCachedState().get(TardisExteriorBlock.ROTATION);

        tardis.consumeFuel(cost);
        tardis.setLastLocation(currentWorld.getRegistryKey().getValue(), current,
                tardis.getCachedState().get(TardisExteriorBlock.ROTATION));
        setDoors(tardis, false, false);
        tardis.beginDemat(target.getRegistryKey().getValue(), requested, rotation, vortexTicks);

        playOutside(currentWorld, current, GallifreySounds.TYPE70DEMAT, 2.0F);
        playInside(tardis, GallifreySounds.TYPE70DEMAT, 1.0F);
        shakeInside(tardis, 1.0F, 50);
        pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.taking_off"), true);
        return true;
    }

    /** If no destination is set yet, start from where the TARDIS is now (so X/Y/Z adjust from here). */
    public static void ensureDestination(TardisExteriorBlockEntity tardis) {
        if (!tardis.hasDestination() && tardis.getWorld() != null) {
            tardis.setDestination(tardis.getWorld().getRegistryKey().getValue(), tardis.getPos());
        }
    }

    // ------------------------------------------------------------------ ticking

    /** Called every server tick for every TARDIS (from TardisDimensionManager.tickFlight). */
    public static void tick(MinecraftServer server, TardisRegistryState registry, ServerWorld world, BlockPos pos,
                            TardisExteriorBlockEntity tardis) {
        int age = tardis.getPhaseAge();
        switch (tardis.getPhase()) {
            case DEMAT -> {
                if (age % 20 == 0 && age > 40) shakeInside(tardis, 0.25F, 25);
                if (age >= TardisPhaseCurves.DEMAT_TICKS) {
                    tardis.setPhase(FlightPhase.VORTEX);
                    playInside(tardis, GallifreySounds.TYPE70FLIGHT, 0.8F);
                    refreshMonitors(server, registry, tardis);
                }
            }
            case VORTEX -> {
                // Arrived: say so (and remind every 30 s); the TARDIS waits until the flight lever is pulled.
                int waiting = age - tardis.getVortexTicks();
                if (waiting >= 0 && waiting % 600 == 0) {
                    for (ServerPlayerEntity player : insidePlayers(tardis)) {
                        player.sendMessage(Text.translatable("tardis.gallifrey.msg.arrived"), true);
                        player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 0.8F, 1.2F);
                    }
                }
                if (age > 0 && age % HUM_TICKS == 0) playInside(tardis, GallifreySounds.TYPE70FLIGHT, 0.8F);
                if (age % 30 == 0) shakeInside(tardis, waiting >= 0 ? 0.08F : 0.15F, 30);
            }
            case REMAT -> {
                if (age == TardisPhaseCurves.REMAT_TICKS - 30) clearFootprint(world, pos, tardis);
                if (age % 20 == 0 && age < TardisPhaseCurves.REMAT_TICKS - 40) shakeInside(tardis, 0.2F, 25);
                if (age >= TardisPhaseCurves.REMAT_TICKS) land(server, registry, world, pos, tardis);
            }
            case LANDED -> {
                if (age % 20 == 0) TardisExteriorBlock.ensureTop(world, pos);
                if (tardis.isRefueling() && age % 20 == 0 && tardis.getFuel() < TardisExteriorBlockEntity.MAX_FUEL) {
                    tardis.addFuel(2);
                }
            }
        }
    }

    /**
     * Landing (the flight lever, once arrived): move the still-invisible box to the destination that's set
     * now and start fading it in. Says why not and stays in the vortex if it can't land there.
     */
    private static boolean rematerialise(ServerPlayerEntity pilot, MinecraftServer server, TardisRegistryState registry,
                                         ServerWorld source, BlockPos sourcePos, TardisExteriorBlockEntity tardis) {
        ServerWorld target = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, tardis.getDestinationWorld()));
        if (target == null || TardisDimensionManager.isInteriorWorld(target)) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.bad_dimension"), true);
            return false;
        }
        BlockPos landing = findLanding(target, tardis.getDestinationPos(), tardis.isLandDown());
        if (landing == null) {
            pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.no_landing"), true);
            return false;
        }
        int rotation = tardis.getDestRotation() >= 0 ? tardis.getDestRotation() : tardis.getFlightRotation();
        TardisExteriorBlockEntity moved = tardis;
        if (target != source || !landing.equals(sourcePos)) {
            BlockState state = tardis.getCachedState()
                    .with(TardisExteriorBlock.ROTATION, rotation)
                    .with(TardisExteriorBlock.OPEN, false);
            NbtCompound nbt = tardis.createNbt();
            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");
            nbt.remove("id");
            source.removeBlockEntity(sourcePos);
            source.setBlockState(sourcePos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
            target.setBlockState(landing, state, 3);
            if (!(target.getBlockEntity(landing) instanceof TardisExteriorBlockEntity placed)) return false;
            placed.readNbt(nbt);
            moved = placed;
            registry.updateLocation(placed.getTardisId(), target, landing.asLong());
        } else {
            target.setBlockState(landing, tardis.getCachedState().with(TardisExteriorBlock.ROTATION, rotation), 3);
        }
        moved.setPhase(FlightPhase.REMAT);
        playOutside(target, landing, GallifreySounds.TYPE70MAT, 2.0F);
        playInside(moved, GallifreySounds.TYPE70MAT, 1.0F);
        shakeInside(moved, 0.6F, 30);
        pilot.sendMessage(Text.translatable("tardis.gallifrey.msg.materialising"), true);
        refreshMonitors(server, registry, moved);
        return true;
    }

    /** Fully solid again: land with a jolt. */
    private static void land(MinecraftServer server, TardisRegistryState registry, ServerWorld world, BlockPos pos,
                             TardisExteriorBlockEntity tardis) {
        tardis.cancelFlight();
        tardis.setDestination(world.getRegistryKey().getValue(), pos);
        shakeInside(tardis, 1.2F, 12);
        for (ServerPlayerEntity player : insidePlayers(tardis)) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.landed",
                    world.getRegistryKey().getValue().toString(), pos.getX(), pos.getY(), pos.getZ()), true);
        }
        refreshMonitors(server, registry, tardis);
    }

    /** Anything standing where the box is appearing gets nudged out of the way before it turns solid. */
    private static void clearFootprint(ServerWorld world, BlockPos pos, TardisExteriorBlockEntity tardis) {
        Box box = new Box(pos).stretch(0, 1, 0);
        Vec3d front = frontVector(tardis.getCachedState().get(TardisExteriorBlock.ROTATION));
        for (Entity entity : world.getOtherEntities(null, box, e -> e instanceof LivingEntity)) {
            Vec3d out = Vec3d.ofBottomCenter(pos).add(front.multiply(1.3));
            entity.requestTeleport(out.x, pos.getY(), out.z);
        }
    }

    // ------------------------------------------------------------------ doors

    /** Opens or closes both the exterior doors and the interior door. Only while landed. */
    public static boolean setDoors(TardisExteriorBlockEntity tardis, boolean open, boolean withSound) {
        World world = tardis.getWorld();
        if (world == null) return false;
        if (open && tardis.isFlightPending()) return false;
        BlockState state = tardis.getCachedState();
        if (state.get(TardisExteriorBlock.OPEN) != open) {
            world.setBlockState(tardis.getPos(), state.with(TardisExteriorBlock.OPEN, open), 3);
            if (withSound) {
                world.playSound(null, tardis.getPos(), open ? GallifreySounds.POLICE_BOX_DOOR_OPEN
                        : GallifreySounds.POLICE_BOX_DOOR_CLOSE, SoundCategory.BLOCKS, 0.7F, 1.0F);
            }
        }
        ServerWorld interior = interiorOf(tardis);
        BlockPos doorPos = tardis.getInteriorDoorPos();
        if (interior != null && doorPos != null && interior.getBlockEntity(doorPos) instanceof TardisInteriorDoorBlockEntity door
                && door.isOpen() != open) {
            door.setOpen(open);
            if (withSound) {
                interior.playSound(null, doorPos, open ? GallifreySounds.POLICE_BOX_DOOR_OPEN
                        : GallifreySounds.POLICE_BOX_DOOR_CLOSE, SoundCategory.BLOCKS, 0.7F, 1.0F);
            }
        }
        return true;
    }

    /** Which way the exterior's doors face, for a rotation value (0-7). */
    public static Vec3d frontVector(int rotation) {
        double angle = Math.toRadians(rotation * 45.0);
        return new Vec3d(-Math.sin(angle), 0, Math.cos(angle));
    }

    // ------------------------------------------------------------------ the panels

    /** A panel button was pressed. Sneaking turns the increment and rotation dials the other way. */
    public static void usePanel(ServerPlayerEntity player, ServerWorld interior, TardisPanelAction action, boolean sneaking) {
        TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(interior);
        if (tardis == null) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.not_connected"), true);
            return;
        }
        if (!tardis.canPilot(player.getUuid())) {
            player.sendMessage(Text.translatable("tardis.gallifrey.msg.not_pilot"), true);
            return;
        }
        int direction = sneaking ? -1 : 1;
        switch (action) {
            case FLIGHT -> pullFlightLever(player, tardis);
            case HANDBRAKE -> {
                tardis.setHandbrake(!tardis.isHandbrakeOn());
                player.sendMessage(Text.translatable(tardis.isHandbrakeOn()
                        ? "tardis.gallifrey.msg.handbrake_on" : "tardis.gallifrey.msg.handbrake_off"), true);
            }
            case DOORS -> {
                boolean open = !tardis.getCachedState().get(TardisExteriorBlock.OPEN);
                if (open && tardis.isFlightPending()) {
                    player.sendMessage(Text.translatable("tardis.gallifrey.msg.doors_in_flight"), true);
                } else if (open && tardis.isLocked()) {
                    player.sendMessage(Text.translatable("tardis.gallifrey.msg.doors_locked"), true);
                } else {
                    setDoors(tardis, open, true);
                    player.sendMessage(Text.translatable(open ? "tardis.gallifrey.msg.doors_open" : "tardis.gallifrey.msg.doors_closed"), true);
                }
            }
            case LOCK -> {
                tardis.setLocked(!tardis.isLocked());
                if (tardis.isLocked()) setDoors(tardis, false, true);
                player.sendMessage(Text.translatable(tardis.isLocked() ? "tardis.gallifrey.msg.locked"
                        : "tardis.gallifrey.msg.unlocked"), true);
            }
            case REFUEL -> {
                tardis.setRefueling(!tardis.isRefueling());
                player.sendMessage(Text.translatable(tardis.isRefueling() ? "tardis.gallifrey.msg.refuel_on"
                        : "tardis.gallifrey.msg.refuel_off", tardis.getFuel(), TardisExteriorBlockEntity.MAX_FUEL), true);
            }
            case FIND_GROUND -> {
                tardis.setLandDown(!tardis.isLandDown());
                player.sendMessage(Text.translatable(tardis.isLandDown() ? "tardis.gallifrey.msg.landing_down"
                        : "tardis.gallifrey.msg.landing_up"), true);
            }
            case X_UP, X_DOWN, Y_UP, Y_DOWN, Z_UP, Z_DOWN -> {
                ensureDestination(tardis);
                BlockPos d = tardis.getDestinationPos();
                int step = tardis.getIncrement() * (action == TardisPanelAction.X_UP || action == TardisPanelAction.Y_UP
                        || action == TardisPanelAction.Z_UP ? 1 : -1);
                BlockPos moved = switch (action) {
                    case X_UP, X_DOWN -> d.add(step, 0, 0);
                    case Y_UP, Y_DOWN -> new BlockPos(d.getX(), MathHelper.clamp(d.getY() + step, -64, 319), d.getZ());
                    default -> d.add(0, 0, step);
                };
                tardis.setDestination(tardis.getDestinationWorld(), clampWorldBorder(moved));
                sendDestination(player, tardis);
            }
            case INCREMENT -> {
                int index = 0;
                for (int i = 0; i < INCREMENTS.length; i++) if (INCREMENTS[i] == tardis.getIncrement()) index = i;
                index = Math.floorMod(index + direction, INCREMENTS.length);
                tardis.setIncrement(INCREMENTS[index]);
                player.sendMessage(Text.translatable("tardis.gallifrey.msg.increment", INCREMENTS[index]), true);
            }
            case ROTATION -> {
                int current = tardis.getDestRotation() >= 0 ? tardis.getDestRotation()
                        : tardis.getCachedState().get(TardisExteriorBlock.ROTATION);
                int next = Math.floorMod(current + 2 * direction, 8);
                tardis.setDestRotation(next);
                player.sendMessage(Text.translatable("tardis.gallifrey.msg.facing",
                        Text.translatable("tardis.gallifrey.facing." + next)), true);
            }
            case DIM_PREV, DIM_NEXT -> {
                ensureDestination(tardis);
                List<Identifier> worlds = new ArrayList<>();
                for (ServerWorld w : player.getServer().getWorlds()) {
                    if (!TardisDimensionManager.isInteriorWorld(w)) worlds.add(w.getRegistryKey().getValue());
                }
                worlds.sort(Comparator.comparing(Identifier::toString));
                int index = Math.max(0, worlds.indexOf(tardis.getDestinationWorld()));
                Identifier next = worlds.get(Math.floorMod(index + (action == TardisPanelAction.DIM_NEXT ? 1 : -1), worlds.size()));
                tardis.setDestination(next, tardis.getDestinationPos());
                player.sendMessage(Text.translatable("tardis.gallifrey.msg.dimension", dimensionName(next)), true);
            }
            case DIM_SHOW -> {
                ensureDestination(tardis);
                sendDestination(player, tardis);
            }
            case RANDOMISE -> {
                ensureDestination(tardis);
                BlockPos d = tardis.getDestinationPos();
                int radius = MathHelper.clamp(tardis.getIncrement() * 25, 250, 25000);
                var random = interior.getRandom();
                BlockPos picked = new BlockPos(d.getX() + random.nextBetween(-radius, radius), d.getY(),
                        d.getZ() + random.nextBetween(-radius, radius));
                tardis.setDestination(tardis.getDestinationWorld(), clampWorldBorder(picked));
                player.sendMessage(Text.translatable("tardis.gallifrey.msg.randomised"), false);
                sendDestination(player, tardis);
            }
            case FAST_RETURN -> {
                if (tardis.getLastWorld() == null || tardis.getLastPos() == null) {
                    player.sendMessage(Text.translatable("tardis.gallifrey.msg.no_return"), true);
                } else {
                    tardis.setDestination(tardis.getLastWorld(), tardis.getLastPos());
                    tardis.setDestRotation(tardis.getLastRotation());
                    player.sendMessage(Text.translatable("tardis.gallifrey.msg.fast_return"), false);
                    sendDestination(player, tardis);
                }
            }
        }
        for (ServerPlayerEntity p : insidePlayers(tardis)) TardisMonitorNetworking.sendStateIfOpen(p);
    }

    /** Where a panel's lever or switch should be, so its block can show it. */
    public static boolean panelState(TardisExteriorBlockEntity tardis, TardisPanel panel) {
        return switch (panel) {
            case FLIGHT_LEVER -> tardis.isFlightPending();
            case HANDBRAKE -> tardis.isHandbrakeOn();
            case REFUELER -> tardis.isRefueling();
            case LOCK_PANEL -> tardis.isLocked();
            case COORDINATE_PANEL -> tardis.isLandDown();
            default -> false;
        };
    }

    private static void sendDestination(ServerPlayerEntity player, TardisExteriorBlockEntity tardis) {
        BlockPos d = tardis.getDestinationPos();
        player.sendMessage(Text.translatable("tardis.gallifrey.msg.destination",
                d.getX(), d.getY(), d.getZ(), dimensionName(tardis.getDestinationWorld()), tardis.getIncrement()), true);
    }

    private static Text dimensionName(Identifier id) {
        String key = "dimension." + id.getNamespace() + "." + id.getPath();
        return Text.translatableWithFallback(key, id.toString());
    }

    private static BlockPos clampWorldBorder(BlockPos pos) {
        int limit = 29_999_000;
        return new BlockPos(MathHelper.clamp(pos.getX(), -limit, limit), pos.getY(), MathHelper.clamp(pos.getZ(), -limit, limit));
    }

    // ------------------------------------------------------------------ landing spots

    /**
     * Where the TARDIS can land near the requested spot: two clear blocks with something solid under
     * them. Normal landing searches up from the requested height to the surface (then down if there's
     * nothing above); "land below" searches down first, for caves and floors under the surface.
     */
    public static BlockPos findLanding(ServerWorld world, BlockPos requested, boolean down) {
        world.getChunk(requested.getX() >> 4, requested.getZ() >> 4);
        int bottom = world.getBottomY() + 1;
        int top = world.getTopY() - 3;
        int y0 = MathHelper.clamp(requested.getY(), bottom, top);
        if (down) {
            for (int y = y0; y >= bottom; y--) if (canLand(world, requested, y)) return at(requested, y);
            for (int y = y0 + 1; y <= top; y++) if (canLand(world, requested, y)) return at(requested, y);
        } else {
            for (int y = y0; y <= top; y++) if (canLand(world, requested, y)) return at(requested, y);
            for (int y = y0 - 1; y >= bottom; y--) if (canLand(world, requested, y)) return at(requested, y);
        }
        return null;
    }

    private static BlockPos at(BlockPos p, int y) { return new BlockPos(p.getX(), y, p.getZ()); }

    private static boolean canLand(ServerWorld world, BlockPos column, int y) {
        BlockPos p = at(column, y);
        return world.getBlockState(p).isReplaceable() && world.getFluidState(p).isEmpty()
                && world.getBlockState(p.up()).isReplaceable()
                && world.getBlockState(p.down()).isSolidBlock(world, p.down());
    }

    // ------------------------------------------------------------------ inside effects

    public static ServerWorld interiorOf(TardisExteriorBlockEntity tardis) {
        if (tardis.getWorld() == null || tardis.getWorld().getServer() == null || tardis.getTardisId() == null) return null;
        MinecraftServer server = tardis.getWorld().getServer();
        return TardisDimensionManager.getInterior(server, TardisRegistryState.get(server).get(tardis.getTardisId()));
    }

    public static List<ServerPlayerEntity> insidePlayers(TardisExteriorBlockEntity tardis) {
        ServerWorld interior = interiorOf(tardis);
        return interior == null ? List.of() : interior.getPlayers();
    }

    private static void playInside(TardisExteriorBlockEntity tardis, SoundEvent sound, float volume) {
        ServerWorld interior = interiorOf(tardis);
        if (interior == null) return;
        // Played on each player so the whole room hears it, wherever they stand.
        for (ServerPlayerEntity player : interior.getPlayers()) {
            interior.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.AMBIENT, volume, 1.0F);
        }
    }

    private static void playOutside(ServerWorld world, BlockPos pos, SoundEvent sound, float volume) {
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, volume, 1.0F);
    }

    /** Shakes the camera of everyone inside. intensity 1 = a hard jolt. */
    public static void shakeInside(TardisExteriorBlockEntity tardis, float intensity, int ticks) {
        for (ServerPlayerEntity player : insidePlayers(tardis)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(intensity);
            buf.writeVarInt(ticks);
            ServerPlayNetworking.send(player, ModPackets.TARDIS_SHAKE, buf);
        }
    }

    private static void refreshMonitors(MinecraftServer server, TardisRegistryState registry, TardisExteriorBlockEntity tardis) {
        for (ServerPlayerEntity player : insidePlayers(tardis)) TardisMonitorNetworking.sendStateIfOpen(player);
    }
}
