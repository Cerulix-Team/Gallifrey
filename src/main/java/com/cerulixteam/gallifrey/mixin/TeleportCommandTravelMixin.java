package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.util.GallifreyDimensionTravelAccess;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

/**
 * Lets operators still use /tp (and /execute in gallifrey:lost_reality run tp ...) to reach Lost
 * Reality, for testing and moderation. The authorization only covers the teleport call itself.
 * Remove this mixin from gallifrey.mixins.json to block commands too.
 */
@Mixin(TeleportCommand.class)
public abstract class TeleportCommandTravelMixin {

    @Redirect(method = "teleport",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z"))
    private static boolean gallifrey$allowCommandTeleport(Entity target, ServerWorld world, double x, double y, double z,
                                                          Set<PositionFlag> flags, float yaw, float pitch) {
        GallifreyDimensionTravelAccess.beginAuthorization(target);
        try {
            return target.teleport(world, x, y, z, flags, yaw, pitch);
        } finally {
            GallifreyDimensionTravelAccess.endAuthorization(target);
        }
    }
}
