package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Decorated pot storage (1.20.3), on vanilla's pot through mixin/backport/DecoratedPotStorageMixin. */
public interface PotStorage {
    ItemStack gallifrey$getStack();

    void gallifrey$setStack(ItemStack stack);

    @Nullable
    Identifier gallifrey$takeLootTable();

    long gallifrey$lootSeed();

    /** Rolls the pot's loot table into it, if it still has one. */
    static void fill(PotStorage pot, World world, BlockPos pos) {
        Identifier table = pot.gallifrey$takeLootTable();
        if (table == null || !(world instanceof ServerWorld server)) return;
        LootTable loot = server.getServer().getLootManager().getLootTable(table);
        List<ItemStack> items = loot.generateLoot(new LootContextParameterSet.Builder(server).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .build(LootContextTypes.CHEST), pot.gallifrey$lootSeed());
        ItemStack first = ItemStack.EMPTY;
        for (ItemStack s : items) {
            if (first.isEmpty()) first = s;
            else if (ItemStack.canCombine(first, s)) first.increment(s.getCount());
        }
        pot.gallifrey$setStack(first);
    }

    /** Right-click with an item to put one in (it must match what's already there); empty-handed just wobbles. */
    static ActionResult use(PotStorage pot, BlockEntity be, World world, BlockPos pos, PlayerEntity player, ItemStack held) {
        fill(pot, world, pos);
        ItemStack inside = pot.gallifrey$getStack();
        if (!held.isEmpty() && (inside.isEmpty() || (ItemStack.canCombine(inside, held) && inside.getCount() < inside.getMaxCount()))) {
            if (!world.isClient) {
                if (inside.isEmpty()) pot.gallifrey$setStack(held.copyWithCount(1));
                else inside.increment(1);
                if (!player.getAbilities().creativeMode) held.decrement(1);
                be.markDirty();
                float fill = pot.gallifrey$getStack().getCount() / (float) pot.gallifrey$getStack().getMaxCount();
                world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_HIT, SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F * fill);
                ((ServerWorld) world).spawnParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0, 0, 0, 0);
            }
            return ActionResult.success(world.isClient);
        }
        if (!world.isClient) world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResult.success(world.isClient);
    }
}
