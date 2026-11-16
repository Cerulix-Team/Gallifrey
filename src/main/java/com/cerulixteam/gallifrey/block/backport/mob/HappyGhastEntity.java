package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Happy Ghast (1.21.6). A ghastling hatches from a Dried Ghast and grows up in 20 minutes. Put a Harness
 * on a grown one and up to four players can ride it; the first flies it where they look. Snowballs heal
 * it, and it follows anyone holding one.
 */
public class HappyGhastEntity extends AnimalEntity implements GeoEntity {
    /** -1 for no harness, otherwise the harness's dye colour id. */
    private static final TrackedData<Integer> HARNESS = DataTracker.registerData(HappyGhastEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public HappyGhastEntity(EntityType<? extends HappyGhastEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return AnimalEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.05).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HARNESS, -1);
    }

    public int getHarness() {
        return this.dataTracker.get(HARNESS);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanPathThroughDoors(false);
        navigation.setCanSwim(true);
        return navigation;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new TemptGoal(this, 1.0, Ingredient.ofItems(Items.SNOWBALL), false));
        this.goalSelector.add(5, new FlyGoal(this, 1.0) {
            @Override public boolean canStart() { return !HappyGhastEntity.this.hasPassengers() && super.canStart(); }
        });
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        World world = this.getWorld();
        if (stack.getItem() instanceof HarnessItem harness && !this.isBaby() && this.getHarness() < 0) {
            if (!world.isClient) {
                this.dataTracker.set(HARNESS, harness.color.getId());
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 1.0F, 1.0F);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.SHEARS) && this.getHarness() >= 0 && !this.hasPassengers()) {
            if (!world.isClient) {
                this.dropItem(BackportMobs.HARNESSES[this.getHarness()]);
                this.dataTracker.set(HARNESS, -1);
                this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isOf(Items.SNOWBALL) && this.getHealth() < this.getMaxHealth()) {
            if (!world.isClient) {
                this.heal(1.0F);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
            }
            return ActionResult.success(world.isClient);
        }
        if (stack.isEmpty() && this.getHarness() >= 0 && !this.isBaby() && this.getPassengerList().size() < 4) {
            if (!world.isClient) player.startRiding(this);
            return ActionResult.success(world.isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 4;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof PlayerEntity player && this.getHarness() >= 0 ? player : null;
    }

    @Override
    protected void tickControlled(PlayerEntity controller, Vec3d input) {
        super.tickControlled(controller, input);
        this.setRotation(controller.getYaw(), 0);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controller, Vec3d input) {
        float forward = controller.forwardSpeed;
        float sideways = controller.sidewaysSpeed * 0.5F;
        double up = -MathHelper.sin(controller.getPitch() * MathHelper.RADIANS_PER_DEGREE) * forward;
        return new Vec3d(sideways, up, forward);
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controller) {
        return 0.1F;
    }

    @Override
    public void travel(Vec3d input) {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateVelocity(this.getControllingPassenger() != null ? 0.1F : 0.02F, input);
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91));
        }
        this.updateLimbs(false);
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        int index = this.getPassengerList().indexOf(passenger);
        double[][] seats = {{0.5, 0.5}, {-0.5, 0.5}, {0.5, -0.5}, {-0.5, -0.5}};
        double[] seat = seats[Math.max(0, Math.min(3, index))];
        Vec3d offset = new Vec3d(seat[0], 0, seat[1]).rotateY(-this.getYaw() * MathHelper.RADIANS_PER_DEGREE);
        positionUpdater.accept(passenger, this.getX() + offset.x, this.getY() + this.getHeight() - 0.1, this.getZ() + offset.z);
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_GHAST_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_GHAST_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_GHAST_DEATH; }
    @Override protected float getSoundVolume() { return 0.4F; }
    @Override public float getSoundPitch() { return 1.4F; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("harness", this.getHarness());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("harness")) this.dataTracker.set(HARNESS, nbt.getInt("harness"));
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (this.getHarness() >= 0) this.dropItem(BackportMobs.HARNESSES[this.getHarness()]);
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    /** A Dried Ghast that's fully hydrated hatches into a ghastling. */
    public static void hatch(ServerWorld world, BlockPos pos) {
        HappyGhastEntity ghastling = BackportMobs.HAPPY_GHAST.create(world);
        if (ghastling == null) return;
        world.removeBlock(pos, false);
        ghastling.setBreedingAge(-24000);
        ghastling.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360, 0);
        world.spawnEntity(ghastling);
        world.playSound(null, pos, SoundEvents.ENTITY_GHAST_AMBIENT, net.minecraft.sound.SoundCategory.NEUTRAL, 0.6F, 1.8F);
    }
}
