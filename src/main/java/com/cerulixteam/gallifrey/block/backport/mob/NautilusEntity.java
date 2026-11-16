package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * Nautilus and Zombie Nautilus (1.21.11): underwater mounts. Tame a nautilus with pufferfish (zombie
 * nautiluses don't need taming), saddle it to ride, and give it Nautilus Armor. Its rider can breathe
 * underwater, and sprinting makes it dash.
 */
public class NautilusEntity extends PathAwareEntity implements GeoEntity {
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(NautilusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    /** -1 none, otherwise index into NautilusArmorItem.MATERIALS. */
    private static final TrackedData<Integer> ARMOR = DataTracker.registerData(NautilusEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final boolean zombie;
    @Nullable private UUID owner;
    private int dashCooldown;

    public NautilusEntity(EntityType<? extends NautilusEntity> type, World world, boolean zombie) {
        super(type, world);
        this.zombie = zombie;
        this.moveControl = new AquaticMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new YawAdjustingLookControl(this, 10);
    }

    public boolean isZombie() { return this.zombie; }
    public boolean isSaddled() { return this.dataTracker.get(SADDLED); }
    public int getArmor2() { return this.dataTracker.get(ARMOR); }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SADDLED, false);
        this.dataTracker.startTracking(ARMOR, -1);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SwimNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(4, new SwimAroundGoal(this, 1.0, 10) {
            @Override public boolean canStart() { return !NautilusEntity.this.hasPassengers() && super.canStart(); }
        });
        this.goalSelector.add(5, new LookAroundGoal(this));
    }

    @Override
    public EntityGroup getGroup() {
        return this.zombie ? EntityGroup.UNDEAD : EntityGroup.AQUATIC;
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    public boolean isTame() {
        return this.zombie || this.owner != null;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = this.getWorld();
        if (!this.isTame() && stack.isOf(Items.PUFFERFISH)) {
            if (!world.isClient) {
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                if (this.random.nextInt(3) == 0) {
                    this.owner = player.getUuid();
                    ((ServerWorld) world).spawnParticles(ParticleTypes.HEART, this.getX(), this.getBodyY(0.8), this.getZ(), 6, 0.4, 0.4, 0.4, 0);
                } else {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.SMOKE, this.getX(), this.getBodyY(0.8), this.getZ(), 6, 0.4, 0.4, 0.4, 0);
                }
            }
            return ActionResult.success(world.isClient);
        }
        if (this.isTame() && stack.isOf(Items.SADDLE) && !this.isSaddled()) {
            if (!world.isClient) {
                this.dataTracker.set(SADDLED, true);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 1.0F, 1.0F);
            }
            return ActionResult.success(world.isClient);
        }
        if (this.isTame() && stack.getItem() instanceof NautilusArmorItem armor && this.getArmor2() < 0) {
            if (!world.isClient) {
                this.dataTracker.set(ARMOR, armor.material);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0F, 1.0F);
            }
            return ActionResult.success(world.isClient);
        }
        if (this.isSaddled() && !this.hasPassengers() && !player.shouldCancelInteraction()) {
            if (!world.isClient) player.startRiding(this);
            return ActionResult.success(world.isClient);
        }
        return super.interactMob(player, hand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.isSaddled() && this.getFirstPassenger() instanceof PlayerEntity player ? player : null;
    }

    @Override
    protected void tickControlled(PlayerEntity controller, Vec3d input) {
        super.tickControlled(controller, input);
        this.setRotation(controller.getYaw(), controller.getPitch() * 0.5F);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
        if (!this.getWorld().isClient) {
            controller.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, true, false));
            if (controller.isSprinting() && --this.dashCooldown <= 0 && this.isTouchingWater()) {
                this.dashCooldown = 40;
                this.addVelocity(controller.getRotationVector().multiply(1.2));
                this.velocityModified = true;
                this.playSound(SoundEvents.ENTITY_DOLPHIN_JUMP, 1.0F, 0.7F);
            }
        }
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controller, Vec3d input) {
        float forward = controller.forwardSpeed;
        double up = this.isTouchingWater() ? -MathHelper.sin(controller.getPitch() * MathHelper.RADIANS_PER_DEGREE) * forward : 0;
        return new Vec3d(controller.sidewaysSpeed * 0.4F, up, forward);
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controller) {
        return this.isTouchingWater() ? 0.06F : 0.02F;
    }

    @Override
    public void travel(Vec3d input) {
        if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
            this.updateVelocity(this.getControllingPassenger() != null ? 0.06F : 0.01F * (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED), input);
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
            this.updateLimbs(false);
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        int armor = this.getArmor2();
        if (armor >= 0) amount *= 1.0F - NautilusArmorItem.PROTECTION[armor] / 25.0F;
        return super.damage(source, amount);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (this.isSaddled()) this.dropItem(Items.SADDLE);
        if (this.getArmor2() >= 0) this.dropItem(BackportMobs.NAUTILUS_ARMOR[this.getArmor2()]);
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_TURTLE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_TURTLE_DEATH; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("saddled", this.isSaddled());
        nbt.putInt("armor", this.getArmor2());
        if (this.owner != null) nbt.putUuid("owner", this.owner);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(SADDLED, nbt.getBoolean("saddled"));
        this.dataTracker.set(ARMOR, nbt.contains("armor") ? nbt.getInt("armor") : -1);
        this.owner = nbt.containsUuid("owner") ? nbt.getUuid("owner") : null;
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
