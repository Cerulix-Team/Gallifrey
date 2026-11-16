package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import com.cerulixteam.gallifrey.tardis.TardisInteriorCatalog;
import com.cerulixteam.gallifrey.tardis.TardisRegistryState;
import com.cerulixteam.gallifrey.tardis.TardisExteriorCatalog;
import com.cerulixteam.gallifrey.tardis.TardisInteriorLayout;
import com.cerulixteam.gallifrey.tardis.TardisPhaseCurves;
import com.cerulixteam.gallifrey.block.entity.TardisInteriorDoorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

/** Persistent state for one physical TARDIS exterior. */
public class TardisExteriorBlockEntity extends BlockEntity {
    public static final int MAX_FUEL = 1000;
    public static final int FLIGHT_COST = 10;

    /**
     * Where the TARDIS is in a flight, like the show: solid and LANDED; DEMAT while it fades away in
     * pulses with the take-off sound; VORTEX while it travels (invisible, nothing to touch); REMAT
     * while it fades back in at the destination with the landing sound. See TardisFlight.
     */
    public enum FlightPhase { LANDED, DEMAT, VORTEX, REMAT }

    private UUID tardisId;
    private UUID owner;
    private boolean locked;
    private boolean antigravity = true;
    private int fuel = 100;
    private BlockPos interiorOrigin = new BlockPos(0, 64, 0);
    private boolean interiorGenerated;
    private String interiorStructure = "70default";
    private String exteriorStyle = "policebox";
    private Vec3i interiorSize = new Vec3i(5, 5, 5);
    private BlockPos consolePos;
    private BlockPos interiorDoorPos;

    /** Where the throttle will take the TARDIS (set on the monitor). Null until a destination is set. */
    private Identifier destinationWorld;
    private BlockPos destinationPos;

    private FlightPhase phase = FlightPhase.LANDED;
    /** World time the current phase started. Clients use it too, so the fade is smooth without constant updates. */
    private long phaseStart;
    /** How long this flight spends in the vortex, in ticks. */
    private int vortexTicks;
    private Identifier flightWorld;
    private BlockPos flightPos;
    private int flightRotation;

    // Dalek Mod-style control settings (changed by the separate control blocks).
    private boolean handbrake = true;
    private boolean refueling;
    /** Landing type: false = land on the surface (search upwards), true = land below (caves). */
    private boolean landDown;
    private int increment = 10;
    /** Facing to land with (0-7, like the exterior's rotation), or -1 to keep the current one. */
    private int destRotation = -1;
    /** Where the TARDIS last took off from, for the fast return switch. */
    private Identifier lastWorld;
    private BlockPos lastPos;
    private int lastRotation;
    private int realWorldFlightTicks;
    private boolean realWorldFlight;
    private int selfDestructTicks;
    private boolean lastPoweredState = true;

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(GallifreyModBlockEntities.TARDIS_EXTERIOR, pos, state);
    }

    public void ensureInitialized(ServerPlayerEntity firstOwner) {
        if (tardisId == null) {
            UUID newId = UUID.randomUUID();
            // Every newly created TARDIS gets a real, isolated pocket world.
            // The legacy shared dimension remains available only for old saves.
            ServerWorld interior = TardisDimensionManager.ensureInteriorWorld(firstOwner.getServer(), newId);
            if (interior == null) {
                firstOwner.sendMessage(net.minecraft.text.Text.literal("The TARDIS could not create its pocket dimension."), true);
                return;
            }
            tardisId = newId;
            owner = firstOwner.getUuid();
            TardisRegistryState registry = TardisRegistryState.get(firstOwner.getServer());
            interiorOrigin = new BlockPos(0, 64, 0);
            String interiorDimension = interior.getRegistryKey().getValue().toString();
            registry.register(tardisId, owner, firstOwner.getServerWorld(), pos.asLong(),
                    interiorOrigin.asLong(), locked, interiorDimension);
            markDirty();
        }
    }

    public UUID getTardisId() { return tardisId; }
    public UUID getOwner() { return owner; }
    public boolean isLocked() { return locked; }
    public boolean isAntigravityEnabled() { return antigravity; }
    public int getFuel() { return fuel; }
    public boolean isPowered() { return fuel > 0; }

    /** Updates console power and the soft interior ceiling lights when artron power changes. */
    public void syncInteriorPower(ServerWorld interior) {
        if (interior == null) return;
        boolean powered = isPowered();
        if (powered == lastPoweredState && interiorGenerated) return;
        lastPoweredState = powered;
        if (consolePos != null && interior.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console) {
            console.setPowered(powered);
        }
        if (interiorDoorPos != null && interior.getBlockEntity(interiorDoorPos) instanceof TardisInteriorDoorBlockEntity door) {
            door.setPowered(powered);
        }
        int[][] points = {{-5,-5},{5,-5},{-5,5},{5,5},{0,0}};
        for (int[] point : points) {
            BlockPos lightPos = interiorOrigin.add(point[0], 6, point[1]);
            if (powered) {
                if (interior.getBlockState(lightPos).isAir()) {
                    interior.setBlockState(lightPos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 12), 3);
                }
            } else if (interior.getBlockState(lightPos).isOf(Blocks.LIGHT)) {
                interior.setBlockState(lightPos, Blocks.AIR.getDefaultState(), 3);
            }
        }
    }
    public int getSelfDestructTicks() { return selfDestructTicks; }
    public boolean isSelfDestructArmed() { return selfDestructTicks > 0; }
    public BlockPos getInteriorOrigin() { return interiorOrigin; }
    public String getInteriorStructure() { return interiorStructure; }
    public String getExteriorStyle() { return TardisExteriorCatalog.get(exteriorStyle).id(); }
    public String getExteriorStyleName() { return TardisExteriorCatalog.get(exteriorStyle).displayName(); }
    public void setExteriorStyle(String style) {
        exteriorStyle = TardisExteriorCatalog.get(style).id();
        updateInteriorDoorStyle();
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }
    public Vec3i getInteriorSize() { return interiorSize; }
    public BlockPos getConsolePos() { return consolePos; }
    public BlockPos getInteriorDoorPos() { return interiorDoorPos; }

    public void setConsolePos(BlockPos pos) {
        consolePos = pos == null ? null : pos.toImmutable();
        markDirty();
    }

    public void setInteriorDoorPos(BlockPos pos) {
        interiorDoorPos = pos == null ? null : pos.toImmutable();
        markDirty();
    }

    private void updateInteriorDoorStyle() {
        if (world == null || world.isClient || world.getServer() == null || tardisId == null || interiorDoorPos == null) return;
        TardisRegistryState.Record record = TardisRegistryState.get(world.getServer()).get(tardisId);
        if (record == null) return;
        ServerWorld interior = TardisDimensionManager.getInterior(world.getServer(), record);
        if (interior != null && interior.getBlockEntity(interiorDoorPos) instanceof TardisInteriorDoorBlockEntity door) {
            door.setExteriorStyle(exteriorStyle);
        }
    }

    public boolean canAccess(UUID player) {
        return !locked || (owner != null && owner.equals(player));
    }

    public boolean canPilot(UUID player) {
        return owner != null && owner.equals(player);
    }

    public void setAntigravityEnabled(boolean enabled) {
        this.antigravity = enabled;
        markDirty();
    }

    public void armSelfDestruct() {
        selfDestructTicks = 200;
        markDirty();
    }

    public void cancelSelfDestruct() {
        selfDestructTicks = 0;
        markDirty();
    }

    public boolean tickSelfDestruct() {
        if (selfDestructTicks <= 0) return false;
        selfDestructTicks--;
        markDirty();
        return selfDestructTicks <= 0;
    }

    public boolean tickRealWorldFlight() {
        if (realWorldFlightTicks <= 0) return false;
        realWorldFlightTicks--;
        markDirty();
        return true;
    }

    public int getRealWorldFlightTicks() { return realWorldFlightTicks; }
    public boolean isRealWorldFlight() { return realWorldFlight; }
    public void setRealWorldFlight(boolean enabled) { realWorldFlight = enabled; markDirty(); }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (world != null && world.getServer() != null && tardisId != null) {
            TardisRegistryState.get(world.getServer()).updateLock(tardisId, locked);
        }
        markDirty();
    }

    public int addFuel(int amount) {
        int old = fuel;
        fuel = Math.min(MAX_FUEL, fuel + Math.max(0, amount));
        markDirty();
        syncClient();
        return fuel - old;
    }

    public boolean consumeFuel(int amount) {
        if (fuel < amount) return false;
        fuel -= amount;
        markDirty();
        syncClient();
        return true;
    }

    public void syncClient() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), net.minecraft.block.Block.NOTIFY_ALL);
        }
    }

    public boolean generateInterior(ServerWorld world) {
        if (interiorGenerated) return true;
        Identifier structureId = TardisInteriorCatalog.id(interiorStructure);
        if (structureId == null) return false;

        java.util.Optional<net.minecraft.structure.StructureTemplate> template =
                world.getStructureTemplateManager().getTemplate(structureId);
        if (template.isEmpty()) return false;

        interiorSize = template.get().getSize();
        BlockPos placement = interiorOrigin.add(-2, 0, -2);
        ChunkPos chunk = new ChunkPos(placement);
        // Structure placement needs the target chunk loaded. POST_TELEPORT is
        // an entity ticket and requires an Integer ticket key, so it must not
        // be used with a chunk-position long.
        world.getChunk(chunk.x, chunk.z);
        template.get().place(world, placement, placement,
                new net.minecraft.structure.StructurePlacementData(), world.getRandom(), 2);
        installControls(world);
        installInteriorDoor(world);
        interiorGenerated = true;
        lastPoweredState = !isPowered();
        syncInteriorPower(world);
        markDirty();
        return true;
    }

    public boolean replaceInterior(ServerWorld world, String structureName) {
        Identifier structureId = TardisInteriorCatalog.id(structureName);
        if (structureId == null) return false;
        java.util.Optional<net.minecraft.structure.StructureTemplate> template =
                world.getStructureTemplateManager().getTemplate(structureId);
        if (template.isEmpty()) return false;

        // Older TARDIS saves predate the stored structure footprint. Recover it
        // from the currently selected template before clearing the old interior.
        if (interiorSize.getX() <= 1 && interiorSize.getY() <= 1 && interiorSize.getZ() <= 1) {
            Identifier oldId = TardisInteriorCatalog.id(interiorStructure);
            if (oldId != null) {
                java.util.Optional<net.minecraft.structure.StructureTemplate> oldTemplate =
                        world.getStructureTemplateManager().getTemplate(oldId);
                oldTemplate.ifPresent(value -> interiorSize = value.getSize());
            }
        }
        clearInterior(world);
        interiorStructure = structureName.toLowerCase(java.util.Locale.ROOT);
        interiorSize = template.get().getSize();
        BlockPos placement = interiorOrigin.add(-2, 0, -2);
        ChunkPos chunk = new ChunkPos(placement);
        // Ensure the target chunk is loaded before placing the structure.
        world.getChunk(chunk.x, chunk.z);
        template.get().place(world, placement, placement,
                new net.minecraft.structure.StructurePlacementData(), world.getRandom(), 2);
        installControls(world);
        installInteriorDoor(world);
        interiorGenerated = true;
        lastPoweredState = !isPowered();
        syncInteriorPower(world);
        markDirty();
        return true;
    }

    /**
     * Places the police-box exit door where the interior's door marker is, facing into the room
     * (see TardisInteriorLayout). Interiors without a marker get it near the front of the room.
     */
    private void installInteriorDoor(ServerWorld world) {
        BlockPos placement = interiorOrigin.add(-2, 0, -2);
        TardisInteriorLayout.Layout layout = TardisInteriorLayout.get(world.getServer().getResourceManager(), interiorStructure);
        if (layout.door() != null) {
            placeInteriorDoor(world, placement.add(layout.door()), layout.doorFacing());
            return;
        }
        BlockPos base = interiorOrigin.add(0, 1, -5);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = base.add(dx, 0, dz);
                if (world.getBlockState(candidate).isAir()
                        && world.getBlockState(candidate.down()).isSolidBlock(world, candidate.down())) {
                    placeInteriorDoor(world, candidate, net.minecraft.util.math.Direction.SOUTH);
                    return;
                }
            }
        }
    }

    private void placeInteriorDoor(ServerWorld world, BlockPos pos, net.minecraft.util.math.Direction roomSide) {
        world.setBlockState(pos, com.cerulixteam.gallifrey.block.GallifreyModBlocks.TARDIS_INTERIOR_DOOR.getDefaultState()
                .with(com.cerulixteam.gallifrey.block.TardisInteriorDoorBlock.FACING, roomSide), 3);
        if (world.getBlockEntity(pos) instanceof TardisInteriorDoorBlockEntity door) {
            door.setTardisId(tardisId);
            door.setExteriorStyle(exteriorStyle);
            door.setPowered(isPowered());
        }
        interiorDoorPos = pos.toImmutable();
        markDirty();
    }

    /**
     * Sets out the control panels (Dalek Mod style) in a ring where a console would stand: flight lever,
     * monitor, coordinate panel, dimension selector, fast return, lock panel, handbrake, refueller and
     * randomiser. Each faces outwards so you work it from outside the ring. Players can pick them up and
     * put them wherever they like, on floors, walls or ceilings.
     */
    private void installControls(ServerWorld world) {
        java.util.List<net.minecraft.block.Block> controls = new java.util.ArrayList<>();
        var panels = com.cerulixteam.gallifrey.block.GallifreyModBlocks.TARDIS_PANELS;
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.FLIGHT_LEVER));
        controls.add(com.cerulixteam.gallifrey.block.GallifreyModBlocks.TARDIS_MONITOR);
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.COORDINATE_PANEL));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.DIMENSION_SELECTOR));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.FAST_RETURN));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.LOCK_PANEL));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.HANDBRAKE));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.REFUELER));
        controls.add(panels.get(com.cerulixteam.gallifrey.tardis.TardisPanel.RANDOMISER));
        // Around where the interior's console marker is (the middle of the console room).
        TardisInteriorLayout.Layout layout = TardisInteriorLayout.get(world.getServer().getResourceManager(), interiorStructure);
        BlockPos centre = layout.console() != null
                ? interiorOrigin.add(-2, 0, -2).add(layout.console())
                : interiorOrigin.add(0, 1, 2);
        java.util.List<BlockPos> spots = new java.util.ArrayList<>();
        for (int r = 2; r <= 4 && spots.size() < controls.size(); r++) {
            for (int i = 0; i < 8 * r; i++) {
                double a = (i + 0.5) * Math.PI * 2 / (8 * r);
                BlockPos p = centre.add((int) Math.round(Math.cos(a) * r), 0, (int) Math.round(Math.sin(a) * r));
                if (!spots.contains(p) && world.getBlockState(p).isAir()
                        && world.getBlockState(p.down()).isSolidBlock(world, p.down())) spots.add(p);
            }
        }
        for (int i = 0; i < controls.size() && i < spots.size(); i++) {
            BlockPos p = spots.get(i);
            net.minecraft.util.math.Direction out = net.minecraft.util.math.Direction.getFacing(
                    p.getX() - centre.getX(), 0, p.getZ() - centre.getZ());
            BlockState state = controls.get(i).getDefaultState();
            for (net.minecraft.state.property.Property<?> property : state.getProperties()) {
                if (property.getName().equals("facing") && property.getValues().contains(out)) {
                    @SuppressWarnings("unchecked")
                    net.minecraft.state.property.Property<net.minecraft.util.math.Direction> facing =
                            (net.minecraft.state.property.Property<net.minecraft.util.math.Direction>) property;
                    state = state.with(facing, out);
                }
            }
            world.setBlockState(p, state, 3);
        }
    }

    private void clearInterior(ServerWorld world) {
        BlockPos placement = interiorOrigin.add(-2, 0, -2);
        int maxX = Math.max(1, interiorSize.getX());
        int maxY = Math.max(1, interiorSize.getY());
        int maxZ = Math.max(1, interiorSize.getZ());
        for (int x = -1; x < maxX + 1; x++) {
            for (int y = -1; y < maxY + 1; y++) {
                for (int z = -1; z < maxZ + 1; z++) {
                    world.setBlockState(placement.add(x, y, z), net.minecraft.block.Blocks.AIR.getDefaultState(), 2);
                }
            }
        }
    }

    // ------------------------------------------------------------------ flight

    public FlightPhase getPhase() { return phase; }
    public long getPhaseStart() { return phaseStart; }

    /** Ticks since the current phase began. */
    public int getPhaseAge() {
        return world == null ? 0 : (int) Math.max(0, world.getTime() - phaseStart);
    }

    /** Starts a take-off. TardisFlight checks everything first; this only records it. */
    public void beginDemat(Identifier targetWorld, BlockPos targetPos, int rotation, int vortexTicks) {
        flightWorld = targetWorld;
        flightPos = targetPos.toImmutable();
        flightRotation = rotation;
        this.vortexTicks = vortexTicks;
        realWorldFlightTicks = 0;
        setPhase(FlightPhase.DEMAT);
    }

    public void setPhase(FlightPhase phase) {
        this.phase = phase;
        phaseStart = world == null ? 0 : world.getTime();
        markDirty();
        syncClient();
    }

    /** Holding in the vortex (handbrake on): push the phase start forward so the trip doesn't progress. */
    public void holdInVortex() {
        phaseStart++;
        markDirty();
    }

    public int getVortexTicks() { return vortexTicks; }

    /**
     * True while the box is too faded to touch: no collision, no doors, can't be clicked.
     * Same answer on client and server (it only depends on synced values and the world time).
     */
    public boolean isIntangible() {
        return isIntangible(0);
    }

    public boolean isIntangible(float tickDelta) {
        return visibility(tickDelta) < 0.35F;
    }

    /** How visible the exterior is right now (0 = gone, 1 = solid). */
    public float visibility(float tickDelta) {
        float age = getPhaseAge() + tickDelta;
        return switch (phase) {
            case LANDED -> 1.0F;
            case DEMAT -> TardisPhaseCurves.dematAlpha(age);
            case VORTEX -> 0.0F;
            case REMAT -> TardisPhaseCurves.rematAlpha(age);
        };
    }

    /** Brightness of the lamp on top (it flashes in time with the sound during take-off and landing). */
    public float lampBrightness(float tickDelta) {
        float age = getPhaseAge() + tickDelta;
        return switch (phase) {
            case LANDED -> 1.0F;
            case DEMAT -> TardisPhaseCurves.dematLamp(age);
            case VORTEX -> 0.0F;
            case REMAT -> TardisPhaseCurves.rematLamp(age);
        };
    }

    /** Any part of a flight (taking off, in the vortex or landing). */
    public boolean isFlightPending() { return phase != FlightPhase.LANDED; }

    public void setDestination(Identifier world, BlockPos pos) {
        destinationWorld = world;
        destinationPos = pos == null ? null : pos.toImmutable();
        markDirty();
    }

    public Identifier getDestinationWorld() { return destinationWorld; }

    public BlockPos getDestinationPos() { return destinationPos; }

    public boolean hasDestination() { return destinationWorld != null && destinationPos != null; }

    public void cancelFlight() {
        flightWorld = null;
        flightPos = null;
        realWorldFlightTicks = 0;
        realWorldFlight = false;
        setPhase(FlightPhase.LANDED);
    }

    public Identifier getFlightWorld() { return flightWorld; }
    public BlockPos getFlightPos() { return flightPos; }
    public int getFlightRotation() { return flightRotation; }

    /** Seconds until the TARDIS arrives and can be landed with the flight lever (for messages). */
    public int secondsToArrival() {
        int left = switch (phase) {
            case LANDED -> 0;
            case DEMAT -> TardisPhaseCurves.DEMAT_TICKS - getPhaseAge() + vortexTicks;
            case VORTEX -> vortexTicks - getPhaseAge();
            case REMAT -> TardisPhaseCurves.REMAT_TICKS - getPhaseAge();
        };
        return Math.max(0, left / 20);
    }

    // ------------------------------------------------------------------ control settings

    public boolean isHandbrakeOn() { return handbrake; }
    public void setHandbrake(boolean on) { handbrake = on; markDirty(); }
    public boolean isRefueling() { return refueling; }
    public void setRefueling(boolean on) { refueling = on; markDirty(); }
    public boolean isLandDown() { return landDown; }
    public void setLandDown(boolean down) { landDown = down; markDirty(); }
    public int getIncrement() { return increment; }
    public void setIncrement(int increment) { this.increment = Math.max(1, increment); markDirty(); }
    public int getDestRotation() { return destRotation; }
    public void setDestRotation(int rotation) { destRotation = rotation; markDirty(); }
    public Identifier getLastWorld() { return lastWorld; }
    public BlockPos getLastPos() { return lastPos; }
    public int getLastRotation() { return lastRotation; }

    public void setLastLocation(Identifier world, BlockPos pos, int rotation) {
        lastWorld = world;
        lastPos = pos == null ? null : pos.toImmutable();
        lastRotation = rotation;
        markDirty();
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (tardisId != null) nbt.putUuid("TardisId", tardisId);
        if (owner != null) nbt.putUuid("Owner", owner);
        nbt.putBoolean("Locked", locked);
        nbt.putBoolean("Antigravity", antigravity);
        nbt.putInt("Fuel", fuel);
        nbt.putLong("InteriorOrigin", interiorOrigin.asLong());
        nbt.putBoolean("InteriorGenerated", interiorGenerated);
        nbt.putString("InteriorStructure", interiorStructure);
        nbt.putString("ExteriorStyle", exteriorStyle);
        if (interiorDoorPos != null) nbt.putLong("InteriorDoorPos", interiorDoorPos.asLong());
        nbt.putInt("InteriorSizeX", interiorSize.getX());
        nbt.putInt("InteriorSizeY", interiorSize.getY());
        nbt.putInt("InteriorSizeZ", interiorSize.getZ());

        if (destinationWorld != null) nbt.putString("DestinationWorld", destinationWorld.toString());
        if (destinationPos != null) nbt.putLong("DestinationPos", destinationPos.asLong());
        nbt.putString("Phase", phase.name());
        nbt.putLong("PhaseStart", phaseStart);
        nbt.putInt("VortexTicks", vortexTicks);
        nbt.putBoolean("Handbrake", handbrake);
        nbt.putBoolean("Refueling", refueling);
        nbt.putBoolean("LandDown", landDown);
        nbt.putInt("Increment", increment);
        nbt.putInt("DestRotation", destRotation);
        if (lastWorld != null) nbt.putString("LastWorld", lastWorld.toString());
        if (lastPos != null) nbt.putLong("LastPos", lastPos.asLong());
        nbt.putInt("LastRotation", lastRotation);
        if (consolePos != null) nbt.putLong("ConsolePos", consolePos.asLong());
        if (flightWorld != null) nbt.putString("FlightWorld", flightWorld.toString());
        if (flightPos != null) nbt.putLong("FlightPos", flightPos.asLong());
        nbt.putInt("FlightRotation", flightRotation);
        nbt.putInt("RealWorldFlightTicks", realWorldFlightTicks);
        nbt.putBoolean("RealWorldFlight", realWorldFlight);
        nbt.putInt("SelfDestructTicks", selfDestructTicks);
        nbt.putBoolean("LastPoweredState", lastPoweredState);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.containsUuid("TardisId")) tardisId = nbt.getUuid("TardisId");
        if (nbt.containsUuid("Owner")) owner = nbt.getUuid("Owner");
        locked = nbt.getBoolean("Locked");
        antigravity = !nbt.contains("Antigravity") || nbt.getBoolean("Antigravity");
        fuel = Math.max(0, Math.min(MAX_FUEL, nbt.getInt("Fuel")));
        if (nbt.contains("InteriorOrigin")) interiorOrigin = BlockPos.fromLong(nbt.getLong("InteriorOrigin"));
        interiorGenerated = nbt.getBoolean("InteriorGenerated");
        if (nbt.contains("InteriorStructure")) interiorStructure = nbt.getString("InteriorStructure");
        if (nbt.contains("ExteriorStyle")) {
            exteriorStyle = TardisExteriorCatalog.get(nbt.getString("ExteriorStyle")).id();
        } else if (nbt.contains("ExteriorVariant")) {
            exteriorStyle = TardisExteriorCatalog.migrateLegacyIndex(nbt.getInt("ExteriorVariant"));
        } else {
            exteriorStyle = "policebox";
        }
        if (nbt.contains("InteriorDoorPos")) interiorDoorPos = BlockPos.fromLong(nbt.getLong("InteriorDoorPos"));
        interiorSize = new Vec3i(Math.max(1, nbt.getInt("InteriorSizeX")), Math.max(1, nbt.getInt("InteriorSizeY")), Math.max(1, nbt.getInt("InteriorSizeZ")));

        destinationWorld = nbt.contains("DestinationWorld") ? new Identifier(nbt.getString("DestinationWorld")) : null;
        destinationPos = nbt.contains("DestinationPos") ? BlockPos.fromLong(nbt.getLong("DestinationPos")) : null;
        if (nbt.contains("Phase")) {
            try { phase = FlightPhase.valueOf(nbt.getString("Phase")); } catch (IllegalArgumentException e) { phase = FlightPhase.LANDED; }
        } else {
            // Saves from before flight phases: a flight in progress finishes from the vortex.
            phase = nbt.getBoolean("FlightPending") ? FlightPhase.VORTEX : FlightPhase.LANDED;
        }
        phaseStart = nbt.getLong("PhaseStart");
        vortexTicks = nbt.contains("VortexTicks") ? nbt.getInt("VortexTicks") : 20;
        handbrake = !nbt.contains("Handbrake") || nbt.getBoolean("Handbrake");
        refueling = nbt.getBoolean("Refueling");
        landDown = nbt.getBoolean("LandDown");
        increment = nbt.contains("Increment") ? Math.max(1, nbt.getInt("Increment")) : 10;
        destRotation = nbt.contains("DestRotation") ? nbt.getInt("DestRotation") : -1;
        lastWorld = nbt.contains("LastWorld") ? new Identifier(nbt.getString("LastWorld")) : null;
        lastPos = nbt.contains("LastPos") ? BlockPos.fromLong(nbt.getLong("LastPos")) : null;
        lastRotation = nbt.getInt("LastRotation");
        consolePos = nbt.contains("ConsolePos") ? BlockPos.fromLong(nbt.getLong("ConsolePos")) : null;
        if (nbt.contains("FlightWorld")) flightWorld = new Identifier(nbt.getString("FlightWorld"));
        if (nbt.contains("FlightPos")) flightPos = BlockPos.fromLong(nbt.getLong("FlightPos"));
        flightRotation = nbt.getInt("FlightRotation");
        realWorldFlightTicks = nbt.getInt("RealWorldFlightTicks");
        realWorldFlight = nbt.getBoolean("RealWorldFlight");
        selfDestructTicks = nbt.getInt("SelfDestructTicks");
        lastPoweredState = nbt.contains("LastPoweredState") ? nbt.getBoolean("LastPoweredState") : isPowered();
    }
}
