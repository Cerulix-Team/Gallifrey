package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

/** Bucket of Sulfur Cube (26.x): a small Sulfur Cube scooped up in an empty bucket, released where you use it. */
public class SulfurCubeBucketItem extends Item {
    public SulfurCubeBucketItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld world)) return ActionResult.SUCCESS;
        BlockPos pos = context.getBlockPos().offset(context.getSide());
        ItemStack stack = context.getStack();
        SulfurCubeEntity cube = BackportMobs.SULFUR_CUBE.create(world, stack.getNbt() != null && stack.getNbt().contains("EntityTag")
                ? stack.getNbt().getCompound("EntityTag") : null, null, pos, SpawnReason.BUCKET, true, false);
        if (cube == null) return ActionResult.FAIL;
        cube.setLarge(false);
        cube.setPersistent();
        if (stack.hasCustomName()) cube.setCustomName(stack.getName());
        world.spawnEntity(cube);
        world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
            context.getPlayer().setStackInHand(context.getHand(), new ItemStack(Items.BUCKET));
        }
        return ActionResult.CONSUME;
    }

    /** The bucket a small cube goes into, keeping its name and data. */
    static ItemStack of(SulfurCubeEntity cube) {
        ItemStack stack = new ItemStack(BackportMobs.SULFUR_CUBE_BUCKET);
        NbtCompound data = new NbtCompound();
        cube.writeCustomDataToNbt(data);
        data.remove("large");
        stack.getOrCreateNbt().put("EntityTag", data);
        if (cube.hasCustomName()) stack.setCustomName(cube.getCustomName());
        return stack;
    }
}
