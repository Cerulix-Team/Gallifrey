package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

/** The Crafter's 3x3 grid, which slots are switched off, and hopper rules (fill the emptiest open slot first). */
public class CrafterBlockEntity extends LockableContainerBlockEntity implements RecipeInputInventory, SidedInventory {
    private static final int[] SLOTS = IntStream.range(0, 9).toArray();
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private final boolean[] disabled = new boolean[9];

    /** 0-8: slot switched off (1) or not; 9: whether it's powered. Synced to the open screen. */
    public final PropertyDelegate properties = new PropertyDelegate() {
        @Override public int get(int index) {
            if (index < 9) return CrafterBlockEntity.this.disabled[index] ? 1 : 0;
            return CrafterBlockEntity.this.getCachedState().get(CrafterBlock.TRIGGERED) ? 1 : 0;
        }
        @Override public void set(int index, int value) {
            if (index < 9) CrafterBlockEntity.this.disabled[index] = value != 0;
        }
        @Override public int size() { return 10; }
    };

    public CrafterBlockEntity(BlockPos pos, BlockState state) {
        super(BackportExtras.CRAFTER_ENTITY, pos, state);
    }

    public boolean isDisabled(int slot) {
        return this.disabled[slot];
    }

    public void toggle(int slot) {
        if (this.items.get(slot).isEmpty()) {
            this.disabled[slot] = !this.disabled[slot];
            this.markDirty();
        }
    }

    public int comparatorOutput() {
        int count = 0;
        for (int i = 0; i < 9; i++) if (this.disabled[i] || !this.items.get(i).isEmpty()) count++;
        return count;
    }

    @Override protected Text getContainerName() { return Text.translatable("container.gallifrey.crafter"); }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CrafterScreenHandler(syncId, playerInventory, this, this.properties);
    }

    @Override public int size() { return 9; }
    @Override public boolean isEmpty() { return this.items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return this.items.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { ItemStack out = Inventories.splitStack(this.items, slot, amount); this.markDirty(); return out; }
    @Override public ItemStack removeStack(int slot) { return Inventories.removeStack(this.items, slot); }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.disabled[slot]) this.disabled[slot] = false;
        this.items.set(slot, stack);
        this.markDirty();
    }

    @Override public boolean canPlayerUse(PlayerEntity player) { return net.minecraft.inventory.Inventory.canPlayerUse(this, player); }
    @Override public void clear() { this.items.clear(); }

    @Override public int getWidth() { return 3; }
    @Override public int getHeight() { return 3; }
    @Override public List<ItemStack> getInputStacks() { return List.copyOf(this.items); }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack stack : this.items) finder.addUnenchantedInput(stack);
    }

    @Override public int[] getAvailableSlots(Direction side) { return SLOTS; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return true; }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (this.disabled[slot]) return false;
        ItemStack here = this.items.get(slot);
        if (!here.isEmpty() && (!ItemStack.canCombine(here, stack) || here.getCount() >= here.getMaxCount())) return false;
        // Hoppers fill the emptiest open slot first, so ingredients spread across the pattern.
        int count = here.getCount();
        for (int i = 0; i < 9; i++) {
            if (i == slot || this.disabled[i]) continue;
            ItemStack other = this.items.get(i);
            if (other.isEmpty() || ItemStack.canCombine(other, stack)) {
                if (other.getCount() < count) return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return !this.disabled[slot];
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items = DefaultedList.ofSize(9, ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.items);
        int[] off = nbt.getIntArray("disabled_slots");
        java.util.Arrays.fill(this.disabled, false);
        for (int i : off) if (i >= 0 && i < 9) this.disabled[i] = true;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.items);
        nbt.putIntArray("disabled_slots", IntStream.range(0, 9).filter(i -> this.disabled[i]).toArray());
    }
}
