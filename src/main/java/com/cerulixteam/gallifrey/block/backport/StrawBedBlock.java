package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Straw Bed (26.3): sleep in it once and it's gone. It doesn't set your spawn point, and in the Nether or
 * the End it just falls apart instead of exploding. (Backport.register() hooks up the spawn and
 * "gone after one use" parts through Fabric's sleep events.)
 */
public class StrawBedBlock extends BedBlock {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

    public StrawBedBlock(Settings settings) {
        super(DyeColor.YELLOW, settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient && !BedBlock.isBedWorking(world)) {
            BlockPos head = state.get(PART) == BedPart.HEAD ? pos : pos.offset(state.get(FACING));
            world.breakBlock(head, false);
            player.sendMessage(Text.translatable("block.minecraft.bed.no_sleep"), true);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    /** Breaks the bed someone just got out of (both halves go, since a bed can't stand without its other half). */
    public static void used(World world, BlockPos sleepingPos) {
        if (!world.isClient && world.getBlockState(sleepingPos).getBlock() instanceof StrawBedBlock) {
            world.breakBlock(sleepingPos, false);
        }
    }
}
