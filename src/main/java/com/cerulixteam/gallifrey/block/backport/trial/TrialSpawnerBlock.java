package com.cerulixteam.gallifrey.block.backport.trial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Trial Spawner (1.21). It wakes when players come near, spawns a wave of mobs sized to how many came, and
 * when they're all dead gives each player a reward, then rests for 30 minutes. A player with Trial Omen
 * makes it ominous: tougher mobs, items dropping on the players, and better rewards.
 */
public class TrialSpawnerBlock extends BlockWithEntity {
    public enum State implements StringIdentifiable {
        INACTIVE("inactive", 4), WAITING_FOR_PLAYERS("waiting_for_players", 4), ACTIVE("active", 8),
        WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8), EJECTING_REWARD("ejecting_reward", 8), COOLDOWN("cooldown", 0);

        private final String name;
        public final int light;

        State(String name, int light) {
            this.name = name;
            this.light = light;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public static final EnumProperty<State> STATE = EnumProperty.of("trial_spawner_state", State.class);
    public static final BooleanProperty OMINOUS = BooleanProperty.of("ominous");

    public TrialSpawnerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(STATE, State.INACTIVE).with(OMINOUS, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE, OMINOUS);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TrialSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, BackportTrials.TRIAL_SPAWNER_ENTITY, TrialSpawnerBlockEntity::serverTick);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        State s = state.get(STATE);
        if (s == State.INACTIVE || s == State.COOLDOWN) return;
        boolean ominous = state.get(OMINOUS);
        for (int i = 0; i < (s == State.ACTIVE ? 3 : 1); i++) {
            double x = pos.getX() + random.nextDouble(), y = pos.getY() + random.nextDouble(), z = pos.getZ() + random.nextDouble();
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            world.addParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
