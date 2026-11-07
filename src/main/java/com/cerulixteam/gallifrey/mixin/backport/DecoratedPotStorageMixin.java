package com.cerulixteam.gallifrey.mixin.backport;

import com.cerulixteam.gallifrey.block.backport.PotStorage;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Decorated pots hold one stack of items (1.20.3), and can be filled from a loot table (1.20.5, Trial Chambers). */
@Mixin(DecoratedPotBlockEntity.class)
public abstract class DecoratedPotStorageMixin implements PotStorage {
    @Unique private ItemStack gallifrey$stack = ItemStack.EMPTY;
    @Unique private Identifier gallifrey$lootTable;
    @Unique private long gallifrey$lootSeed;

    @Override
    public ItemStack gallifrey$getStack() {
        return this.gallifrey$stack;
    }

    @Override
    public void gallifrey$setStack(ItemStack stack) {
        this.gallifrey$stack = stack;
        ((DecoratedPotBlockEntity) (Object) this).markDirty();
    }

    @Override
    public Identifier gallifrey$takeLootTable() {
        Identifier table = this.gallifrey$lootTable;
        this.gallifrey$lootTable = null;
        return table;
    }

    @Override
    public long gallifrey$lootSeed() {
        return this.gallifrey$lootSeed;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void gallifrey$writeStack(NbtCompound nbt, CallbackInfo ci) {
        if (this.gallifrey$lootTable != null) {
            nbt.putString("LootTable", this.gallifrey$lootTable.toString());
            if (this.gallifrey$lootSeed != 0) nbt.putLong("LootTableSeed", this.gallifrey$lootSeed);
        } else if (!this.gallifrey$stack.isEmpty()) {
            nbt.put("item", this.gallifrey$stack.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void gallifrey$readStack(NbtCompound nbt, CallbackInfo ci) {
        this.gallifrey$lootTable = nbt.contains("LootTable") ? Identifier.tryParse(nbt.getString("LootTable")) : null;
        this.gallifrey$lootSeed = nbt.getLong("LootTableSeed");
        this.gallifrey$stack = nbt.contains("item") ? ItemStack.fromNbt(nbt.getCompound("item")) : ItemStack.EMPTY;
    }
}
