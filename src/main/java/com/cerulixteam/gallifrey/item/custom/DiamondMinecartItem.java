package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.entity.custom.DiamondMinecartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/** Places a Diamond Minecart on rails, the same way the vanilla minecart item does. */
public class DiamondMinecartItem extends Item {

    public DiamondMinecartItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!state.isIn(BlockTags.RAILS)) return ActionResult.FAIL;

        ItemStack stack = context.getStack();
        if (world instanceof ServerWorld) {
            RailShape shape = state.getBlock() instanceof AbstractRailBlock rail
                    ? state.get(rail.getShapeProperty()) : RailShape.NORTH_SOUTH;
            double yOffset = shape.isAscending() ? 0.5 : 0.0;

            DiamondMinecartEntity cart = new DiamondMinecartEntity(world,
                    pos.getX() + 0.5, pos.getY() + 0.0625 + yOffset, pos.getZ() + 0.5);
            if (stack.hasCustomName()) cart.setCustomName(stack.getName());
            world.spawnEntity(cart);
            world.emitGameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Emitter.of(context.getPlayer(), world.getBlockState(pos.down())));
        }
        stack.decrement(1);
        return ActionResult.success(world.isClient);
    }
}
