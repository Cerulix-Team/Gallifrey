package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.util.GallifreyDimensionTravelAccess;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Stops players entering Lost Reality except through the allowed routes
 * (see GallifreyDimensionTravelAccess). These are the three ways a player can change dimension in
 * 1.20.1, and every mod's TARDIS, portal gun or teleporter ends up calling one of them:
 *  - moveToWorld: portals, and Fabric's FabricDimensions.teleport
 *  - teleport(world, x, y, z, flags, yaw, pitch): /tp and many mods (blocked before it gets the
 *    player off their mount or out of bed)
 *  - teleport(world, x, y, z, yaw, pitch): what the one above ends up calling, and many mods
 */
@Mixin(ServerPlayerEntity.class)
public abstract class GallifreyDimensionTravelMixin {

    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$blockWorldTransfer(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (GallifreyDimensionTravelAccess.shouldBlock((Entity) (Object) this, destination)) {
            cir.setReturnValue((Entity) (Object) this);
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$blockTeleportWithFlags(ServerWorld destination, double x, double y, double z,
                                                  Set<PositionFlag> flags, float yaw, float pitch,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (GallifreyDimensionTravelAccess.shouldBlock((Entity) (Object) this, destination)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$blockTeleport(ServerWorld destination, double x, double y, double z,
                                         float yaw, float pitch, CallbackInfo ci) {
        if (GallifreyDimensionTravelAccess.shouldBlock((Entity) (Object) this, destination)) {
            ci.cancel();
        }
    }
}
