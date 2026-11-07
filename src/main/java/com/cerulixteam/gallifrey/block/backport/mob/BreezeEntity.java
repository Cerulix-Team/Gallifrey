package com.cerulixteam.gallifrey.block.backport.mob;

import com.cerulixteam.gallifrey.block.backport.WindChargeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Breeze (1.21): a jumpy wind spirit from Trial Chambers. It leaps about, shoots Wind Charges, and
 * deflects other projectiles. No fall damage. Drops Breeze Rods.
 */
public class BreezeEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BreezeEntity(EntityType<? extends BreezeEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new ShootGoal(this));
        this.goalSelector.add(2, new LeapGoal(this));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, false));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_PROJECTILE) && source.getSource() instanceof ProjectileEntity projectile
                && !(projectile instanceof WindChargeEntity)) {
            // Deflect: send it back the way it came.
            projectile.setVelocity(projectile.getVelocity().multiply(-0.5));
            projectile.setOwner(this);
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient && this.random.nextInt(3) == 0) {
            this.getWorld().addParticle(ParticleTypes.CLOUD, this.getParticleX(0.5), this.getY() + 0.1, this.getParticleZ(0.5), 0, 0.02, 0);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PHANTOM_FLAP; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_BLAZE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_BLAZE_DEATH; }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    /** Shoots a wind charge at its target every couple of seconds. */
    static class ShootGoal extends Goal {
        private final BreezeEntity breeze;
        private int cooldown;

        ShootGoal(BreezeEntity breeze) {
            this.breeze = breeze;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.breeze.getTarget();
            return target != null && target.isAlive() && this.breeze.squaredDistanceTo(target) < 24 * 24 && this.breeze.canSee(target);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.breeze.getTarget();
            if (target == null) return;
            this.breeze.getLookControl().lookAt(target, 30, 30);
            if (--this.cooldown > 0) return;
            this.cooldown = 40 + this.breeze.random.nextInt(20);
            WindChargeEntity charge = new WindChargeEntity(this.breeze.getWorld(), this.breeze);
            Vec3d from = this.breeze.getPos().add(0, this.breeze.getHeight() * 0.7, 0);
            charge.setPosition(from);
            Vec3d to = target.getPos().add(0, target.getHeight() * 0.5, 0).subtract(from);
            charge.setVelocity(to.x, to.y, to.z, 0.9F, 5.0F);
            this.breeze.getWorld().spawnEntity(charge);
            this.breeze.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F, 1.4F);
        }
    }

    /** Big leaps: towards its target when far, away when close, at random otherwise. */
    static class LeapGoal extends Goal {
        private final BreezeEntity breeze;
        private int cooldown = 40;

        LeapGoal(BreezeEntity breeze) {
            this.breeze = breeze;
            this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (--this.cooldown > 0 || !this.breeze.isOnGround()) return false;
            this.cooldown = 50 + this.breeze.random.nextInt(60);
            return true;
        }

        @Override
        public void start() {
            LivingEntity target = this.breeze.getTarget();
            Vec3d dir;
            if (target != null) {
                Vec3d to = target.getPos().subtract(this.breeze.getPos());
                double d = to.horizontalLength();
                dir = d > 8 ? to.normalize() : d < 4 ? to.normalize().multiply(-1) : new Vec3d(this.breeze.random.nextGaussian(), 0, this.breeze.random.nextGaussian()).normalize();
            } else {
                dir = new Vec3d(this.breeze.random.nextGaussian(), 0, this.breeze.random.nextGaussian()).normalize();
            }
            this.breeze.setVelocity(dir.x * 0.8, 0.9, dir.z * 0.8);
            this.breeze.velocityDirty = true;
            this.breeze.playSound(SoundEvents.ENTITY_PHANTOM_FLAP, 1.0F, 1.6F);
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }
    }
}
