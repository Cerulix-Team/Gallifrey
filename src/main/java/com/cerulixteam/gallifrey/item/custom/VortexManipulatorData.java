package com.cerulixteam.gallifrey.item.custom;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persistent per-Vortex-Manipulator data: owner, isomorphic users and saved locations. */
public final class VortexManipulatorData {
    public static final String OWNER = "VMOwner";
    public static final String USERS = "VMUsers";
    public static final String LOCATIONS = "VMLocations";
    public static final String BLOCK_INBOUND_TELEPORTS = "VMBlockInboundTeleports";
    public static final String NAME = "Name";
    public static final String DIMENSION = "Dimension";
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    private VortexManipulatorData() {}

    public static NbtCompound nbt(ItemStack stack) {
        return stack.getOrCreateNbt();
    }

    public static void ensureOwner(ItemStack stack, ServerPlayerEntity player) {
        NbtCompound nbt = nbt(stack);
        if (!nbt.containsUuid(OWNER)) {
            nbt.putUuid(OWNER, player.getUuid());
            nbt.put(USERS, new NbtList());
        }
    }

    public static boolean isOwner(ItemStack stack, UUID uuid) {
        return nbt(stack).containsUuid(OWNER) && nbt(stack).getUuid(OWNER).equals(uuid);
    }

    public static boolean isAuthorized(ItemStack stack, UUID uuid) {
        if (isOwner(stack, uuid)) return true;
        NbtList list = nbt(stack).getList(USERS, NbtElement.STRING_TYPE);
        String id = uuid.toString();
        for (int i = 0; i < list.size(); i++) if (list.getString(i).equals(id)) return true;
        return false;
    }

    public static boolean addUser(ItemStack stack, UUID uuid) {
        NbtList list = nbt(stack).getList(USERS, NbtElement.STRING_TYPE);
        String id = uuid.toString();
        for (int i = 0; i < list.size(); i++) if (list.getString(i).equals(id)) return false;
        list.add(NbtString.of(id));
        nbt(stack).put(USERS, list);
        return true;
    }

    public static boolean removeUser(ItemStack stack, UUID uuid) {
        if (isOwner(stack, uuid)) return false;
        NbtList list = nbt(stack).getList(USERS, NbtElement.STRING_TYPE);
        String id = uuid.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(id)) {
                list.remove(i);
                nbt(stack).put(USERS, list);
                return true;
            }
        }
        return false;
    }

    public static boolean blocksInboundTeleports(ItemStack stack) {
        return nbt(stack).getBoolean(BLOCK_INBOUND_TELEPORTS);
    }

    public static void setBlocksInboundTeleports(ItemStack stack, boolean blocked) {
        nbt(stack).putBoolean(BLOCK_INBOUND_TELEPORTS, blocked);
    }

    public static List<String> userIds(ItemStack stack) {
        List<String> result = new ArrayList<>();
        NbtList list = nbt(stack).getList(USERS, NbtElement.STRING_TYPE);
        for (int i = 0; i < list.size(); i++) result.add(list.getString(i));
        return result;
    }

    public static void saveLocation(ItemStack stack, String name, String dimension, double x, double y, double z) {
        NbtList locations = nbt(stack).getList(LOCATIONS, 10);
        for (int i = locations.size() - 1; i >= 0; i--) {
            NbtCompound old = locations.getCompound(i);
            if (old.getString(NAME).equalsIgnoreCase(name)) locations.remove(i);
        }
        NbtCompound location = new NbtCompound();
        location.putString(NAME, name);
        location.putString(DIMENSION, dimension);
        location.putDouble(X, x);
        location.putDouble(Y, y);
        location.putDouble(Z, z);
        locations.add(location);
        nbt(stack).put(LOCATIONS, locations);
    }

    public static boolean deleteLocation(ItemStack stack, String name) {
        NbtList locations = nbt(stack).getList(LOCATIONS, 10);
        for (int i = 0; i < locations.size(); i++) {
            if (locations.getCompound(i).getString(NAME).equalsIgnoreCase(name)) {
                locations.remove(i);
                nbt(stack).put(LOCATIONS, locations);
                return true;
            }
        }
        return false;
    }

    public static NbtCompound findLocation(ItemStack stack, String name) {
        NbtList locations = nbt(stack).getList(LOCATIONS, 10);
        for (int i = 0; i < locations.size(); i++) {
            NbtCompound location = locations.getCompound(i);
            if (location.getString(NAME).equalsIgnoreCase(name)) return location.copy();
        }
        return null;
    }

    public static NbtList locations(ItemStack stack) {
        NbtList source = nbt(stack).getList(LOCATIONS, 10);
        NbtList copy = new NbtList();
        for (int i = 0; i < source.size(); i++) copy.add(source.getCompound(i).copy());
        return copy;
    }
}
