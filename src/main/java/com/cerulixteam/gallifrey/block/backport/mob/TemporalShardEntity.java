package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A Shardwarden's Temporal Shard: hurts, and slows what it hits, as if time had thickened around it. */
public class TemporalShardEntity extends ThrownItemEntity {
    public TemporalShardEntity(EntityType<? extends TemporalShardEntity> type, World world) {
        super(type, world);
    }

    public TemporalShardEntity(World world, LivingEntity owner) {
        super(BackportMobs.TEMPORAL_SHARD_PROJECTILE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return BackportMobs.TEMPORAL_SHARD;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > 100) this.discard();
        if (this.getWorld().isClient) this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (hit.getEntity() == this.getOwner()) return;
        hit.getEntity().damage(this.getDamageSources().mobProjectile(this, this.getOwner() instanceof LivingEntity l ? l : null), 4.0F);
        if (hit.getEntity() instanceof LivingEntity living) living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1), this.getOwner());
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld() instanceof ServerWorld world) {
            world.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 8, 0.1, 0.1, 0.1, 0.05);
            world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, net.minecraft.sound.SoundCategory.HOSTILE, 0.6F, 1.5F);
            this.discard();
        }
    }
}
