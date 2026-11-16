package com.cerulixteam.gallifrey.tardis;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class TardisCommands {
    private TardisCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tardis")
                .then(CommandManager.literal("exit")
                        .executes(ctx -> exit(ctx.getSource())))
                .then(CommandManager.literal("travel")
                        .then(CommandManager.argument("dimension", StringArgumentType.word())
                                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> travel(
                                                                ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "dimension"),
                                                                DoubleArgumentType.getDouble(ctx, "x"),
                                                                DoubleArgumentType.getDouble(ctx, "y"),
                                                                DoubleArgumentType.getDouble(ctx, "z"))))))))
                .then(CommandManager.literal("idfinder")
                        .executes(ctx -> idFinder(ctx.getSource())))
                .then(CommandManager.literal("IdFinder")
                        .executes(ctx -> idFinder(ctx.getSource())))
                .then(CommandManager.literal("exterior")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> exterior(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
                .then(CommandManager.literal("antigrav")
                        .executes(ctx -> antigrav(ctx.getSource())))
                .then(CommandManager.literal("rwf")
                        .executes(ctx -> rwf(ctx.getSource())))
                .then(CommandManager.literal("selfdestruct")
                        .executes(ctx -> selfDestruct(ctx.getSource(), false))
                        .then(CommandManager.literal("cancel")
                                .executes(ctx -> selfDestruct(ctx.getSource(), true))))
                .then(CommandManager.literal("lock")
                        .executes(ctx -> setLock(ctx.getSource(), true)))
                .then(CommandManager.literal("unlock")
                        .executes(ctx -> setLock(ctx.getSource(), false)))
                .then(CommandManager.literal("refuel")
                        .executes(ctx -> refuel(ctx.getSource())))
                .then(CommandManager.literal("info")
                        .executes(ctx -> info(ctx.getSource())))
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("tardisId", StringArgumentType.word())
                                .executes(ctx -> delete(ctx.getSource(), StringArgumentType.getString(ctx, "tardisId")))))
                .then(CommandManager.literal("interiors")
                        .executes(ctx -> interiors(ctx.getSource())))
                .then(CommandManager.literal("interior")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> interior(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))));
    }

    private static int exit(ServerCommandSource source) {
        try {
            return TardisDimensionManager.exit(source.getPlayerOrThrow()) ? 1 : 0;
        } catch (Exception e) {
            source.sendError(Text.literal("Only players can use TARDIS controls."));
            return 0;
        }
    }

    private static int travel(ServerCommandSource source, String dimension, double x, double y, double z) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!requireOwner(source, player)) return 0;
            Identifier id = new Identifier(dimension);
            ServerWorld world = player.getServer().getWorld(
                    net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, id));
            if (world == null) {
                source.sendError(Text.literal("Unknown dimension: " + id));
                return 0;
            }
            int minY = world.getBottomY();
            int maxY = world.getTopY() - 2;
            if (y < minY || y > maxY) {
                source.sendError(Text.literal("Y must be between " + minY + " and " + maxY + "."));
                return 0;
            }
            return TardisDimensionManager.travel(player, world,
                    BlockPos.ofFloored(x, y, z), player.getYaw()) ? 1 : 0;
        } catch (Exception e) {
            source.sendError(Text.literal("Invalid TARDIS destination."));
            return 0;
        }
    }

    private static int setLock(ServerCommandSource source, boolean locked) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            if (id == null || state.get(id) == null) {
                source.sendError(Text.literal("You are not linked to a TARDIS."));
                return 0;
            }
            TardisRegistryState.Record record = state.get(id);
            if (record.owner() == null || !record.owner().equals(player.getUuid())) {
                source.sendError(Text.literal("Only the TARDIS owner can change its lock."));
                return 0;
            }
            ServerWorld world = player.getServer().getWorld(
                    net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            if (world == null || !(world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis)) {
                source.sendError(Text.literal("TARDIS exterior is unavailable."));
                return 0;
            }
            tardis.setLocked(locked);
            source.sendFeedback(() -> Text.literal(locked ? "TARDIS locked." : "TARDIS unlocked."), true);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int refuel(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            if (id == null || state.get(id) == null) {
                source.sendError(Text.literal("Enter your TARDIS before refuelling it."));
                return 0;
            }
            TardisRegistryState.Record record = state.get(id);
            if (record.owner() == null || !record.owner().equals(player.getUuid())) {
                source.sendError(Text.literal("Only the owner can refuel the TARDIS."));
                return 0;
            }
            ServerWorld world = player.getServer().getWorld(
                    net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            if (world == null || !(world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis)) {
                source.sendError(Text.literal("TARDIS exterior is unavailable."));
                return 0;
            }

            int consumed = 0;
            int added = 0;
            for (int slot = 0; slot < player.getInventory().size() && tardis.getFuel() < com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity.MAX_FUEL; slot++) {
                net.minecraft.item.ItemStack stack = player.getInventory().getStack(slot);
                if (!stack.isOf(GallifreyModItems.ATRIUM_FUEL)) continue;
                stack.decrement(1);
                int before = tardis.getFuel();
                added += tardis.addFuel(100);
                consumed++;
            }
            if (consumed == 0) {
                source.sendError(Text.literal("You need Atrium Fuel."));
                return 0;
            }
            int finalAdded = added;
            source.sendFeedback(() -> Text.literal("Refuelled TARDIS: +" + finalAdded + " artron energy (" +
                    tardis.getFuel() + "/" + com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity.MAX_FUEL + ")."), true);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static int delete(ServerCommandSource source, String rawId) {
        try {
            java.util.UUID id = java.util.UUID.fromString(rawId);
            net.minecraft.server.MinecraftServer server = source.getServer();
            TardisRegistryState state = TardisRegistryState.get(server);
            TardisRegistryState.Record record = state.get(id);
            if (record == null) {
                source.sendError(Text.literal("No TARDIS exists with ID " + rawId + "."));
                return 0;
            }

            // Owners may delete their own TARDIS. Server operators may delete
            // any TARDIS so administrators can clean up abandoned worlds.
            ServerPlayerEntity player = source.getPlayer();
            boolean allowed = source.hasPermissionLevel(2)
                    || (player != null && record.owner() != null && record.owner().equals(player.getUuid()));
            if (!allowed) {
                source.sendError(Text.literal("Only the TARDIS owner or a server operator can delete that TARDIS."));
                return 0;
            }

            if (!TardisDimensionManager.deleteTardis(server, id)) {
                source.sendError(Text.literal("The TARDIS could not be deleted."));
                return 0;
            }
            source.sendFeedback(() -> Text.literal("Deleted TARDIS " + id + "."), true);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal("Invalid TARDIS ID. Use the UUID shown by /tardis info."));
            return 0;
        }
    }

    private static int interiors(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("Available TARDIS interiors: " + String.join(", ", TardisInteriorCatalog.names())), false);
        return 1;
    }

    private static int interior(ServerCommandSource source, String name) {
        try {
            return TardisDimensionManager.swapInterior(source.getPlayerOrThrow(), name) ? 1 : 0;
        } catch (Exception e) {
            source.sendError(Text.literal("Only players can change TARDIS interiors."));
            return 0;
        }
    }

    private static int info(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!requireOwner(source, player)) return 0;
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            if (id == null || state.get(id) == null) {
                source.sendFeedback(() -> Text.literal("No active TARDIS link."), false);
                return 1;
            }
            TardisRegistryState.Record record = state.get(id);
            ServerWorld world = player.getServer().getWorld(
                    net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            int fuel = -1;
            boolean locked = record.locked();
            if (world != null && world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis) {
                fuel = tardis.getFuel();
                locked = tardis.isLocked();
            }
            int shownFuel = fuel;
            boolean shownLocked = locked;
            source.sendFeedback(() -> Text.literal("TARDIS " + id + " | exterior " + record.world() +
                    " @ " + pos.toShortString() + " | pocket " + record.interiorDimension() +
                    " | fuel " + shownFuel + "/" +
                    com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity.MAX_FUEL +
                    " | " + (shownLocked ? "LOCKED" : "UNLOCKED")), false);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    private static boolean requireOwner(ServerCommandSource source, ServerPlayerEntity player) {
        TardisRegistryState state = TardisRegistryState.get(player.getServer());
        java.util.UUID id = state.getActiveTardis(player.getUuid());
        TardisRegistryState.Record record = id == null ? null : state.get(id);
        if (record == null || record.owner() == null || !record.owner().equals(player.getUuid())) {
            source.sendError(Text.literal("Only the owner of this TARDIS can use that command."));
            return false;
        }
        return true;
    }

    private static int idFinder(ServerCommandSource source) {
        if (source.getServer() == null) return 0;
        TardisRegistryState state = TardisRegistryState.get(source.getServer());
        if (state.records().isEmpty()) {
            source.sendFeedback(() -> Text.literal("No TARDISes are registered."), false);
            return 1;
        }
        for (TardisRegistryState.Record record : state.records()) {
            String owner = record.owner() == null ? "unowned" : record.owner().toString();
            source.sendFeedback(() -> Text.literal(record.id() + " | owner=" + owner + " | " + record.world() + " @ " + BlockPos.fromLong(record.pos()).toShortString()), false);
        }
        return state.records().size();
    }

    private static int exterior(ServerCommandSource source, String name) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!requireOwner(source, player)) return 0;
            if (!TardisExteriorCatalog.contains(name)) {
                source.sendError(Text.literal("Unknown exterior. Available: " + String.join(", ", TardisExteriorCatalog.names())));
                return 0;
            }
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            TardisRegistryState.Record record = state.get(id);
            ServerWorld world = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            if (world == null || !(world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis)) {
                source.sendError(Text.literal("TARDIS exterior is unavailable."));
                return 0;
            }
            if (tardis.isFlightPending()) {
                source.sendError(Text.literal("You cannot change the exterior during flight."));
                return 0;
            }
            tardis.setExteriorStyle(name);
            source.sendFeedback(() -> Text.literal("TARDIS exterior changed to " + tardis.getExteriorStyleName() + "."), true);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Only a TARDIS owner can change the exterior."));
            return 0;
        }
    }

    private static int rwf(ServerCommandSource source) {
        try {
            return TardisDimensionManager.toggleRealWorldFlight(source.getPlayerOrThrow()) ? 1 : 0;
        } catch (Exception e) {
            source.sendError(Text.literal("Only players can use Real World Flight."));
            return 0;
        }
    }

    private static int antigrav(ServerCommandSource source) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!requireOwner(source, player)) return 0;
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            TardisRegistryState.Record record = state.get(id);
            ServerWorld world = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            if (world == null || !(world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis)) return 0;
            tardis.setAntigravityEnabled(!tardis.isAntigravityEnabled());
            source.sendFeedback(() -> Text.literal("Antigravity " + (tardis.isAntigravityEnabled() ? "enabled" : "disabled") + "."), true);
            return 1;
        } catch (Exception e) { return 0; }
    }

    private static int selfDestruct(ServerCommandSource source, boolean cancel) {
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            if (!requireOwner(source, player)) return 0;
            TardisRegistryState state = TardisRegistryState.get(player.getServer());
            java.util.UUID id = state.getActiveTardis(player.getUuid());
            TardisRegistryState.Record record = state.get(id);
            ServerWorld world = player.getServer().getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, new Identifier(record.world())));
            BlockPos pos = BlockPos.fromLong(record.pos());
            if (world == null || !(world.getBlockEntity(pos) instanceof com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity tardis)) return 0;
            if (cancel) {
                tardis.cancelSelfDestruct();
                source.sendFeedback(() -> Text.literal("TARDIS self-destruct cancelled."), true);
            } else {
                tardis.armSelfDestruct();
                source.sendFeedback(() -> Text.literal("TARDIS self-destruct armed. 10 seconds."), true);
            }
            return 1;
        } catch (Exception e) { return 0; }
    }

}
