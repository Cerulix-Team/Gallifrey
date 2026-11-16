package com.cerulixteam.gallifrey.mixin.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LightningRodBlock;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lightning powers and un-rusts the backported (rusted and waxed) lightning rods, not just vanilla's. */
@Mixin(LightningEntity.class)
public abstract class LightningRodStrikeMixin {
    @Redirect(method = "powerLightningRod", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean gallifrey$powerAnyRod(BlockState state, Block block) {
        return gallifrey$isRod(state, block);
    }

    @Redirect(method = "cleanOxidation", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean gallifrey$cleanAnyRod(BlockState state, Block block) {
        return gallifrey$isRod(state, block);
    }

    private static boolean gallifrey$isRod(BlockState state, Block block) {
        return state.isOf(block) || (block == net.minecraft.block.Blocks.LIGHTNING_ROD && state.getBlock() instanceof LightningRodBlock);
    }
}
