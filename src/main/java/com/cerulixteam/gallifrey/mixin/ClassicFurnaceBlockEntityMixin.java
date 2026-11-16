package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Classic Furnace uses the vanilla furnace block entity. Vanilla only lets that block entity
 * tick (smelt) on minecraft:furnace, so this tells it the Classic Furnace is allowed too.
 */
@Mixin(BlockEntityType.class)
public abstract class ClassicFurnaceBlockEntityMixin {

    @Inject(method = "supports", at = @At("HEAD"), cancellable = true)
    private void gallifrey$supportClassicFurnace(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == BlockEntityType.FURNACE && state.isOf(GallifreyModBlocks.CLASSIC_FURNACE)) {
            cir.setReturnValue(true);
        }
    }
}
