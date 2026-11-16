package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Datapack dimensions can't set their own seed in 1.20.1, so every dimension
 * generates from the world seed. Pete's World and Lost Reality need different
 * terrain, so this offsets the seed they report. Terrain, biomes, structures,
 * decoration and slime chunks all read the seed through this method.
 *
 * The result is derived from the world seed, so it is stable per world and
 * different between worlds.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldSeedMixin {
    // "PetePete" in ASCII.
    private static final long PETES_WORLD_SEED_SALT = 0x5065746550657465L;
    // "LostReal" in ASCII.
    private static final long LOST_REALITY_SEED_SALT = 0x4C6F73745265616CL;

    @Inject(method = "getSeed", at = @At("RETURN"), cancellable = true)
    private void gallifrey$offsetDimensionSeed(CallbackInfoReturnable<Long> cir) {
        ServerWorld self = (ServerWorld) (Object) this;
        if (ModDimensions.PETES_WORLD_LEVEL_KEY.equals(self.getRegistryKey())) {
            cir.setReturnValue(cir.getReturnValue() ^ PETES_WORLD_SEED_SALT);
        } else if (ModDimensions.LOST_REALITY_LEVEL_KEY.equals(self.getRegistryKey())) {
            cir.setReturnValue(cir.getReturnValue() ^ LOST_REALITY_SEED_SALT);
        }
    }
}
