package com.cerulixteam.gallifrey.block.backport.trial;

import com.cerulixteam.gallifrey.block.backport.trial.TrialSpawnerBlock.State;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** The Trial Spawner's state: the players it's fighting, the mobs it's spawned and its timers. */
public class TrialSpawnerBlockEntity extends BlockEntity {
    public static final int PLAYER_RANGE = 14;
    public static final int COOLDOWN = 36000;
    public static final String SPAWNED_TAG = "gallifrey.trial_spawned";

    @Nullable private Identifier normalConfig;
    @Nullable private Identifier ominousConfig;
    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> mobs = new HashSet<>();
    private int spawned;
    private long nextSpawnAt;
    private long cooldownEndsAt;
    private long nextOminousDropAt;
    private int rewardsLeft;

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BackportTrials.TRIAL_SPAWNER_ENTITY, pos, state);
    }

    public void setConfigs(@Nullable Identifier normal, @Nullable Identifier ominous) {
        this.normalConfig = normal;
        this.ominousConfig = ominous;
        this.markDirty();
    }

    @Nullable
    private TrialSpawnerConfig config(BlockState state) {
        TrialSpawnerConfig ominous = state.get(TrialSpawnerBlock.OMINOUS) ? TrialSpawnerConfig.get(this.ominousConfig) : null;
        return ominous != null ? ominous : TrialSpawnerConfig.get(this.normalConfig);
    }

    private void set(ServerWorld world, BlockState state, State s, boolean ominous) {
        BlockState next = state.with(TrialSpawnerBlock.STATE, s).with(TrialSpawnerBlock.OMINOUS, ominous);
        if (next != state) world.setBlockState(this.pos, next, Block.NOTIFY_ALL);
        this.markDirty();
    }

    private List<PlayerEntity> nearbyPlayers(ServerWorld world) {
        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity p : world.getPlayers()) {
            if (p.isSpectator() || p.isCreative() || !p.isAlive()) continue;
            if (p.squaredDistanceTo(Vec3d.ofCenter(this.pos)) <= PLAYER_RANGE * PLAYER_RANGE) list.add(p);
        }
        return list;
    }

    public static void serverTick(World w, BlockPos pos, BlockState state, TrialSpawnerBlockEntity be) {
        if (w instanceof ServerWorld world) be.tick(world, state);
    }

    private void tick(ServerWorld world, BlockState state) {
        long now = world.getTime();
        State s = state.get(TrialSpawnerBlock.STATE);
        boolean ominous = state.get(TrialSpawnerBlock.OMINOUS);
        TrialSpawnerConfig config = this.config(state);
        if (config == null) {
            if (s != State.INACTIVE) this.set(world, state, State.INACTIVE, false);
            return;
        }
        switch (s) {
            case INACTIVE -> this.set(world, state, State.WAITING_FOR_PLAYERS, ominous);
            case WAITING_FOR_PLAYERS -> {
                if (now % 20 != 0) return;
                List<PlayerEntity> near = this.nearbyPlayers(world);
                if (near.isEmpty()) return;
                boolean omen = false;
                for (PlayerEntity p : near) {
                    this.players.add(p.getUuid());
                    omen |= BackportTrials.takeOmen(p);
                }
                this.spawned = 0;
                this.mobs.clear();
                this.nextSpawnAt = now + 20;
                world.playSound(null, this.pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, omen ? 0.6F : 1.2F);
                this.set(world, state, State.ACTIVE, omen);
            }
            case ACTIVE -> this.tickActive(world, state, config, now, ominous);
            case WAITING_FOR_REWARD_EJECTION -> {
                if (now >= this.nextSpawnAt) {
                    this.rewardsLeft = this.players.size();
                    this.set(world, state, State.EJECTING_REWARD, ominous);
                }
            }
            case EJECTING_REWARD -> {
                if (now % 30 != 0) return;
                if (this.rewardsLeft > 0) {
                    this.eject(world, config);
                    this.rewardsLeft--;
                } else {
                    this.cooldownEndsAt = now + COOLDOWN;
                    this.players.clear();
                    world.playSound(null, this.pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 0.6F);
                    this.set(world, state, State.COOLDOWN, ominous);
                }
            }
            case COOLDOWN -> {
                if (now >= this.cooldownEndsAt) this.set(world, state, State.WAITING_FOR_PLAYERS, false);
            }
        }
    }

    private void tickActive(ServerWorld world, BlockState state, TrialSpawnerConfig config, long now, boolean ominous) {
        // New players joining the fight raise the totals.
        for (PlayerEntity p : this.nearbyPlayers(world)) {
            if (this.players.add(p.getUuid())) this.markDirty();
            if (!ominous && BackportTrials.takeOmen(p)) {
                ominous = true;
                this.set(world, state, State.ACTIVE, true);
                state = world.getBlockState(this.pos);
                TrialSpawnerConfig o = this.config(state);
                if (o != null) config = o;
            }
        }
        this.mobs.removeIf(id -> {
            Entity e = world.getEntity(id);
            return e == null || !e.isAlive() || e.squaredDistanceTo(Vec3d.ofCenter(this.pos)) > 47 * 47;
        });
        int total = config.totalFor(this.players.size());
        if (this.spawned >= total && this.mobs.isEmpty()) {
            this.nextSpawnAt = now + 40;
            this.set(world, state, State.WAITING_FOR_REWARD_EJECTION, ominous);
            return;
        }
        if (this.spawned < total && this.mobs.size() < config.simultaneousFor(this.players.size()) && now >= this.nextSpawnAt) {
            if (this.spawnMob(world, config, ominous)) this.spawned++;
            this.nextSpawnAt = now + config.ticksBetweenSpawn();
            this.markDirty();
        }
        if (ominous && now >= this.nextOminousDropAt) {
            this.nextOminousDropAt = now + 160;
            this.ominousDrop(world, config);
        }
    }

    private boolean spawnMob(ServerWorld world, TrialSpawnerConfig config, boolean ominous) {
        TrialSpawnerConfig.Potential potential = TrialSpawnerConfig.pick(config.spawnPotentials(), world.random);
        if (potential == null) return false;
        int range = config.spawnRange();
        for (int attempt = 0; attempt < 10; attempt++) {
            double x = this.pos.getX() + 0.5 + (world.random.nextDouble() - world.random.nextDouble()) * range;
            double y = this.pos.getY() + world.random.nextInt(3) - 1;
            double z = this.pos.getZ() + 0.5 + (world.random.nextDouble() - world.random.nextDouble()) * range;
            NbtCompound data = potential.entity().copy();
            Entity entity = EntityType.loadEntityWithPassengers(data, world, e -> {
                e.refreshPositionAndAngles(x, y, z, world.random.nextFloat() * 360.0F, 0.0F);
                return e;
            });
            if (entity == null) return false;
            BlockPos at = BlockPos.ofFloored(x, y, z);
            if (!world.isSpaceEmpty(entity) || !world.getBlockState(at.down()).isSolidBlock(world, at.down())) {
                entity.discard();
                continue;
            }
            if (entity instanceof MobEntity mob) {
                if (data.getSize() <= 1) mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.SPAWNER, null, null);
                mob.setPersistent();
                if (potential.equipment() != null) this.equip(world, mob, potential.equipment(), potential.dropChance());
            }
            entity.addCommandTag(SPAWNED_TAG);
            if (!world.spawnNewEntityAndPassengers(entity)) return false;
            this.mobs.add(entity.getUuid());
            world.spawnParticles(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, entity.getX(), entity.getBodyY(0.5), entity.getZ(), 20, 0.3, 0.5, 0.3, 0.01);
            world.playSound(null, at, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private void equip(ServerWorld world, MobEntity mob, Identifier table, float dropChance) {
        for (ItemStack stack : loot(world, table, mob)) {
            EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
            mob.equipStack(slot, stack);
            mob.setEquipmentDropChance(slot, dropChance);
        }
    }

    private List<ItemStack> loot(ServerWorld world, Identifier table, @Nullable Entity entity) {
        LootTable lootTable = world.getServer().getLootManager().getLootTable(table);
        LootContextParameterSet.Builder params = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos));
        if (entity != null) params.addOptional(LootContextParameters.THIS_ENTITY, entity);
        return lootTable.generateLoot(params.build(LootContextTypes.CHEST));
    }

    private void eject(ServerWorld world, TrialSpawnerConfig config) {
        Identifier table = TrialSpawnerConfig.pick(config.lootTablesToEject(), world.random);
        if (table == null) return;
        for (ItemStack stack : this.loot(world, table, null)) {
            ItemEntity item = new ItemEntity(world, this.pos.getX() + 0.5, this.pos.getY() + 1.2, this.pos.getZ() + 0.5, stack);
            item.setVelocity(world.random.nextGaussian() * 0.05, 0.25, world.random.nextGaussian() * 0.05);
            world.spawnEntity(item);
        }
        world.playSound(null, this.pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 0.8F);
        world.spawnParticles(ParticleTypes.SMALL_FLAME, this.pos.getX() + 0.5, this.pos.getY() + 1.1, this.pos.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0.01);
    }

    /** Ominous spawners drop things (potions, arrows, wind charges) on the players from above. */
    private void ominousDrop(ServerWorld world, TrialSpawnerConfig config) {
        List<ServerPlayerEntity> targets = new ArrayList<>();
        for (UUID id : this.players) {
            if (world.getPlayerByUuid(id) instanceof ServerPlayerEntity p && p.isAlive() && p.squaredDistanceTo(Vec3d.ofCenter(this.pos)) < 47 * 47) targets.add(p);
        }
        if (targets.isEmpty()) return;
        ServerPlayerEntity target = targets.get(world.random.nextInt(targets.size()));
        List<ItemStack> items = this.loot(world, config.itemsToDropWhenOminous(), null);
        if (items.isEmpty()) return;
        Vec3d at = target.getPos().add(world.random.nextGaussian(), 3.0 + world.random.nextInt(2), world.random.nextGaussian());
        if (!world.isSpaceEmpty(new Box(at.subtract(0.25, 0.25, 0.25), at.add(0.25, 0.25, 0.25)))) at = target.getPos().add(0, 2.0, 0);
        BackportTrials.dropOminousItem(world, at, items.get(0));
    }

    // =========================================================
    // SAVING
    // =========================================================

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.normalConfig = nbt.contains("normal_config") ? Identifier.tryParse(nbt.getString("normal_config")) : null;
        this.ominousConfig = nbt.contains("ominous_config") ? Identifier.tryParse(nbt.getString("ominous_config")) : null;
        this.players.clear();
        for (NbtElement e : nbt.getList("registered_players", NbtElement.INT_ARRAY_TYPE)) this.players.add(NbtHelper.toUuid(e));
        this.mobs.clear();
        for (NbtElement e : nbt.getList("current_mobs", NbtElement.INT_ARRAY_TYPE)) this.mobs.add(NbtHelper.toUuid(e));
        this.spawned = nbt.getInt("total_mobs_spawned");
        this.nextSpawnAt = nbt.getLong("next_mob_spawns_at");
        this.cooldownEndsAt = nbt.getLong("cooldown_ends_at");
        this.rewardsLeft = nbt.getInt("rewards_left");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.normalConfig != null) nbt.putString("normal_config", this.normalConfig.toString());
        if (this.ominousConfig != null) nbt.putString("ominous_config", this.ominousConfig.toString());
        NbtList players = new NbtList();
        for (UUID id : this.players) players.add(NbtHelper.fromUuid(id));
        nbt.put("registered_players", players);
        NbtList mobs = new NbtList();
        for (UUID id : this.mobs) mobs.add(NbtHelper.fromUuid(id));
        nbt.put("current_mobs", mobs);
        nbt.putInt("total_mobs_spawned", this.spawned);
        nbt.putLong("next_mob_spawns_at", this.nextSpawnAt);
        nbt.putLong("cooldown_ends_at", this.cooldownEndsAt);
        nbt.putInt("rewards_left", this.rewardsLeft);
    }
}
