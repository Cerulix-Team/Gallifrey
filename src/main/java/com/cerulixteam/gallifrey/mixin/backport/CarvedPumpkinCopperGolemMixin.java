package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.mob.BackportMobs;
import com.cerulixteam.gallifrey.block.backport.mob.CopperGolemEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Copper Golem (1.21.9): a carved pumpkin placed on a block of copper (any rust, waxed or not) builds one. */
@Mixin(CarvedPumpkinBlock.class)
public abstract class CarvedPumpkinCopperGolemMixin {
    @Inject(method = "trySpawnEntity", at = @At("HEAD"), cancellable = true)
    private void gallifrey$copperGolem(World world, BlockPos pos, CallbackInfo ci) {
        BlockState below = world.getBlockState(pos.down());
        int oxidation = gallifrey$oxidation(below);
        if (oxidation < 0) return;
        CopperGolemEntity golem = BackportMobs.COPPER_GOLEM.create(world);
        if (golem == null) return;
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pos.down(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(below));
        golem.setOxidation(oxidation & 3);
        golem.setWaxed(oxidation > 3);
        golem.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
        world.spawnEntity(golem);
        for (ServerPlayerEntity player : world.getNonSpectatingEntities(ServerPlayerEntity.class, golem.getBoundingBox().expand(5.0))) {
            Criteria.SUMMONED_ENTITY.trigger(player, golem);
        }
        world.updateNeighbors(pos, Blocks.AIR);
        world.updateNeighbors(pos.down(), Blocks.AIR);
        ci.cancel();
    }

    /** 0 to 3 for the rust level, plus 4 if waxed; -1 if it isn't a block of copper. */
    private static int gallifrey$oxidation(BlockState state) {
        Block b = state.getBlock();
        if (b == Blocks.COPPER_BLOCK) return 0;
        if (b == Blocks.EXPOSED_COPPER) return 1;
        if (b == Blocks.WEATHERED_COPPER) return 2;
        if (b == Blocks.OXIDIZED_COPPER) return 3;
        if (b == Blocks.WAXED_COPPER_BLOCK) return 4;
        if (b == Blocks.WAXED_EXPOSED_COPPER) return 5;
        if (b == Blocks.WAXED_WEATHERED_COPPER) return 6;
        if (b == Blocks.WAXED_OXIDIZED_COPPER) return 7;
        return -1;
    }
}
