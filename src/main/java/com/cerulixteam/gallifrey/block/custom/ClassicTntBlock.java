package com.cerulixteam.gallifrey.block.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Classic TNT with explicit support for the mod's Classic Flint and Steel. */
public class ClassicTntBlock extends TntBlock {
    public ClassicTntBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(GallifreyModItems.CLASSIC_FLINT_AND_STEEL)) {
            if (!world.isClient) {
                // Create the primed TNT directly; TntBlock.prime is not accessible in this Yarn version.
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                TntEntity primedTnt = new TntEntity(
                    world,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    player
                );
                world.spawnEntity(primedTnt);
                world.playSound(
                    null,
                    pos,
                    SoundEvents.ENTITY_TNT_PRIMED,
                    SoundCategory.BLOCKS,
                    1.0F,
                    1.0F
                );
                if (!player.getAbilities().creativeMode) {
                    stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                }
            }
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
