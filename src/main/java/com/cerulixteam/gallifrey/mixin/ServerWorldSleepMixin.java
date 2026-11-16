package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.world.dimension.ModDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Lets sleeping in a bed skip the night from dimensions other than the Overworld.
 *
 * In vanilla only the Overworld owns the clock. Every other dimension reads the
 * Overworld's time but is not allowed to change it, so when everyone sleeps
 * there the game wakes them up again without the night ever passing.
 *
 * This runs at the exact moment vanilla tries (and fails) to move the clock
 * forward after a successful sleep, and moves the Overworld's clock instead.
 * All dimensions share that clock, so the night passes everywhere.
 *
 * Dimensions listed in NO_NIGHT_SKIP are left alone: sleeping there does not
 * skip the night.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldSleepMixin {

    /** Dimensions where a bed must NOT skip the night. */
    private static final Set<RegistryKey<World>> NO_NIGHT_SKIP = Set.of(
            ModDimensions.MONDAS_LEVEL_KEY,
            ModDimensions.SKARO_LEVEL_KEY,
            ModDimensions.CLASSIC_NETHER_LEVEL_KEY
    );

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V")
    )
    private void gallifrey$skipNightInOtherDimensions(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerWorld self = (ServerWorld) (Object) this;
        RegistryKey<World> dimension = self.getRegistryKey();

        // The Overworld already works; vanilla handles it right after this.
        if (World.OVERWORLD.equals(dimension) || NO_NIGHT_SKIP.contains(dimension)) {
            return;
        }

        ServerWorld overworld = self.getServer().getOverworld();

        // Same sum vanilla does: jump to the start of the next day.
        long nextDay = overworld.getTimeOfDay() + 24000L;
        overworld.setTimeOfDay(nextDay - nextDay % 24000L);

        // Sleeping also clears rain and thunder, exactly like in the Overworld.
        if (self.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE) && overworld.isRaining()) {
            overworld.setWeather(0, 0, false, false);
        }
    }
}
