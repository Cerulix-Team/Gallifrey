package com.cerulixteam.gallifrey.block.backport.trial;

import com.cerulixteam.gallifrey.block.backport.trial.VaultBlock.State;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A Vault's settings (loot table and key), who it's already rewarded, and the item it shows inside. */
public class VaultBlockEntity extends BlockEntity {
    public static final double ACTIVATION_RANGE = 4.0;

    private Identifier lootTable = new Identifier("gallifrey", "chests/trial_chambers/reward");
    private Item keyItem = BackportTrials.TRIAL_KEY;
    private final Set<UUID> rewarded = new HashSet<>();
    private final List<ItemStack> ejecting = new ArrayList<>();
    private ItemStack display = ItemStack.EMPTY;
    private long nextStepAt;

    /** Client side: the spin of the displayed item. */
    public float spin, lastSpin;

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(BackportTrials.VAULT_ENTITY, pos, state);
    }

    public ItemStack getDisplay() {
        return this.display;
    }

    private void set(ServerWorld world, BlockState state, State s) {
        if (state.get(VaultBlock.STATE) != s) world.setBlockState(this.pos, state.with(VaultBlock.STATE, s), Block.NOTIFY_ALL);
        this.markDirty();
    }

    private List<ItemStack> roll(ServerWorld world, PlayerEntity player) {
        LootTable table = world.getServer().getLootManager().getLootTable(this.lootTable);
        LootContextParameterSet params = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos))
                .addOptional(LootContextParameters.THIS_ENTITY, player).luck(player.getLuck()).build(LootContextTypes.CHEST);
        return table.generateLoot(params);
    }

    boolean tryUnlock(ServerWorld world, BlockState state, PlayerEntity player, ItemStack held) {
        if (state.get(VaultBlock.STATE) != State.ACTIVE) return false;
        if (!held.isOf(this.keyItem)) {
            world.playSound(null, this.pos, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_INSERT_ITEM_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return false;
        }
        if (this.rewarded.contains(player.getUuid())) {
            world.playSound(null, this.pos, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_REJECT_REWARDED_PLAYER, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return false;
        }
        List<ItemStack> loot = this.roll(world, player);
        if (loot.isEmpty()) return false;
        if (!player.getAbilities().creativeMode) held.decrement(1);
        this.rewarded.add(player.getUuid());
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
            com.cerulixteam.gallifrey.block.backport.BackportAdvancements.grant(sp, state.isOf(BackportTrials.GALLIFREYAN_VAULT) ? "gallifreyan_vault" : state.get(VaultBlock.OMINOUS) ? "revaulting" : "under_lock_and_key");
        }
        this.ejecting.clear();
        this.ejecting.addAll(loot);
        this.display = loot.get(0).copy();
        this.nextStepAt = world.getTime() + 14;
        world.playSound(null, this.pos, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_INSERT_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
        this.set(world, state, State.UNLOCKING);
        this.sync(world, state);
        return true;
    }

    public static void serverTick(World w, BlockPos pos, BlockState state, VaultBlockEntity be) {
        if (w instanceof ServerWorld world) be.tick(world, state);
    }

    private void tick(ServerWorld world, BlockState state) {
        long now = world.getTime();
        State s = state.get(VaultBlock.STATE);
        switch (s) {
            case INACTIVE, ACTIVE -> {
                if (now % 20 != 0) return;
                boolean someone = false;
                for (PlayerEntity p : world.getPlayers()) {
                    if (!p.isSpectator() && p.squaredDistanceTo(Vec3d.ofCenter(this.pos)) <= ACTIVATION_RANGE * ACTIVATION_RANGE
                            && !this.rewarded.contains(p.getUuid())) {
                        someone = true;
                        break;
                    }
                }
                if (someone != (s == State.ACTIVE)) {
                    world.playSound(null, this.pos, someone ? com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_ACTIVATE : com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_DEACTIVATE,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
                    this.set(world, state, someone ? State.ACTIVE : State.INACTIVE);
                }
                if (someone && now % 40 == 0) {
                    PlayerEntity anyone = world.getClosestPlayer(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, ACTIVATION_RANGE, false);
                    if (anyone != null) {
                        List<ItemStack> preview = this.roll(world, anyone);
                        this.display = preview.isEmpty() ? ItemStack.EMPTY : preview.get(world.random.nextInt(preview.size())).copy();
                        this.sync(world, state);
                    }
                } else if (!someone && !this.display.isEmpty()) {
                    this.display = ItemStack.EMPTY;
                    this.sync(world, state);
                }
            }
            case UNLOCKING -> {
                if (now >= this.nextStepAt) {
                    this.nextStepAt = now + 20;
                    this.set(world, state, State.EJECTING);
                }
            }
            case EJECTING -> {
                if (now < this.nextStepAt) return;
                this.nextStepAt = now + 20;
                if (this.ejecting.isEmpty()) {
                    this.display = ItemStack.EMPTY;
                    this.sync(world, state);
                    this.set(world, state, State.ACTIVE);
                    return;
                }
                ItemStack stack = this.ejecting.remove(0);
                Direction facing = state.get(VaultBlock.FACING);
                Vec3d out = Vec3d.ofCenter(this.pos).add(facing.getOffsetX() * 0.7, 0.2, facing.getOffsetZ() * 0.7);
                ItemEntity item = new ItemEntity(world, out.x, out.y, out.z, stack);
                item.setVelocity(facing.getOffsetX() * 0.12, 0.1, facing.getOffsetZ() * 0.12);
                world.spawnEntity(item);
                world.playSound(null, this.pos, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_EJECT_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.spawnParticles(ParticleTypes.SMALL_FLAME, out.x, out.y, out.z, 6, 0.1, 0.1, 0.1, 0.01);
                this.display = this.ejecting.isEmpty() ? ItemStack.EMPTY : this.ejecting.get(0).copy();
                this.sync(world, state);
            }
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, VaultBlockEntity be) {
        be.lastSpin = be.spin;
        be.spin = (be.spin + 4.0F) % 360.0F;
        if (state.get(VaultBlock.STATE) != State.INACTIVE && world.random.nextInt(200) == 0) {
            world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, com.cerulixteam.gallifrey.block.backport.BackportSounds.BLOCK_VAULT_AMBIENT, SoundCategory.BLOCKS, 0.5F, 1.0F, false);
        }
        if (state.get(VaultBlock.STATE) != State.INACTIVE && world.random.nextInt(8) == 0) {
            world.addParticle(state.get(VaultBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME,
                    pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(), pos.getZ() + world.random.nextDouble(), 0, 0, 0);
        }
    }

    private void sync(ServerWorld world, BlockState state) {
        this.markDirty();
        world.updateListeners(this.pos, state, state, Block.NOTIFY_LISTENERS);
    }

    // =========================================================
    // SAVING
    // =========================================================

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("config")) {
            NbtCompound config = nbt.getCompound("config");
            if (config.contains("loot_table")) this.lootTable = new Identifier(config.getString("loot_table"));
            if (config.contains("key_item")) {
                NbtCompound key = config.getCompound("key_item");
                String id = key.contains("id") ? key.getString("id") : "";
                Item item = Registries.ITEM.get(Identifier.tryParse(id));
                if (item != net.minecraft.item.Items.AIR) this.keyItem = item;
            }
        }
        this.rewarded.clear();
        for (NbtElement e : nbt.getList("rewarded_players", NbtElement.INT_ARRAY_TYPE)) this.rewarded.add(NbtHelper.toUuid(e));
        this.ejecting.clear();
        for (NbtElement e : nbt.getList("items_to_eject", NbtElement.COMPOUND_TYPE)) this.ejecting.add(ItemStack.fromNbt((NbtCompound) e));
        this.display = nbt.contains("display_item") ? ItemStack.fromNbt(nbt.getCompound("display_item")) : ItemStack.EMPTY;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound config = new NbtCompound();
        config.putString("loot_table", this.lootTable.toString());
        NbtCompound key = new NbtCompound();
        key.putString("id", Registries.ITEM.getId(this.keyItem).toString());
        key.putInt("count", 1);
        config.put("key_item", key);
        nbt.put("config", config);
        NbtList rewarded = new NbtList();
        for (UUID id : this.rewarded) rewarded.add(NbtHelper.fromUuid(id));
        nbt.put("rewarded_players", rewarded);
        NbtList eject = new NbtList();
        for (ItemStack s : this.ejecting) eject.add(s.writeNbt(new NbtCompound()));
        nbt.put("items_to_eject", eject);
        if (!this.display.isEmpty()) nbt.put("display_item", this.display.writeNbt(new NbtCompound()));
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /** Clients only need the item on show. */
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        if (!this.display.isEmpty()) nbt.put("display_item", this.display.writeNbt(new NbtCompound()));
        return nbt;
    }
}
