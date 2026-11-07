package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

/** Puts a cushion on top of the block you use it on, in the middle of the block. */
public class CushionItem extends Item {
    private final DyeColor color;

    public CushionItem(DyeColor color, Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getSide() != Direction.UP) return ActionResult.FAIL;
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        if (shape.isEmpty()) return ActionResult.FAIL;
        double y = pos.getY() + shape.getMax(Direction.Axis.Y);
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        Box space = new Box(x - 0.45, y, z - 0.45, x + 0.45, y + 0.25, z + 0.45);
        if (!world.getEntitiesByClass(CushionEntity.class, space, e -> true).isEmpty() || !world.isSpaceEmpty(space)) {
            return ActionResult.FAIL;
        }
        if (world instanceof ServerWorld server) {
            CushionEntity cushion = Backport26.CUSHION_ENTITY.create(server);
            if (cushion == null) return ActionResult.FAIL;
            cushion.setColor(this.color);
            cushion.refreshPositionAndAngles(x, y, z, 0, 0);
            server.spawnEntity(cushion);
            world.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.success(world.isClient);
    }
}
