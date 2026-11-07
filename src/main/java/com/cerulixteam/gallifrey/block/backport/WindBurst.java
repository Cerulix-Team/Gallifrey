package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * A wind burst (1.21's Wind Charge and the Wind Burst enchantment): knocks entities away from a point
 * without hurting them or breaking blocks, and flicks doors, trapdoors, fence gates, buttons, levers and
 * bells within reach. Whoever caused it is launched harder, which is the "wind charge jump".
 */
public final class WindBurst {
    private WindBurst() {}

    public static void burst(ServerWorld world, Vec3d at, double radius, double strength, @Nullable Entity owner) {
        world.spawnParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 16, 0.5, 0.5, 0.5, 0.15);
        world.playSound(null, at.x, at.y, at.z, com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_WIND_CHARGE_WIND_BURST, SoundCategory.BLOCKS, 1.0F, 1.0F);

        Box box = new Box(at, at).expand(radius);
        for (Entity entity : world.getOtherEntities(null, box)) {
            if (entity.isSpectator()) continue;
            Vec3d away = entity.getPos().add(0, entity.getHeight() * 0.5, 0).subtract(at);
            double distance = away.length();
            if (distance > radius) continue;
            double power = (1.0 - distance / radius) * strength;
            if (entity instanceof LivingEntity living) {
                power *= 1.0 - living.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) * 0.5;
            }
            if (entity == owner) power *= 1.6;
            Vec3d push = distance < 1.0E-4 ? new Vec3d(0, 1, 0) : away.normalize();
            entity.addVelocity(push.x * power, Math.max(0.2, push.y) * power, push.z * power);
            entity.velocityModified = true;
            if (entity == owner) entity.fallDistance = 0;
            if (entity instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
            }
        }

        int r = (int) Math.ceil(radius * 0.6);
        BlockPos center = BlockPos.ofFloored(at);
        for (BlockPos pos : BlockPos.iterateOutwards(center, r, r, r)) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door) {
                if (door.getBlockSetType().canOpenByHand()) door.setOpen(null, world, state, pos, !state.get(DoorBlock.OPEN));
            } else if (state.getBlock() instanceof TrapdoorBlock || state.getBlock() instanceof FenceGateBlock) {
                world.setBlockState(pos, state.cycle(Properties.OPEN), 10);
            } else if (state.getBlock() instanceof ButtonBlock button && !state.get(ButtonBlock.POWERED)) {
                button.powerOn(state, world, pos);
            } else if (state.getBlock() instanceof LeverBlock lever) {
                lever.togglePower(state, world, pos);
            } else if (state.getBlock() instanceof BellBlock bell) {
                bell.ring(world, pos, state.get(BellBlock.FACING).rotateYClockwise());
            }
        }
    }
}
