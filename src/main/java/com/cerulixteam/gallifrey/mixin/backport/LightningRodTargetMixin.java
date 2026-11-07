package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.BackportBlocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Lightning is drawn to the backported lightning rods too (their own point of interest, see BackportBlocks). */
@Mixin(ServerWorld.class)
public abstract class LightningRodTargetMixin {
    @Inject(method = "getLightningRodPos", at = @At("RETURN"), cancellable = true)
    private void gallifrey$backportRods(BlockPos pos, CallbackInfoReturnable<Optional<BlockPos>> cir) {
        if (cir.getReturnValue().isPresent()) return;
        ServerWorld self = (ServerWorld) (Object) this;
        Optional<BlockPos> rod = self.getPointOfInterestStorage().getNearestPosition(type -> type.matchesKey(BackportBlocks.LIGHTNING_ROD_POI),
                p -> p.getY() == self.getTopY(Heightmap.Type.WORLD_SURFACE, p.getX(), p.getZ()) - 1, pos, 128, PointOfInterestStorage.OccupationStatus.ANY);
        cir.setReturnValue(rod.map(BlockPos::up));
    }
}
