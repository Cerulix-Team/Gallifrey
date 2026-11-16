package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A thrown Wind Charge (1.21): flies straight, and bursts into a gust where it hits. */
public class WindChargeEntity extends ThrownItemEntity {
    public WindChargeEntity(EntityType<? extends WindChargeEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public WindChargeEntity(World world, LivingEntity owner) {
        super(BackportCombat.WIND_CHARGE_ENTITY, owner, world);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return BackportCombat.WIND_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age > 200) this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        hit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 1.0F);
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld() instanceof ServerWorld world) {
            WindBurst.burst(world, hit.getPos(), 2.4, 1.1, this.getOwner());
            this.discard();
        }
    }
}
