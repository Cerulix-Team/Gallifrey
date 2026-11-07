package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * Creaking Heart (1.21.4). Between two pale oak logs along its axis it's "dormant" by day and "awake" at
 * night; anywhere else it's "uprooted". (The Creaking itself comes with the mob backports.)
 */
public class CreakingHeartBlock extends PillarBlock {
    public enum State implements StringIdentifiable {
        UPROOTED("uprooted"), DORMANT("dormant"), AWAKE("awake");
        private final String name;
        State(String name) { this.name = name; }
        @Override public String asString() { return this.name; }
    }

    public static final EnumProperty<State> STATE = EnumProperty.of("creaking_heart_state", State.class);

    public CreakingHeartBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y).with(STATE, State.UPROOTED));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STATE);
    }

    private static boolean rooted(WorldView world, BlockPos pos, Direction.Axis axis) {
        for (Direction d : Direction.values()) {
            if (d.getAxis() != axis) continue;
            BlockState s = world.getBlockState(pos.offset(d));
            if (!s.isOf(BackportBlocks.PALE_OAK_LOG) && !s.isOf(BackportBlocks.PALE_OAK_WOOD)) return false;
            if (s.contains(PillarBlock.AXIS) && s.get(PillarBlock.AXIS) != axis) return false;
        }
        return true;
    }

    private static State stateFor(WorldView world, BlockPos pos, Direction.Axis axis) {
        if (!rooted(world, pos, axis)) return State.UPROOTED;
        boolean night = world instanceof ServerWorld server ? server.isNight() : world.getAmbientDarkness() > 4;
        return night ? State.AWAKE : State.DORMANT;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        return state.with(STATE, stateFor(ctx.getWorld(), ctx.getBlockPos(), state.get(AXIS)));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state.with(STATE, stateFor(world, pos, state.get(AXIS)));
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        State now = stateFor(world, pos, state.get(AXIS));
        if (now != state.get(STATE)) world.setBlockState(pos, state.with(STATE, now), Block.NOTIFY_ALL);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, net.minecraft.world.World world, BlockPos pos) {
        return state.get(STATE) == State.AWAKE ? 15 : 0;
    }
}
