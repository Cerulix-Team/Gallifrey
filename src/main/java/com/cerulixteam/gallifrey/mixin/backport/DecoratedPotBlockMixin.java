package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.PotStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.DecoratedPotBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

/** Lets you put items in a decorated pot, and spills them (or its loot) when it breaks. */
@Mixin(DecoratedPotBlock.class)
public abstract class DecoratedPotBlockMixin extends net.minecraft.block.BlockWithEntity {
    protected DecoratedPotBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PotStorage pot) return PotStorage.use(pot, be, world, pos, player, player.getStackInHand(hand));
        return ActionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PotStorage pot && !world.isClient) {
                PotStorage.fill(pot, world, pos);
                if (!pot.gallifrey$getStack().isEmpty()) ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), pot.gallifrey$getStack());
                pot.gallifrey$setStack(net.minecraft.item.ItemStack.EMPTY);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
