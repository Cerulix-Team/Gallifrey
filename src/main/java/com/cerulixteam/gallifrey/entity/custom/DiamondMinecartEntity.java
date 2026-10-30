package com.cerulixteam.gallifrey.entity.custom;

import com.cerulixteam.gallifrey.entity.GallifreyEntities;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Rideable minecart with 4x the top speed of a normal one.
 *
 * It only goes full speed on straight, flat track. Within 2 blocks of a corner, a slope or the
 * end of the track it drops back to normal minecart speed, because at 4x speed a cart can
 * jump past a corner in a single tick and fly off the rails.
 */
public class DiamondMinecartEntity extends MinecartEntity {
    private static final double SPEED_MULTIPLIER = 4.0;

    public DiamondMinecartEntity(EntityType<? extends DiamondMinecartEntity> type, World world) {
        super(type, world);
    }

    public DiamondMinecartEntity(World world, double x, double y, double z) {
        this(GallifreyEntities.DIAMOND_MINECART, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    protected double getMaxSpeed() {
        double normal = super.getMaxSpeed();
        return this.isSafeToSpeed() ? normal * SPEED_MULTIPLIER : normal;
    }

    /** True when this rail and the next two in the direction of travel are straight and flat. */
    private boolean isSafeToSpeed() {
        Vec3d v = this.getVelocity();
        int dx = 0, dz = 0;
        if (Math.abs(v.x) > Math.abs(v.z)) dx = v.x > 0 ? 1 : -1;
        else if (v.z != 0) dz = v.z > 0 ? 1 : -1;

        BlockPos pos = this.getBlockPos();
        for (int i = 0; i <= 2; i++) {
            if (!isStraightFlatRail(pos.add(dx * i, 0, dz * i))) return false;
        }
        return true;
    }

    private boolean isStraightFlatRail(BlockPos pos) {
        BlockState state = this.getWorld().getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractRailBlock)) {
            state = this.getWorld().getBlockState(pos.down()); // carts on slopes sit a block up
        }
        if (!(state.getBlock() instanceof AbstractRailBlock rail)) return false;
        RailShape shape = state.get(rail.getShapeProperty());
        return shape == RailShape.NORTH_SOUTH || shape == RailShape.EAST_WEST;
    }

    @Override
    protected Item getItem() {
        return GallifreyModItems.DIAMOND_MINECART;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(GallifreyModItems.DIAMOND_MINECART);
    }
}
