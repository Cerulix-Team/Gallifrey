package com.cerulixteam.gallifrey.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class RoundelBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS =
            EnumProperty.of("axis", Direction.Axis.class, Direction.Axis.X, Direction.Axis.Z);

    public RoundelBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(AXIS, Direction.Axis.Z));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction.Axis playerFacingAxis = ctx.getHorizontalPlayerFacing().getAxis();
        Direction.Axis axis = playerFacingAxis == Direction.Axis.X ? Direction.Axis.X : Direction.Axis.Z;
        return this.getDefaultState().with(AXIS, axis);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        Direction.Axis current = state.get(AXIS);
        boolean swaps = rotation == BlockRotation.CLOCKWISE_90 || rotation == BlockRotation.COUNTERCLOCKWISE_90;
        if (!swaps) {
            return state;
        }
        Direction.Axis swapped = current == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return state.with(AXIS, swapped);
    }
}

