package com.cerulixteam.gallifrey.entity.custom;

import com.cerulixteam.gallifrey.GallifreySounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Hostile Skaro City Dalek.
 *
 * Daleks can walk on land and navigate through water. They also hover slightly:
 * when airborne they fall slowly rather than dropping like a normal mob.
 */
public class SkaroCityDalekEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public SkaroCityDalekEntity(EntityType<? extends SkaroCityDalekEntity> type, World world) {
        super(type, world);
        // Use normal gravity so the Dalek can walk and swim naturally. The custom
        // airborne damping below gives it the slow, chicken-like hover.
        this.setNoGravity(false);
        this.experiencePoints = 12;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 8.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        AmphibiousSwimNavigation navigation = new AmphibiousSwimNavigation(this, world);
        navigation.setCanSwim(true);
        return navigation;
    }

    @Override
    protected void initGoals() {
        // Target every living creature except other Skaro City Daleks.
        this.targetSelector.add(1, new FindHostileTargetGoal(this));
        this.goalSelector.add(2, new LaserAttackGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7D));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.random.nextInt(6)) {
            case 0 -> GallifreySounds.DALEK_SKARO_AMBIENT_0;
            case 1 -> GallifreySounds.DALEK_SKARO_AMBIENT_1;
            case 2 -> GallifreySounds.DALEK_SKARO_AMBIENT_2;
            case 3 -> GallifreySounds.DALEK_SKARO_AMBIENT_3;
            case 4 -> GallifreySounds.DALEK_SKARO_AMBIENT_4;
            default -> GallifreySounds.DALEK_SKARO_AMBIENT_5;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (this.random.nextInt(3)) {
            case 0 -> GallifreySounds.DALEK_HURT_0;
            case 1 -> GallifreySounds.DALEK_HURT_1;
            default -> GallifreySounds.DALEK_HURT_2;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return GallifreySounds.DALEK_HURT_2;
    }

    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state) {
        this.playSound(GallifreySounds.DALEK_GLIDE, 0.45F, 1.0F);
    }

    @Override
    public float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        // Daleks hover/glide and are not harmed by falls.
        return false;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Swim when submerged; otherwise retain ordinary land movement.
        if (this.isTouchingWater()) {
            this.setSwimming(true);
        } else {
            this.setSwimming(false);
        }

        // Chicken-like hovering: the Dalek is still affected by gravity, but
        // while airborne its downward speed is capped to a gentle fall.
        if (!this.isOnGround() && !this.isTouchingWater() && !this.hasVehicle()) {
            Vec3d velocity = this.getVelocity();
            if (velocity.y < -0.12D) {
                this.setVelocity(velocity.x, -0.12D, velocity.z);
                this.velocityModified = true;
            }
        }

        // A quiet hover/mechanical sound while the Dalek is moving.
        if (!this.getWorld().isClient() && this.age % 80 == 0 && this.getVelocity().horizontalLengthSquared() > 0.0025D) {
            this.playSound(GallifreySounds.DALEK_HOVER, 0.35F, 1.0F);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // The supplied model is static, so it does not require an animation file.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private static final class FindHostileTargetGoal extends Goal {
        private final SkaroCityDalekEntity dalek;
        private LivingEntity target;

        private FindHostileTargetGoal(SkaroCityDalekEntity dalek) {
            this.dalek = dalek;
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            this.target = this.dalek.getWorld().getEntitiesByClass(
                    LivingEntity.class,
                    this.dalek.getBoundingBox().expand(16.0D),
                    entity -> entity.isAlive()
                            && entity != this.dalek
                            && !(entity instanceof SkaroCityDalekEntity)
                            && gallifrey$canBeTargeted(entity)
            ).stream().min((a, b) -> Double.compare(
                    this.dalek.squaredDistanceTo(a),
                    this.dalek.squaredDistanceTo(b)
            )).orElse(null);
            return this.target != null;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity current = this.dalek.getTarget();
            return current != null
                    && current.isAlive()
                    && !(current instanceof SkaroCityDalekEntity)
                    && gallifrey$canBeTargeted(current)
                    && this.dalek.squaredDistanceTo(current) <= 16.0D * 16.0D;
        }

        private static boolean gallifrey$canBeTargeted(LivingEntity entity) {
            if (entity instanceof ServerPlayerEntity player) {
                return player.interactionManager.getGameMode() == GameMode.SURVIVAL;
            }
            return !(entity instanceof PlayerEntity);
        }

        @Override
        public void start() {
            this.dalek.setTarget(this.target);
        }

        @Override
        public void stop() {
            if (this.dalek.getTarget() instanceof SkaroCityDalekEntity) {
                this.dalek.setTarget(null);
            }
        }
    }

    private static final class LaserAttackGoal extends Goal {
        private final SkaroCityDalekEntity dalek;
        private int cooldown;

        private LaserAttackGoal(SkaroCityDalekEntity dalek) {
            this.dalek = dalek;
            this.cooldown = 20;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.dalek.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = this.dalek.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.cooldown = 10;
        }

        @Override
        public void stop() {
            this.dalek.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.dalek.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }

            double distanceSq = this.dalek.squaredDistanceTo(target);
            this.dalek.getLookControl().lookAt(target, 30.0F, 30.0F);

            if (distanceSq > 20.0D * 20.0D || !this.dalek.getVisibilityCache().canSee(target)) {
                this.dalek.getNavigation().startMovingTo(target, 0.85D);
                return;
            }

            this.dalek.getNavigation().stop();

            if (this.cooldown > 0) {
                this.cooldown--;
                return;
            }

            Vec3d origin = new Vec3d(this.dalek.getX(), this.dalek.getEyeY() - 0.05D, this.dalek.getZ());
            Vec3d direction = new Vec3d(
                    target.getX() - origin.x,
                    target.getEyeY() - origin.y,
                    target.getZ() - origin.z
            );

            if (direction.lengthSquared() > 0.0001D) {
                LaserEntity.fire(
                        this.dalek.getWorld(),
                        this.dalek,
                        origin,
                        direction,
                        LaserEntity.LaserColor.BLUE,
                        10.0D,
                        1.7F,
                        0.0F
                );
                this.dalek.playSound(GallifreySounds.DALEK_LASER_SHOOT, 1.0F, 0.95F + this.dalek.random.nextFloat() * 0.1F);
                this.dalek.playSound(GallifreySounds.DALEK_SKARO_ATTACK_0, 0.7F, 1.0F);
            }

            this.cooldown = 30;
        }
    }
}
