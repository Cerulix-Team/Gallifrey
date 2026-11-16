package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Shardwarden: an original Gallifrey creature, made for the Gallifreyan Trial Chambers. A hovering guardian of
 * gold and time-crystal: a glowing core in a gold halo, with four crystal plates orbiting it. It keeps its
 * distance and fires Temporal Shards that slow whatever they hit, and when it's hurt it may blink away.
 */
public class ShardwardenEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShardwardenEntity(EntityType<? extends ShardwardenEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.experiencePoints = 12;
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation nav = new BirdNavigation(this, world);
        nav.setCanPathThroughDoors(false);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        return nav;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new ShardGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient && this.random.nextInt(3) == 0) {
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getParticleX(0.6), this.getBodyY(0.6), this.getParticleZ(0.6), 0, -0.02, 0);
        }
        // Hover: drift gently up and down when idle.
        if (!this.getWorld().isClient && this.getTarget() == null) {
            Vec3d v = this.getVelocity();
            this.setVelocity(v.x * 0.9, Math.sin(this.age * 0.05) * 0.01, v.z * 0.9);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hurt = super.damage(source, amount);
        if (hurt && this.isAlive() && !this.getWorld().isClient && this.random.nextFloat() < 0.3F) this.blink();
        return hurt;
    }

    /** A short jump through time: reappears up to 8 blocks away, somewhere it can float. */
    private void blink() {
        ServerWorld world = (ServerWorld) this.getWorld();
        for (int i = 0; i < 16; i++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double y = this.getY() + this.random.nextInt(5) - 2;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            Vec3d from = this.getPos();
            this.setPosition(x, y, z);
            if (world.isSpaceEmpty(this) && !world.containsFluid(this.getBoundingBox())) {
                this.refreshPositionAfterTeleport(x, y, z);
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 0.7, from.z, 24, 0.3, 0.5, 0.3, 0.05);
                world.spawnParticles(ParticleTypes.REVERSE_PORTAL, x, y + 0.7, z, 24, 0.3, 0.5, 0.3, 0.05);
                this.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, 1.0F, 1.6F);
                return;
            }
            this.setPosition(from);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK; }
    @Override public int getMinAmbientSoundDelay() { return 120; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Keeps 6 to 10 blocks from its target, circling, and fires a shard every couple of seconds. */
    static class ShardGoal extends Goal {
        private final ShardwardenEntity warden;
        private int cooldown;

        ShardGoal(ShardwardenEntity warden) {
            this.warden = warden;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity t = this.warden.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = this.warden.getTarget();
            if (target == null) return;
            this.warden.getLookControl().lookAt(target, 30.0F, 30.0F);
            double d = this.warden.squaredDistanceTo(target);
            Vec3d around = target.getPos().add(0, 2.5, 0);
            Vec3d away = this.warden.getPos().subtract(target.getPos()).multiply(1, 0, 1);
            if (away.lengthSquared() < 0.01) away = new Vec3d(1, 0, 0);
            Vec3d side = new Vec3d(-away.z, 0, away.x).normalize().multiply(2.0);
            Vec3d goal = around.add(away.normalize().multiply(8.0)).add(side);
            if (d < 36.0 || d > 100.0 || this.warden.age % 40 == 0) {
                this.warden.getMoveControl().moveTo(goal.x, goal.y, goal.z, 1.0);
            }
            if (--this.cooldown <= 0 && d < 400.0 && this.warden.canSee(target)) {
                this.cooldown = 40 + this.warden.random.nextInt(20);
                TemporalShardEntity shard = new TemporalShardEntity(this.warden.getWorld(), this.warden);
                Vec3d from = this.warden.getPos().add(0, this.warden.getHeight() * 0.55, 0);
                shard.setPosition(from);
                Vec3d to = target.getPos().add(0, target.getHeight() * 0.5, 0).subtract(from);
                shard.setVelocity(to.x, to.y, to.z, 1.2F, 2.0F);
                this.warden.getWorld().spawnEntity(shard);
                this.warden.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 1.0F, 1.8F);
            }
        }
    }
}
