package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.PaleMossCarpetBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.SimpleBlockFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Like 1.21.4's SimpleBlockFeature: when worldgen places a pale moss carpet it uses the carpet's own
 * placeAt, so it grows up the sides of nearby blocks and sometimes gets a second layer on top.
 */
@Mixin(SimpleBlockFeature.class)
public abstract class SimpleBlockFeaturePaleMossMixin {
    @Redirect(method = "generate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/StructureWorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean gallifrey$placePaleMossCarpet(StructureWorldAccess world, BlockPos pos, BlockState state, int flags) {
        if (state.getBlock() instanceof PaleMossCarpetBlock) {
            PaleMossCarpetBlock.placeAt(world, pos, world.getRandom(), flags);
            return true;
        }
        return world.setBlockState(pos, state, flags);
    }
}
