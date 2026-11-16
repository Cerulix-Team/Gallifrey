package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Copper Bulb (1.21): every time it starts getting a redstone signal it switches on or off.
 * Gives off light while on (less the more it has rusted), and a comparator reads 15 from it while on.
 * The waxed bulbs use this class.
 */
public class CopperBulbBlock extends Block {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty POWERED = Properties.POWERED;

    public CopperBulbBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(LIT, false).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.getBlock() != state.getBlock() && world instanceof ServerWorld server) {
            this.update(state, server, pos);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world instanceof ServerWorld server) {
            this.update(state, server, pos);
        }
    }

    private void update(BlockState state, ServerWorld world, BlockPos pos) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered == state.get(POWERED)) return;
        BlockState next = state;
        if (!state.get(POWERED)) {
            next = next.cycle(LIT);
            world.playSound(null, pos, next.get(LIT) ? SoundEvents.BLOCK_COPPER_PLACE : SoundEvents.BLOCK_COPPER_BREAK,
                    SoundCategory.BLOCKS, 0.6F, next.get(LIT) ? 1.4F : 1.0F);
        }
        world.setBlockState(pos, next.with(POWERED, powered), Block.NOTIFY_ALL);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LIT) ? 15 : 0;
    }
}
