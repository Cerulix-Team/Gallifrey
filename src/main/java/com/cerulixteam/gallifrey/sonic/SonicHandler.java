package com.cerulixteam.gallifrey.sonic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SonicHandler {

    private SonicHandler() {}

    public static void use(PlayerEntity player, World world, HitResult target, SonicMode mode) {
        switch (mode) {
            case INTERACTION -> interaction(player, world, target);
            case OVERLOAD -> overload(player, world, target);
            case SCAN -> scan(player, world, target);
        }
    }

    private static void interaction(PlayerEntity player, World world, HitResult target) {
        if (target.getType() != HitResult.Type.BLOCK) {
            player.sendMessage(Text.literal("§7SONIC: No interactable target."), true);
            return;
        }

        BlockHitResult hit = (BlockHitResult) target;
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Adventures in Time interaction mode: simple, non-destructive
        // interactions that do not need the overload mode.
        if (block instanceof CandleBlock && !state.get(CandleBlock.LIT)) {
            world.setBlockState(pos, state.with(CandleBlock.LIT, true), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal("§bSONIC: Candle ignited."), true);
            return;
        }

        if (block instanceof CampfireBlock && !state.get(CampfireBlock.LIT)) {
            world.setBlockState(pos, state.with(CampfireBlock.LIT, true), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal("§bSONIC: Campfire ignited."), true);
            return;
        }

        if (block instanceof DoorBlock) {
            boolean open = state.get(DoorBlock.OPEN);
            world.setBlockState(pos, state.with(DoorBlock.OPEN, !open), Block.NOTIFY_ALL);

            // Keep the other half of a two-block door synchronized. This is
            // especially important for iron doors because the normal hand
            // interaction is redstone-only.
            if (state.get(DoorBlock.HALF) == net.minecraft.block.enums.DoubleBlockHalf.LOWER) {
                BlockPos upperPos = pos.up();
                BlockState upper = world.getBlockState(upperPos);
                if (upper.getBlock() == block && upper.contains(DoorBlock.OPEN)) {
                    world.setBlockState(upperPos, upper.with(DoorBlock.OPEN, !open), Block.NOTIFY_ALL);
                }
            } else {
                BlockPos lowerPos = pos.down();
                BlockState lower = world.getBlockState(lowerPos);
                if (lower.getBlock() == block && lower.contains(DoorBlock.OPEN)) {
                    world.setBlockState(lowerPos, lower.with(DoorBlock.OPEN, !open), Block.NOTIFY_ALL);
                }
            }

            success(world, pos);
            player.sendMessage(Text.literal(open ? "§bSONIC: Door closed." : "§bSONIC: Door opened."), true);
            return;
        }

        if (block instanceof TrapdoorBlock) {
            boolean open = state.get(TrapdoorBlock.OPEN);
            world.setBlockState(pos, state.with(TrapdoorBlock.OPEN, !open), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal(open ? "§bSONIC: Trapdoor closed." : "§bSONIC: Trapdoor opened."), true);
            return;
        }

        if (block instanceof RepeaterBlock) {
            int delay = state.get(RepeaterBlock.DELAY);
            int nextDelay = delay >= 4 ? 1 : delay + 1;
            world.setBlockState(pos, state.with(RepeaterBlock.DELAY, nextDelay), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal("§bSONIC: Repeater delay " + nextDelay + "/4."), true);
            return;
        }

        if (block instanceof LeverBlock) {
            boolean powered = state.get(LeverBlock.POWERED);
            world.setBlockState(pos, state.with(LeverBlock.POWERED, !powered), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal(powered ? "§bSONIC: Lever deactivated." : "§bSONIC: Lever activated."), true);
            return;
        }

        if (block instanceof ButtonBlock button) {
            button.powerOn(state, world, pos);
            success(world, pos);
            player.sendMessage(Text.literal("§bSONIC: Button activated."), true);
            return;
        }

        player.sendMessage(Text.literal("§7SONIC: Target is not interactable."), true);
    }

    private static void overload(PlayerEntity player, World world, HitResult target) {
        if (target.getType() != HitResult.Type.BLOCK) {
            player.sendMessage(Text.literal("§7SONIC: No overload target."), true);
            return;
        }

        BlockHitResult hit = (BlockHitResult) target;
        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof RedstoneLampBlock) {
            boolean lit = state.get(RedstoneLampBlock.LIT);
            world.setBlockState(pos, state.with(RedstoneLampBlock.LIT, !lit), Block.NOTIFY_ALL);
            success(world, pos);
            player.sendMessage(Text.literal(lit ? "§bSONIC: Lamp deactivated." : "§bSONIC: Lamp activated."), true);
            return;
        }

        if (block instanceof TntBlock) {
            igniteTnt(world, pos);
            player.sendMessage(Text.literal("§cSONIC: TNT activated!"), true);
            return;
        }

        if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
            world.breakBlock(pos, true, player);
            player.sendMessage(Text.literal("§cSONIC: Glass shattered."), true);
            return;
        }

        player.sendMessage(Text.literal("§7SONIC: Target cannot be overloaded."), true);
    }

    private static void scan(PlayerEntity player, World world, HitResult target) {
        if (target.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) target;
            BlockState state = world.getBlockState(hit.getBlockPos());
            Identifier id = Registries.BLOCK.getId(state.getBlock());

            player.sendMessage(Text.literal(
                    "§bSONIC SCAN\n§fTarget: §e" + state.getBlock().getName().getString()
                            + "\n§fID: §7" + id
            ), false);
            return;
        }

        if (target.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) target).getEntity();
            Identifier id = Registries.ENTITY_TYPE.getId(entity.getType());
            StringBuilder text = new StringBuilder(
                    "§bSONIC SCAN\n§fTarget: §e" + entity.getName().getString()
                            + "\n§fType: §7" + id
            );

            if (entity instanceof LivingEntity living) {
                text.append("\n§fHealth: §a")
                        .append(String.format("%.1f", living.getHealth()))
                        .append(" §7/ §a")
                        .append(String.format("%.1f", living.getMaxHealth()));
            }

            player.sendMessage(Text.literal(text.toString()), false);
            return;
        }

        player.sendMessage(Text.literal("§7SONIC SCAN: Nothing detected."), true);
    }

    private static void success(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT,
                SoundCategory.BLOCKS, 0.5F, 1.5F);
    }

    private static void igniteTnt(World world, BlockPos pos) {
        world.removeBlock(pos, false);
        TntEntity tnt = new TntEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, null);
        tnt.setFuse(80);
        world.spawnEntity(tnt);
        world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }
}
