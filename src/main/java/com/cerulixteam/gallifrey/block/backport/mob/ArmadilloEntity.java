package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Armadillo (1.21): rolls up into a ball when something scary is near (a sprinting or riding player, an
 * undead mob, or after being hit), and takes less damage like that. Drops a scute every 5-10 minutes,
 * or when brushed. Breeds with spider eyes.
 */
public class ArmadilloEntity extends AnimalEntity implements GeoEntity {
    private static final TrackedData<Boolean> ROLLED = DataTracker.registerData(ArmadilloEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int scuteTimer;
    private int calm;

    public ArmadilloEntity(EntityType<? extends ArmadilloEntity> type, World world) {
        super(type, world);
        this.scuteTimer = 6000 + this.random.nextInt(6000);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return AnimalEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.14);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ROLLED, false);
    }

    public boolean isRolledUp() {
        return this.dataTracker.get(ROLLED);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0) {
            @Override public boolean canStart() { return !ArmadilloEntity.this.isRolledUp() && super.canStart(); }
        });
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(3, new TemptGoal(this, 1.25, Ingredient.ofItems(Items.SPIDER_EYE), false));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isOf(Items.SPIDER_EYE);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return BackportMobs.ARMADILLO.create(world);
    }

    private boolean threatened() {
        if (this.hurtTime > 0) return true;
        for (LivingEntity other : this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(7, 2, 7), e -> e != this)) {
            if (other.getGroup() == EntityGroup.UNDEAD) return true;
            if (other instanceof PlayerEntity player && !player.isSpectator() && (player.isSprinting() || player.hasVehicle())) return true;
        }
        return false;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld().isClient || !this.isAlive()) return;
        if (!this.isBaby() && --this.scuteTimer <= 0) {
            this.dropItem(BackportMobs.ARMADILLO_SCUTE);
            this.playSound(SoundEvents.ENTITY_TURTLE_EGG_CRACK, 1.0F, 1.2F);
            this.scuteTimer = 6000 + this.random.nextInt(6000);
        }
        if (this.age % 5 == 0) {
            if (this.threatened()) {
                this.calm = 0;
                if (!this.isRolledUp()) {
                    this.dataTracker.set(ROLLED, true);
                    this.getNavigation().stop();
                    this.playSound(SoundEvents.ENTITY_TURTLE_SHAMBLE, 1.0F, 1.4F);
                }
            } else if (this.isRolledUp() && ++this.calm > 12) {
                this.dataTracker.set(ROLLED, false);
            }
        }
    }

    @Override
    public void travel(Vec3d input) {
        super.travel(this.isRolledUp() ? Vec3d.ZERO : input);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isRolledUp()) amount = Math.max(0, (amount - 1.0F) / 2.0F);
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(Items.BRUSH) && !this.isBaby()) {
            if (!this.getWorld().isClient) {
                this.dropItem(BackportMobs.ARMADILLO_SCUTE);
                this.playSound(SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC, 1.0F, 1.0F);
                stack.damage(16, player, p -> p.sendToolBreakStatus(hand));
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("scute_time", this.scuteTimer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("scute_time")) this.scuteTimer = nbt.getInt("scute_time");
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
