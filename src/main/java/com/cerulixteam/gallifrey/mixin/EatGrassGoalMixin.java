package com.cerulixteam.gallifrey.mixin;

import com.cerulixteam.gallifrey.block.custom.SpreadingGrassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets sheep graze on classic and wasted grass the same way they graze on vanilla grass. */
@Mixin(EatGrassGoal.class)
public abstract class EatGrassGoalMixin extends Goal {
    @Shadow @Final private MobEntity mob;
    @Shadow @Final private World world;
    @Shadow private int timer;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void gallifrey$startOnModGrass(CallbackInfoReturnable<Boolean> cir) {
        BlockPos below = this.mob.getBlockPos().down();
        if (this.world.getBlockState(below).getBlock() instanceof SpreadingGrassBlock
                && this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) == 0) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void gallifrey$eatModGrass(CallbackInfo ci) {
        if (this.timer != this.getTickCount(4)) {
            return;
        }
        BlockPos below = this.mob.getBlockPos().down();
        BlockState state = this.world.getBlockState(below);
        if (state.getBlock() instanceof SpreadingGrassBlock grass) {
            if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                this.world.syncWorldEvent(2001, below, Block.getRawIdFromState(state));
                this.world.setBlockState(below, grass.getDirt().getDefaultState(), Block.NOTIFY_LISTENERS);
            }
            this.mob.onEatingGrass();
        }
    }
}
