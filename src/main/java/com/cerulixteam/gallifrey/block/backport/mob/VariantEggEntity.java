package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A thrown Blue or Brown Egg: like a normal egg, but its chicks are cold or warm chickens. */
public class VariantEggEntity extends ThrownItemEntity {
    private int variant = 1;

    public VariantEggEntity(EntityType<? extends VariantEggEntity> type, World world) {
        super(type, world);
    }

    public VariantEggEntity(World world, LivingEntity owner, int variant) {
        super(BackportMobs.VARIANT_EGG, owner, world);
        this.variant = variant;
    }

    @Override
    protected Item getDefaultItem() {
        return this.variant == 2 ? BackportMobs.BLUE_EGG : BackportMobs.BROWN_EGG;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        hit.getEntity().damage(this.getDamageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld().isClient) return;
        if (this.random.nextInt(8) == 0) {
            int chicks = this.random.nextInt(32) == 0 ? 4 : 1;
            for (int i = 0; i < chicks; i++) {
                ChickenEntity chick = EntityType.CHICKEN.create(this.getWorld());
                if (chick == null) continue;
                chick.setBreedingAge(-24000);
                chick.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
                AnimalVariants.set(chick, this.variant);
                this.getWorld().spawnEntity(chick);
            }
        }
        this.getWorld().sendEntityStatus(this, (byte) 3);
        this.discard();
    }
}
