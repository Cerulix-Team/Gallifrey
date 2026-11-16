package com.cerulixteam.gallifrey.compat.claims;

import com.mojang.authlib.GameProfile;
import com.cerulixteam.gallifrey.GallifreyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

/**
 * Land claim protection through Patbox's Common Protection API (1.0.0, the version bundled with
 * the 1.20.1 releases of Flan, GOML ReServed, Open Parties and Claims' Fabric compat, Ledger and
 * others). Any claim mod that reports to it is respected:
 *
 *  - Vortex Manipulator: you can't jump into land where you aren't allowed to use blocks
 *    (VMPacket.teleport). That covers coordinate jumps and following another player into a claim.
 *  - Sonic Screwdriver and Sonic Shades: no effect on blocks or creatures in someone else's claim
 *    (SonicScrewdriver.activate). Your own and trusted claims work as normal.
 *
 * Without a claims mod everything is allowed. No compile-time dependency: the API is called by
 * reflection, and if anything about it goes wrong the action is allowed rather than crashing.
 * Operators in creative are never blocked by the claim mods themselves, so that's left to them.
 */
public final class ClaimsCompat {
    private ClaimsCompat() {}

    public static final String MOD_ID = "common-protection-api";

    private static Method canInteractBlock;   // (World, BlockPos, GameProfile, PlayerEntity)
    private static Method canInteractEntity;  // (World, Entity, GameProfile, PlayerEntity)

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;
        try {
            Class<?> api = Class.forName("eu.pb4.common.protection.api.CommonProtection");
            canInteractBlock = api.getMethod("canInteractBlock", World.class, BlockPos.class, GameProfile.class, PlayerEntity.class);
            canInteractEntity = api.getMethod("canInteractEntity", World.class, Entity.class, GameProfile.class, PlayerEntity.class);
            GallifreyMod.LOGGER.info("[Gallifrey] Common Protection API found: the Vortex Manipulator and sonic respect land claims");
        } catch (ReflectiveOperationException e) {
            canInteractBlock = null;
            canInteractEntity = null;
            GallifreyMod.LOGGER.warn("[Gallifrey] Common Protection API is installed but its methods have changed; claim checks are off", e);
        }
    }

    /** May this player use blocks at pos? True if no claims mod is installed. */
    public static boolean canUseBlock(PlayerEntity player, World world, BlockPos pos) {
        if (canInteractBlock == null) return true;
        try {
            return (Boolean) canInteractBlock.invoke(null, world, pos, player.getGameProfile(), player);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return true;
        }
    }

    /** May this player interact with this entity? True if no claims mod is installed. */
    public static boolean canUseOnEntity(PlayerEntity player, Entity entity) {
        if (canInteractEntity == null) return true;
        try {
            return (Boolean) canInteractEntity.invoke(null, entity.getWorld(), entity, player.getGameProfile(), player);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return true;
        }
    }
}
