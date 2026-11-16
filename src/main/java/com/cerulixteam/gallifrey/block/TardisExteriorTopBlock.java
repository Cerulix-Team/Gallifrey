package com.cerulixteam.gallifrey.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * The invisible upper half of the police box. The box is two blocks tall, but the game only lets you
 * click (and target) the block space your crosshair is in, so without this the top half could not be
 * clicked at all. Everything here is handed down to the TARDIS exterior block underneath.
 *
 * It is placed and removed automatically with the exterior (see TardisExteriorBlock.onBlockAdded) and
 * has no item.
 */
public class TardisExteriorTopBlock extends Block {
    public TardisExteriorTopBlock(Settings settings) {
        super(settings);
    }

    private static boolean hasBase(BlockView world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() instanceof TardisExteriorBlock;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // The exterior's block entity renderer draws the whole box.
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!hasBase(world, pos)) return VoxelShapes.empty();
        BlockState base = world.getBlockState(pos.down());
        return base.getOutlineShape(world, pos.down(), context).offset(0, -1, 0);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!hasBase(world, pos)) return VoxelShapes.empty();
        BlockState base = world.getBlockState(pos.down());
        return base.getCollisionShape(world, pos.down(), context).offset(0, -1, 0);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!hasBase(world, pos)) return ActionResult.PASS;
        BlockPos base = pos.down();
        return world.getBlockState(base).onUse(world, player, hand,
                new BlockHitResult(hit.getPos(), hit.getSide(), base, hit.isInsideBlock()));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (hasBase(world, pos)) {
            BlockPos base = pos.down();
            world.getBlockState(base).onEntityCollision(world, base, entity);
        }
    }

    /** Goes away by itself if the box under it is gone (it took off, or was deleted). */
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !(neighborState.getBlock() instanceof TardisExteriorBlock)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(GallifreyModBlocks.TARDIS_EXTERIOR);
    }
}
