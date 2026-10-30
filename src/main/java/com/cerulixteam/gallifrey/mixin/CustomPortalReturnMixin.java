package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.NetherOriginHolder;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.util.GallifreyDimensionTravelAccess;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Custom Portal API portals (the White Point Star portals to Gallifrey, Mars, Skaro, Mondas,
 * Pete's World and Lost Reality) always lead back to their return dimension, which is the
 * Overworld unless set. This remembers which dimension each entity came through from and sends
 * it back there instead. So a Gallifrey portal built in Pete's World leads back to Pete's World.
 *
 * Custom Portal API's CustomTeleporter.TPToDim picks the destination in two steps:
 *   destKey = the portal's destination;
 *   if (already in the destination) destKey = the portal's return dimension;
 * The first handler records where the entity came from, the second swaps the return dimension.
 */
@Mixin(value = CustomTeleporter.class, remap = false)
public abstract class CustomPortalReturnMixin {

    /** destKey = destination: remember where the entity is coming from. */
    @ModifyVariable(method = "TPToDim", at = @At(value = "STORE", ordinal = 0), remap = false)
    private static RegistryKey<World> gallifrey$rememberOrigin(RegistryKey<World> destKey, World world, Entity entity,
                                                              Block portalBase, BlockPos portalPos) {
        if (world instanceof ServerWorld && !world.getRegistryKey().equals(destKey)) {
            var origins = ((NetherOriginHolder) entity).gallifrey$getPortalOrigins();
            if (world.getRegistryKey() == World.OVERWORLD
                    || world.getRegistryKey().getValue().equals(GallifreyDimensionTravelAccess.LOST_REALITY)) {
                // The normal way back. Lost Reality is never a way back: only the Lost Dirt portal
                // and the Vortex Manipulator's drift lead there.
                origins.remove(destKey);
            } else {
                origins.put(destKey, world.getRegistryKey());
            }
        }
        return destKey;
    }

    /** destKey = return dimension: send the entity back where it came from, if that world still exists. */
    @ModifyVariable(method = "TPToDim", at = @At(value = "STORE", ordinal = 1), remap = false)
    private static RegistryKey<World> gallifrey$returnToOrigin(RegistryKey<World> destKey, World world, Entity entity,
                                                              Block portalBase, BlockPos portalPos) {
        if (!(world instanceof ServerWorld serverWorld)) return destKey;
        RegistryKey<World> origin = ((NetherOriginHolder) entity).gallifrey$getPortalOrigins().get(world.getRegistryKey());
        if (origin != null && serverWorld.getServer().getWorld(origin) != null) {
            return origin;
        }
        return destKey;
    }

    /**
     * The Lost Dirt portal is one of the only ways into Lost Reality (see
     * GallifreyDimensionTravelAccess): authorize whatever goes through it, players, mobs or items.
     */
    @Inject(
            method = "TPToDim",
            at = @At("HEAD"),
            remap = false
    )
    private static void gallifrey$authorizeLostRealityPortal(
            World world,
            Entity entity,
            Block portalBase,
            BlockPos portalPos,
            CallbackInfo ci
    ) {
        if (world instanceof ServerWorld && entity != null && portalBase == GallifreyModBlocks.LOST_DIRT) {
            GallifreyDimensionTravelAccess.beginAuthorization(entity);
        }
    }

    /**
     * Remove the portal authorization after Custom Portal API finishes.
     */
    @Inject(
            method = "TPToDim",
            at = @At("RETURN"),
            remap = false
    )
    private static void gallifrey$endLostRealityPortalAuthorization(
            World world,
            Entity entity,
            Block portalBase,
            BlockPos portalPos,
            CallbackInfo ci
    ) {
        if (world instanceof ServerWorld && entity != null && portalBase == GallifreyModBlocks.LOST_DIRT) {
            GallifreyDimensionTravelAccess.endAuthorization(entity);
        }
    }
}
