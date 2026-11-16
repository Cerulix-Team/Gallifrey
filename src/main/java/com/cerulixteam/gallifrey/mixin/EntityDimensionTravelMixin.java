package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.util.GallifreyDimensionTravelAccess;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Stops mobs, items and every other non-player entity entering Lost Reality except through the
 * allowed routes (see GallifreyDimensionTravelAccess), so a TARDIS, portal gun or teleporter can't
 * send them there either. Players override both methods and are handled by
 * GallifreyDimensionTravelMixin.
 */
@Mixin(Entity.class)
public abstract class EntityDimensionTravelMixin {

    @Inject(method = "moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$blockWorldTransfer(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (GallifreyDimensionTravelAccess.shouldBlock((Entity) (Object) this, destination)) {
            cir.setReturnValue(null); // vanilla's "didn't move"
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"), cancellable = true)
    private void gallifrey$blockTeleport(ServerWorld destination, double x, double y, double z,
                                         Set<PositionFlag> flags, float yaw, float pitch,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (GallifreyDimensionTravelAccess.shouldBlock((Entity) (Object) this, destination)) {
            cir.setReturnValue(false);
        }
    }
}
