package com.cerulixteam.gallifrey.networking.packets;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import com.cerulixteam.gallifrey.item.custom.VortexManipulatorData;
import com.cerulixteam.gallifrey.networking.ModPackets;
import com.cerulixteam.gallifrey.util.GallifreyDimensionTravelAccess;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class VMPacket {

    private static final Identifier LOST_REALITY =
            new Identifier("gallifrey", "lost_reality");

    private static final Map<UUID, PendingTeleportEffect> PENDING_EFFECTS =
            new ConcurrentHashMap<>();

    private static final Map<UUID, Integer> SELF_DESTRUCTS =
            new ConcurrentHashMap<>();

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public static final SoundEvent VM_TAKE_OFF_SOUND =
            SoundEvent.of(new Identifier("gallifrey", "vm_take_off"));

    public static final SoundEvent VM_LAND_SOUND =
            SoundEvent.of(new Identifier("gallifrey", "vm_land"));

    static {
        ServerTickEvents.END_SERVER_TICK.register(VMPacket::tickTeleport);
    }

    private static void tickTeleport(MinecraftServer server) {
        tickSelfDestruct(server);

        Iterator<Map.Entry<UUID, PendingTeleportEffect>> iterator =
                PENDING_EFFECTS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingTeleportEffect> entry = iterator.next();
            PendingTeleportEffect pending = entry.getValue();

            ServerPlayerEntity player =
                    server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null || player.isRemoved()) {
                SELF_DESTRUCTS.remove(entry.getKey());
                cleanupPending(entry, iterator, null);
                continue;
            }

            if (pending.phase() == TeleportPhase.SOURCE_WARMUP) {
                ServerWorld sourceWorld =
                        server.getWorld(pending.sourceWorldKey());

                if (sourceWorld == null) {
                    cleanup(player, pending);
                    iterator.remove();
                    continue;
                }

                if (pending.ticksRemaining() > 0) {
                    entry.setValue(pending.tickDown());
                    continue;
                }

                Vec3d source = pending.sourcePosition();

                sourceWorld.playSound(
                        null, source.x, source.y, source.z,
                        VM_TAKE_OFF_SOUND,
                        SoundCategory.PLAYERS, 1.0F, 1.0F
                );

                sourceWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        source.x, source.y + 1.0, source.z,
                        60, 0.4, 0.8, 0.4, 0.05
                );

                entry.setValue(
                        pending.nextPhase(TeleportPhase.PRE_CLOAK, 19)
                );
                continue;
            }

            if (pending.phase() == TeleportPhase.PRE_CLOAK) {
                if (pending.ticksRemaining() > 0) {
                    entry.setValue(pending.tickDown());
                    continue;
                }

                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.INVISIBILITY,
                        40, 0, false, false, false
                ));

                ItemStack mainHand = player.getMainHandStack();
                ItemStack offHand = player.getOffHandStack();

                ItemStack hiddenMainHand = ItemStack.EMPTY;
                ItemStack hiddenOffHand = ItemStack.EMPTY;

                if (mainHand.isOf(GallifreyModItems.VORTEX_MANIPULATOR)) {
                    hiddenMainHand = mainHand.copy();
                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                }

                if (offHand.isOf(GallifreyModItems.VORTEX_MANIPULATOR)) {
                    hiddenOffHand = offHand.copy();
                    player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                }

                Map<EquipmentSlot, ItemStack> hiddenArmor =
                        new EnumMap<>(EquipmentSlot.class);

                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    ItemStack piece = player.getEquippedStack(slot);

                    if (!piece.isEmpty()) {
                        hiddenArmor.put(slot, piece.copy());
                        player.equipStack(slot, ItemStack.EMPTY);
                    }
                }

                entry.setValue(pending.cloak(
                        hiddenMainHand,
                        hiddenOffHand,
                        hiddenArmor,
                        TeleportPhase.TELEPORT,
                        20
                ));
                continue;
            }

            if (pending.phase() == TeleportPhase.TELEPORT) {
                ServerWorld targetWorld =
                        server.getWorld(pending.targetWorldKey());

                if (targetWorld == null) {
                    cleanup(player, pending);
                    iterator.remove();
                    continue;
                }

                if (pending.ticksRemaining() > 0) {
                    entry.setValue(pending.tickDown());
                    continue;
                }

                if (!player.isAlive()
                        || !player.getServerWorld().getRegistryKey()
                        .equals(pending.sourceWorldKey())) {

                    cleanup(player, pending);

                    if (player.isAlive()) {
                        player.sendMessage(
                                Text.literal("Vortex Manipulator jump interrupted."),
                                true
                        );
                    }

                    iterator.remove();
                    continue;
                }

                if (player.isSleeping()) {
                    player.wakeUp();
                }

                ChunkPos chunkPos = new ChunkPos(
                        pending.targetChunkPos().x(),
                        pending.targetChunkPos().z()
                );

                targetWorld.getChunkManager().addTicket(
                        ChunkTicketType.POST_TELEPORT,
                        chunkPos,
                        1,
                        player.getId()
                );

                Vec3d target = pending.targetPosition();

                // Only the VM's transfer is authorized to enter Lost Reality.
                if (targetWorld.getRegistryKey().getValue()
                        .equals(LOST_REALITY)) {

                    GallifreyDimensionTravelAccess.runAuthorized(
                            player,
                            () -> player.teleport(
                                    targetWorld,
                                    target.x,
                                    target.y,
                                    target.z,
                                    player.getYaw(),
                                    player.getPitch()
                            )
                    );

                } else {
                    player.teleport(
                            targetWorld,
                            target.x,
                            target.y,
                            target.z,
                            player.getYaw(),
                            player.getPitch()
                    );
                }

                entry.setValue(
                        pending.nextPhase(TeleportPhase.TARGET_ARRIVAL, 10)
                );
                continue;
            }

            ServerWorld targetWorld =
                    server.getWorld(pending.targetWorldKey());

            if (targetWorld == null
                    || player.getServerWorld() != targetWorld) {
                cleanup(player, pending);
                iterator.remove();
                continue;
            }

            if (pending.ticksRemaining() > 0) {
                entry.setValue(pending.tickDown());
                continue;
            }

            Vec3d target = pending.targetPosition();

            targetWorld.playSound(
                    null, target.x, target.y, target.z,
                    VM_LAND_SOUND,
                    SoundCategory.PLAYERS, 1.0F, 1.0F
            );

            targetWorld.spawnParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    target.x, target.y + 1.0, target.z,
                    60, 0.4, 0.8, 0.4, 0.05
            );

            cleanup(player, pending);

            ItemStack arrivedVm = findVortexManipulator(player);

            if (!arrivedVm.isEmpty()) {
                NbtCompound arrivedNbt = arrivedVm.getOrCreateNbt();

                if (!arrivedNbt.contains("VMUsesRemaining")) {
                    arrivedNbt.putInt("VMUsesRemaining", 10);
                }

                int remaining = Math.max(
                        0, arrivedNbt.getInt("VMUsesRemaining")
                );

                boolean consumesPower =
                        !player.getAbilities().creativeMode
                                && !player.isSpectator();

                if (consumesPower && remaining > 0) {
                    arrivedNbt.putInt("VMUsesRemaining", remaining - 1);
                }

                if (consumesPower) {
                    player.getItemCooldownManager().set(
                            arrivedVm.getItem(), 15 * 20
                    );
                }

                int afterUse = Math.max(
                        0, arrivedNbt.getInt("VMUsesRemaining")
                );

                player.sendMessage(
                        Text.literal(
                                "Vortex Manipulator power: "
                                        + afterUse + "/10 uses remaining."
                        ),
                        true
                );
            }

            iterator.remove();
        }
    }

    /*
     * Removes a pending teleport and restores equipment when a player
     * is no longer available.
     */
    private static void cleanupPending(
            Map.Entry<UUID, PendingTeleportEffect> entry,
            Iterator<Map.Entry<UUID, PendingTeleportEffect>> iterator,
            ServerPlayerEntity player
    ) {
        if (player != null) {
            cleanup(player, entry.getValue());
        }
        iterator.remove();
    }

    private static void cleanup(
            ServerPlayerEntity player,
            PendingTeleportEffect pending
    ) {
        restoreHiddenItems(player, pending);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
    }

    public static void receive(
            MinecraftServer server,
            ServerPlayerEntity player,
            ServerPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        PacketByteBuf copy = new PacketByteBuf(buf.copy());
        String action = copy.readString(32);
        server.execute(() -> handle(server, player, action, copy));
    }

    private static void handle(
            MinecraftServer server,
            ServerPlayerEntity player,
            String action,
            PacketByteBuf buf
    ) {
        ItemStack vm = findVortexManipulator(player);

        if (vm.isEmpty()) {
            player.sendMessage(
                    Text.literal("You need a Vortex Manipulator."), true
            );
            return;
        }

        VortexManipulatorData.ensureOwner(vm, player);

        switch (action) {
            case "REQUEST_STATE" -> sendState(server, player);
            case "TELEPORT" -> teleport(server, player, vm, buf);
            case "SAVE" -> saveLocation(player, vm, buf);
            case "DELETE" -> deleteLocation(server, player, vm, buf);
            case "GO" -> goLocation(server, player, vm, buf);
            case "ADD_PLAYER" -> addPlayer(server, player, vm, buf);
            case "REMOVE_PLAYER" -> removePlayer(server, player, vm, buf);
            case "SET_INBOUND_TELEPORTS" ->
                    setInboundTeleports(server, player, vm, buf);
            case "SELF_DESTRUCT" -> armSelfDestruct(player, vm);
            case "CANCEL_SELF_DESTRUCT" -> cancelSelfDestruct(player, vm);
            default -> player.sendMessage(
                    Text.literal("Unknown VM command."), true
            );
        }
    }

    private static void teleport(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isAuthorized(vm, player.getUuid())) {
            player.sendMessage(Text.literal("ISOMORPHIC LOCK ACTIVE."), true);
            return;
        }

        if (!player.getAbilities().creativeMode
                && player.getItemCooldownManager().isCoolingDown(vm.getItem())) {
            player.sendMessage(
                    Text.literal(
                            "VORTEX MANIPULATOR RECHARGING: please wait 15 seconds after arrival."
                    ),
                    true
            );
            return;
        }

        NbtCompound vmNbt = vm.getOrCreateNbt();

        if (!vmNbt.contains("VMUsesRemaining")) {
            vmNbt.putInt("VMUsesRemaining", 10);
        }

        if (vmNbt.getInt("VMUsesRemaining") <= 0) {
            player.sendMessage(
                    Text.literal(
                            "VORTEX MANIPULATOR OUT OF POWER: refuel with an Atrium Core."
                    ),
                    true
            );
            return;
        }

        if (PENDING_EFFECTS.containsKey(player.getUuid())) {
            player.sendMessage(
                    Text.literal("Vortex Manipulator sequence already active."),
                    true
            );
            return;
        }

        if (isInAnyLiquid(player)) {
            player.sendMessage(
                    Text.literal(
                            "VORTEX MANIPULATOR DISABLED: liquid interference detected."
                    ),
                    true
            );
            return;
        }

        boolean targetPlayerMode = buf.readBoolean();
        String targetPlayerName =
                targetPlayerMode ? buf.readString(64) : "";

        Identifier dimensionId =
                !targetPlayerMode ? buf.readIdentifier() : null;

        double x = !targetPlayerMode ? buf.readDouble() : 0;
        double y = !targetPlayerMode ? buf.readDouble() : 0;
        double z = !targetPlayerMode ? buf.readDouble() : 0;

        boolean surfaceMode = !targetPlayerMode && buf.readBoolean();

        ServerWorld targetWorld;
        Vec3d targetPos;

        if (targetPlayerMode) {
            ServerPlayerEntity targetPlayer =
                    server.getPlayerManager().getPlayer(targetPlayerName);

            if (targetPlayer == null) {
                player.sendMessage(
                        Text.literal("Player not found: " + targetPlayerName),
                        true
                );
                return;
            }

            if (!targetPlayer.getUuid().equals(player.getUuid())) {
                ItemStack targetVm = findVortexManipulator(targetPlayer);

                if (!targetVm.isEmpty()) {
                    VortexManipulatorData.ensureOwner(targetVm, targetPlayer);

                    if (VortexManipulatorData.blocksInboundTeleports(targetVm)) {
                        player.sendMessage(
                                Text.literal(
                                        "That player has disabled incoming Vortex Manipulator teleports."
                                ),
                                true
                        );
                        return;
                    }
                }
            }

            targetWorld = targetPlayer.getServerWorld();
            targetPos = targetPlayer.getPos();

            // Following a player into Lost Reality would be a way in that isn't the drift.
            if (LOST_REALITY.equals(targetWorld.getRegistryKey().getValue())
                    && !LOST_REALITY.equals(player.getServerWorld().getRegistryKey().getValue())) {
                player.sendMessage(
                        Text.literal("§5Cannot lock on: that player's dimensional coordinates are corrupted."),
                        true
                );
                return;
            }

        } else {
            // Direct selection of Lost Reality is always denied.
            // The random drift below is the only VM exception.
            if (LOST_REALITY.equals(dimensionId)) {
                player.sendMessage(
                        Text.literal(
                                "§5Destination unavailable: dimensional coordinates are corrupted."
                        ),
                        true
                );
                return;
            }

            RegistryKey<World> key =
                    RegistryKey.of(RegistryKeys.WORLD, dimensionId);

            targetWorld = server.getWorld(key);

            if (targetWorld == null) {
                player.sendMessage(
                        Text.literal("Unknown dimension: " + dimensionId),
                        true
                );
                return;
            }

            if (!checkDimensionUnlock(server, player, vm, dimensionId)) {
                return;
            }

            // IMPORTANT: establish the normal destination before attempting drift.
            // If drift succeeds, its safe position replaces this value.
            targetPos = new Vec3d(x, y, z);

            // Preserve the original 1-in-40 drift chance for cross-dimensional travel.
            if (!player.getServerWorld().getRegistryKey().getValue()
                    .equals(dimensionId)
                    && ThreadLocalRandom.current().nextInt(40) == 0) {

                ServerWorld lostReality = server.getWorld(
                        RegistryKey.of(RegistryKeys.WORLD, LOST_REALITY)
                );

                if (lostReality != null) {
                    Vec3d driftPosition =
                            findLostRealityArrival(lostReality);

                    if (driftPosition != null) {
                        targetWorld = lostReality;
                        targetPos = driftPosition;
                        surfaceMode = false;

                        player.sendMessage(
                                Text.literal(
                                        "§5DIMENSIONAL DRIFT! The Vortex Manipulator lost its lock and dumped you in Lost Reality."
                                ),
                                false
                        );
                    }
                }
            }

            // Do not use an else here: it would overwrite driftPosition.
            if (surfaceMode) {
                int surfaceX = (int) Math.floor(x);
                int surfaceZ = (int) Math.floor(z);

                Double surfaceY = findSafeSurfaceY(
                        targetWorld, surfaceX, surfaceZ
                );

                if (surfaceY == null) {
                    player.sendMessage(
                            Text.literal(
                                    "SURFACE TELEPORT FAILED: no safe surface above Y=64 at those coordinates."
                            ),
                            true
                    );
                    return;
                }

                targetPos = new Vec3d(x, surfaceY, z);
            }
        }

        // Reject invalid coordinates, unsafe world heights and border positions.
        if (!Double.isFinite(targetPos.x)
                || !Double.isFinite(targetPos.y)
                || !Double.isFinite(targetPos.z)) {
            player.sendMessage(
                    Text.literal("VMS SAFETY LOCK: invalid destination coordinates."),
                    true
            );
            return;
        }

        BlockPos targetBlockPos = BlockPos.ofFloored(targetPos);

        if (targetBlockPos.getY() < targetWorld.getBottomY()
                || targetBlockPos.getY() >= targetWorld.getTopY()
                || !targetWorld.getWorldBorder().contains(targetBlockPos)
                || !targetWorld.getWorldBorder().contains(
                targetBlockPos.add(1, 0, 0))
                || !targetWorld.getWorldBorder().contains(
                targetBlockPos.add(-1, 0, 0))
                || !targetWorld.getWorldBorder().contains(
                targetBlockPos.add(0, 0, 1))
                || !targetWorld.getWorldBorder().contains(
                targetBlockPos.add(0, 0, -1))) {

            player.sendMessage(
                    Text.literal(
                            "VMS SAFETY LOCK: destination is outside or too close to the world border, or outside the world height."
                    ),
                    true
            );
            return;
        }

        // Land claims (Flan, GOML, Open Parties and Claims...): no jumping into land you aren't trusted in.
        if (!com.cerulixteam.gallifrey.compat.claims.ClaimsCompat.canUseBlock(player, targetWorld, targetBlockPos)) {
            player.sendMessage(
                    Text.literal("§cVMS SAFETY LOCK: destination is inside protected land."),
                    true
            );
            return;
        }

        ServerWorld sourceWorld = player.getServerWorld();
        Vec3d sourcePos = player.getPos();

        PENDING_EFFECTS.put(
                player.getUuid(),
                new PendingTeleportEffect(
                        sourceWorld.getRegistryKey(),
                        sourcePos,
                        targetWorld.getRegistryKey(),
                        targetPos,
                        new ChunkPosKey(
                                targetBlockPos.getX() >> 4,
                                targetBlockPos.getZ() >> 4
                        ),
                        TeleportPhase.SOURCE_WARMUP,
                        1,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        Map.of()
                )
        );
    }

    private static boolean isInAnyLiquid(ServerPlayerEntity player) {
        if (player.isTouchingWater() || player.isInLava()) {
            return true;
        }

        ServerWorld world = player.getServerWorld();

        BlockPos feet = BlockPos.ofFloored(
                player.getX(), player.getY() + 0.1D, player.getZ()
        );

        BlockPos body = BlockPos.ofFloored(
                player.getX(),
                player.getY() + Math.max(0.5D, player.getHeight() * 0.5D),
                player.getZ()
        );

        BlockPos head = BlockPos.ofFloored(
                player.getX(), player.getEyeY(), player.getZ()
        );

        return !world.getFluidState(feet).isEmpty()
                || !world.getFluidState(body).isEmpty()
                || !world.getFluidState(head).isEmpty();
    }

    private static boolean checkDimensionUnlock(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            Identifier dimensionId
    ) {
        NbtCompound nbt = vm.getOrCreateNbt();

        String unlockKey = "DimensionUnlocked_" + dimensionId.getPath();

        if (nbt.getBoolean(unlockKey)) {
            return true;
        }

        if (dimensionId.equals(World.END.getValue())) {
            Identifier dragonAdvancement =
                    new Identifier("minecraft", "end/kill_dragon");

            var advancement =
                    server.getAdvancementLoader().get(dragonAdvancement);

            boolean dragonKilled = advancement != null
                    && player.getAdvancementTracker()
                    .getProgress(advancement).isDone();

            if (dragonKilled) {
                nbt.putBoolean(unlockKey, true);
                return true;
            }

            if (consumeItem(player, net.minecraft.item.Items.CHORUS_FRUIT)) {
                nbt.putBoolean(unlockKey, true);
                player.sendMessage(
                        Text.literal("The End unlocked in this Vortex Manipulator."),
                        true
                );
                return true;
            }

            player.sendMessage(
                    Text.literal(
                            "The End is locked. Use a Chorus Fruit or defeat the Ender Dragon first."
                    ),
                    true
            );
            return false;
        }

        if (dimensionId.equals(World.NETHER.getValue())
                || dimensionId.equals(
                new Identifier("gallifrey", "classic_nether"))) {

            if (consumeItem(player, net.minecraft.item.Items.BLAZE_ROD)) {
                nbt.putBoolean(unlockKey, true);
                player.sendMessage(
                        Text.literal("Dimension unlocked in this Vortex Manipulator."),
                        true
                );
                return true;
            }

            player.sendMessage(
                    Text.literal(
                            "This dimension is locked. A Blaze Rod is required to unlock it."
                    ),
                    true
            );
            return false;
        }

        if (dimensionId.equals(new Identifier("gallifrey", "skaro"))) {
            if (consumeItem(player, GallifreyModItems.DALEKANIUM_INGOT)) {
                nbt.putBoolean(unlockKey, true);
                player.sendMessage(
                        Text.literal("Skaro unlocked in this Vortex Manipulator."),
                        true
                );
                return true;
            }

            player.sendMessage(
                    Text.literal("Skaro is locked. A Dalekanium Ingot is required."),
                    true
            );
            return false;
        }

        if (dimensionId.equals(new Identifier("gallifrey", "mondas"))) {
            if (consumeItem(player, GallifreyModItems.STEEL_INGOT)) {
                nbt.putBoolean(unlockKey, true);
                player.sendMessage(
                        Text.literal("Mondas unlocked in this Vortex Manipulator."),
                        true
                );
                return true;
            }

            player.sendMessage(
                    Text.literal("Mondas is locked. A Steel Ingot is required."),
                    true
            );
            return false;
        }

        return true;
    }

    private static boolean consumeItem(
            ServerPlayerEntity player,
            Item item
    ) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isOf(item)) {
                stack.decrement(1);
                return true;
            }
        }

        return false;
    }

    private static Vec3d findLostRealityArrival(ServerWorld world) {
        for (int radius = 0; radius <= 256; radius += 16) {
            int checks = radius == 0 ? 1 : 8;

            for (int i = 0; i < checks; i++) {
                double angle = (Math.PI * 2.0 * i) / checks;

                int x = radius == 0
                        ? 0
                        : (int) Math.round(Math.cos(angle) * radius);

                int z = radius == 0
                        ? 0
                        : (int) Math.round(Math.sin(angle) * radius);

                Double y = findSafeSurfaceY(world, x, z);

                if (y != null) {
                    return new Vec3d(x + 0.5, y, z + 0.5);
                }
            }
        }

        return null;
    }

    private static Double findSafeSurfaceY(
            ServerWorld world,
            int x,
            int z
    ) {
        world.getChunk(x >> 4, z >> 4);

        int surfaceY = world.getTopY(
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z
        );

        if (surfaceY <= 64) {
            return null;
        }

        return (double) surfaceY;
    }

    private static void saveLocation(
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isAuthorized(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "ISOMORPHIC LOCK: you are not authorized to modify VM memory."
                    ),
                    true
            );
            return;
        }

        String name = buf.readString(32).trim();

        if (name.isEmpty()) {
            player.sendMessage(
                    Text.literal("Location name cannot be empty."), true
            );
            return;
        }

        if (VortexManipulatorData.locations(vm).size() >= 20
                && VortexManipulatorData.findLocation(vm, name) == null) {
            player.sendMessage(
                    Text.literal(
                            "VM memory full: maximum 20 saved locations."
                    ),
                    true
            );
            return;
        }

        VortexManipulatorData.saveLocation(
                vm,
                name,
                player.getServerWorld().getRegistryKey()
                        .getValue().toString(),
                player.getX(),
                player.getY(),
                player.getZ()
        );

        player.sendMessage(
                Text.literal("Saved VM location: " + name), true
        );
        sendState(player.getServer(), player);
    }

    private static void deleteLocation(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isAuthorized(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "ISOMORPHIC LOCK: you are not authorized to modify VM memory."
                    ),
                    true
            );
            return;
        }

        String name = buf.readString(32);

        player.sendMessage(
                Text.literal(
                        VortexManipulatorData.deleteLocation(vm, name)
                                ? "Deleted VM location: " + name
                                : "Location not found: " + name
                ),
                true
        );

        sendState(server, player);
    }

    private static void goLocation(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isAuthorized(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal("ISOMORPHIC LOCK ACTIVE."), true
            );
            return;
        }

        String name = buf.readString(32);
        NbtCompound location =
                VortexManipulatorData.findLocation(vm, name);

        if (location == null) {
            player.sendMessage(
                    Text.literal("Location not found: " + name), true
            );
            return;
        }

        PacketByteBuf fake = PacketByteBufs.create();

        fake.writeBoolean(false);
        fake.writeIdentifier(
                Identifier.tryParse(
                        location.getString(VortexManipulatorData.DIMENSION)
                )
        );
        fake.writeDouble(location.getDouble(VortexManipulatorData.X));
        fake.writeDouble(location.getDouble(VortexManipulatorData.Y));
        fake.writeDouble(location.getDouble(VortexManipulatorData.Z));
        fake.writeBoolean(false);

        teleport(server, player, vm, fake);
    }

    private static void addPlayer(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isOwner(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "Only the VM owner can change isomorphic users."
                    ),
                    true
            );
            return;
        }

        String name = buf.readString(64);
        ServerPlayerEntity target =
                server.getPlayerManager().getPlayer(name);

        if (target == null) {
            player.sendMessage(
                    Text.literal("Player must be online: " + name), true
            );
            return;
        }

        if (VortexManipulatorData.addUser(vm, target.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "Added " + target.getName().getString()
                                    + " to isomorphic controls."
                    ),
                    true
            );
        } else {
            player.sendMessage(
                    Text.literal("Player is already authorized."), true
            );
        }

        sendState(server, player);
    }

    private static void setInboundTeleports(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isOwner(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "Only the VM owner can change inbound teleport protection."
                    ),
                    true
            );
            return;
        }

        boolean blocked = buf.readBoolean();
        VortexManipulatorData.setBlocksInboundTeleports(vm, blocked);

        player.sendMessage(
                Text.literal(
                        blocked
                                ? "Incoming Vortex Manipulator teleports BLOCKED."
                                : "Incoming Vortex Manipulator teleports ALLOWED."
                ),
                true
        );

        sendState(server, player);
    }

    private static void removePlayer(
            MinecraftServer server,
            ServerPlayerEntity player,
            ItemStack vm,
            PacketByteBuf buf
    ) {
        if (!VortexManipulatorData.isOwner(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal(
                            "Only the VM owner can change isomorphic users."
                    ),
                    true
            );
            return;
        }

        String name = buf.readString(64);
        ServerPlayerEntity target =
                server.getPlayerManager().getPlayer(name);

        UUID uuid = target != null
                ? target.getUuid()
                : findUuidByName(vm, name);

        if (uuid != null
                && VortexManipulatorData.removeUser(vm, uuid)) {
            player.sendMessage(
                    Text.literal(
                            "Removed " + name + " from isomorphic controls."
                    ),
                    true
            );
        } else {
            player.sendMessage(
                    Text.literal("Authorized player not found: " + name),
                    true
            );
        }

        sendState(server, player);
    }

    private static UUID findUuidByName(ItemStack vm, String name) {
        for (String id : VortexManipulatorData.userIds(vm)) {
            try {
                UUID uuid = UUID.fromString(id);

                if (id.equalsIgnoreCase(name)) {
                    return uuid;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return null;
    }

    private static void armSelfDestruct(
            ServerPlayerEntity player,
            ItemStack vm
    ) {
        if (!VortexManipulatorData.isOwner(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal("SELF-DESTRUCT requires the VM owner."),
                    true
            );
            return;
        }

        if (SELF_DESTRUCTS.containsKey(player.getUuid())) {
            player.sendMessage(
                    Text.literal("SELF-DESTRUCT is already armed."),
                    true
            );
            return;
        }

        SELF_DESTRUCTS.put(player.getUuid(), 200);

        player.sendMessage(
                Text.literal(
                        "SELF-DESTRUCT ARMED: 10 seconds. Use CANCEL to abort."
                ),
                true
        );

        sendState(player.getServer(), player);
    }

    private static void cancelSelfDestruct(
            ServerPlayerEntity player,
            ItemStack vm
    ) {
        if (!VortexManipulatorData.isOwner(vm, player.getUuid())) {
            player.sendMessage(
                    Text.literal("SELF-DESTRUCT requires the VM owner."),
                    true
            );
            return;
        }

        if (SELF_DESTRUCTS.remove(player.getUuid()) != null) {
            player.sendMessage(
                    Text.literal("SELF-DESTRUCT CANCELLED."), true
            );
        } else {
            player.sendMessage(
                    Text.literal("No self-destruct sequence is armed."),
                    true
            );
        }

        sendState(player.getServer(), player);
    }

    private static void tickSelfDestruct(MinecraftServer server) {
        for (Map.Entry<UUID, Integer> entry : SELF_DESTRUCTS.entrySet()) {
            ServerPlayerEntity player =
                    server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null || player.isRemoved()) {
                SELF_DESTRUCTS.remove(entry.getKey());
                continue;
            }

            int ticks = entry.getValue() - 1;

            if (ticks > 0) {
                SELF_DESTRUCTS.put(entry.getKey(), ticks);

                if (ticks % 20 == 0) {
                    sendState(server, player);
                }

                continue;
            }

            ItemStack vm = findVortexManipulator(player);

            if (!vm.isEmpty()
                    && VortexManipulatorData.isOwner(vm, player.getUuid())) {
                ServerWorld world = player.getServerWorld();

                world.createExplosion(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        4.0F,
                        false,
                        World.ExplosionSourceType.TNT
                );

                vm.decrement(1);

                player.sendMessage(
                        Text.literal("VORTEX MANIPULATOR SELF-DESTRUCT COMPLETE"),
                        true
                );
            }

            SELF_DESTRUCTS.remove(player.getUuid());
            sendState(server, player);
        }
    }

    private static ItemStack findVortexManipulator(
            ServerPlayerEntity player
    ) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);

            if (stack.isOf(GallifreyModItems.VORTEX_MANIPULATOR)) {
                return stack;
            }
        }

        // Worn in a Trinkets glove slot (EMPTY if Trinkets isn't installed).
        return com.cerulixteam.gallifrey.compat.trinkets.TrinketsCompat.findWorn(
                player, GallifreyModItems.VORTEX_MANIPULATOR);
    }

    public static void sendState(
            MinecraftServer server,
            ServerPlayerEntity player
    ) {
        ItemStack vm = findVortexManipulator(player);

        if (vm.isEmpty()) {
            return;
        }

        VortexManipulatorData.ensureOwner(vm, player);

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeBoolean(
                VortexManipulatorData.isOwner(vm, player.getUuid())
        );
        buf.writeVarInt(
                SELF_DESTRUCTS.getOrDefault(player.getUuid(), 0)
        );
        buf.writeBoolean(
                VortexManipulatorData.blocksInboundTeleports(vm)
        );

        NbtList locations = VortexManipulatorData.locations(vm);

        buf.writeVarInt(locations.size());

        for (int i = 0; i < locations.size(); i++) {
            NbtCompound loc = locations.getCompound(i);

            buf.writeString(
                    loc.getString(VortexManipulatorData.NAME), 32
            );
            buf.writeString(
                    loc.getString(VortexManipulatorData.DIMENSION), 128
            );
            buf.writeDouble(
                    loc.getDouble(VortexManipulatorData.X)
            );
            buf.writeDouble(
                    loc.getDouble(VortexManipulatorData.Y)
            );
            buf.writeDouble(
                    loc.getDouble(VortexManipulatorData.Z)
            );
        }

        java.util.List<String> users =
                VortexManipulatorData.userIds(vm);

        buf.writeVarInt(users.size());

        for (String id : users) {
            UUID uuid = UUID.fromString(id);

            ServerPlayerEntity online =
                    player.getServer().getPlayerManager().getPlayer(uuid);

            buf.writeUuid(uuid);
            buf.writeString(
                    online == null ? id : online.getName().getString(),
                    64
            );
        }

        ServerPlayNetworking.send(player, ModPackets.VM_STATE, buf);
    }

    private static void restoreHiddenItems(
            ServerPlayerEntity player,
            PendingTeleportEffect pending
    ) {
        if (!pending.hiddenMainHand().isEmpty()) {
            player.setStackInHand(
                    Hand.MAIN_HAND, pending.hiddenMainHand()
            );
        }

        if (!pending.hiddenOffHand().isEmpty()) {
            player.setStackInHand(
                    Hand.OFF_HAND, pending.hiddenOffHand()
            );
        }

        for (Map.Entry<EquipmentSlot, ItemStack> hidden
                : pending.hiddenArmor().entrySet()) {
            player.equipStack(hidden.getKey(), hidden.getValue());
        }
    }

    private record ChunkPosKey(int x, int z) {}

    private enum TeleportPhase {
        SOURCE_WARMUP,
        PRE_CLOAK,
        TELEPORT,
        TARGET_ARRIVAL
    }

    private record PendingTeleportEffect(
            RegistryKey<World> sourceWorldKey,
            Vec3d sourcePosition,
            RegistryKey<World> targetWorldKey,
            Vec3d targetPosition,
            ChunkPosKey targetChunkPos,
            TeleportPhase phase,
            int ticksRemaining,
            ItemStack hiddenMainHand,
            ItemStack hiddenOffHand,
            Map<EquipmentSlot, ItemStack> hiddenArmor
    ) {
        private PendingTeleportEffect tickDown() {
            return new PendingTeleportEffect(
                    sourceWorldKey,
                    sourcePosition,
                    targetWorldKey,
                    targetPosition,
                    targetChunkPos,
                    phase,
                    ticksRemaining - 1,
                    hiddenMainHand,
                    hiddenOffHand,
                    hiddenArmor
            );
        }

        private PendingTeleportEffect nextPhase(
                TeleportPhase next,
                int ticks
        ) {
            return new PendingTeleportEffect(
                    sourceWorldKey,
                    sourcePosition,
                    targetWorldKey,
                    targetPosition,
                    targetChunkPos,
                    next,
                    ticks,
                    hiddenMainHand,
                    hiddenOffHand,
                    hiddenArmor
            );
        }

        private PendingTeleportEffect cloak(
                ItemStack main,
                ItemStack off,
                Map<EquipmentSlot, ItemStack> armor,
                TeleportPhase next,
                int ticks
        ) {
            return new PendingTeleportEffect(
                    sourceWorldKey,
                    sourcePosition,
                    targetWorldKey,
                    targetPosition,
                    targetChunkPos,
                    next,
                    ticks,
                    main,
                    off,
                    armor
            );
        }
    }
}
