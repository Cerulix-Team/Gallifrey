package com.cerulixteam.gallifrey.block.backport.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The two newer skeletons: the Bogged (1.21, swamps, poison arrows, shear its mushrooms off) and the
 * Parched (1.21.11, deserts, weakness arrows, doesn't burn in daylight).
 */
public class BackportSkeletonEntity extends SkeletonEntity {
    public enum Kind { BOGGED, PARCHED }

    private final Kind kind;
    private static final net.minecraft.entity.data.TrackedData<Boolean> SHEARED = net.minecraft.entity.data.DataTracker.registerData(
            BackportSkeletonEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHEARED, false);
    }

    public boolean isSheared() {
        return this.dataTracker.get(SHEARED);
    }

    public BackportSkeletonEntity(EntityType<? extends BackportSkeletonEntity> type, World world, Kind kind) {
        super(type, world);
        this.kind = kind;
    }

    public Kind getKind() {
        return this.kind;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return AbstractSkeletonEntity.createAbstractSkeletonAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0);
    }

    /** Bogged and Parched don't turn into Strays in powder snow. */
    @Override
    protected void convertToStray() {
    }

    @Override
    public boolean isShaking() {
        return false;
    }

    @Override protected SoundEvent getAmbientSound() { return this.kind == Kind.BOGGED ? com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_BOGGED_AMBIENT : SoundEvents.ENTITY_SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) { return this.kind == Kind.BOGGED ? com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_BOGGED_HURT : SoundEvents.ENTITY_SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound() { return this.kind == Kind.BOGGED ? com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_BOGGED_DEATH : SoundEvents.ENTITY_SKELETON_DEATH; }

    @Override
    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
        PersistentProjectileEntity projectile = super.createArrowProjectile(arrow, damageModifier);
        if (projectile instanceof ArrowEntity tipped) {
            tipped.addEffect(this.kind == Kind.BOGGED ? new StatusEffectInstance(StatusEffects.POISON, 100)
                    : new StatusEffectInstance(StatusEffects.WEAKNESS, 600));
        }
        return projectile;
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return this.kind != Kind.PARCHED && super.isAffectedByDaylight();
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (this.kind == Kind.BOGGED && stack.isOf(Items.SHEARS) && !this.isSheared()) {
            if (!this.getWorld().isClient) {
                this.dataTracker.set(SHEARED, true);
                this.dropItem(Items.RED_MUSHROOM);
                this.dropItem(Items.BROWN_MUSHROOM);
                this.playSound(com.cerulixteam.gallifrey.block.backport.BackportSounds.ENTITY_BOGGED_SHEAR, 1.0F, 1.0F);
                stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("sheared", this.isSheared());
    }

    @Override
    public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(SHEARED, nbt.getBoolean("sheared"));
    }
}
