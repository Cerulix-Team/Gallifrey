package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/** The three items on a {@link ShelfBlock}, kept in sync with the client so they can be drawn. */
public class ShelfBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(BackportExtras.SHELF_ENTITY, pos, state);
    }

    @Override public int size() { return 3; }
    @Override public boolean isEmpty() { return this.items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return this.items.get(slot); }
    @Override public int getMaxCountPerStack() { return 64; }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack out = Inventories.splitStack(this.items, slot, amount);
        this.markDirty();
        return out;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack out = Inventories.removeStack(this.items, slot);
        this.markDirty();
        return out;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(this, player); }
    @Override public void clear() { this.items.clear(); }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items.clear();
        Inventories.readNbt(nbt, this.items);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.items, true);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }
}
