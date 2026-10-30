package com.cerulixteam.gallifrey.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import com.cerulixteam.gallifrey.entity.GallifreyEntities;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/** A peaceful, wandering Time Lord found on Gallifrey. */
public class TimeLordEntity extends PathAwareEntity {
    /** Number of regenerations already used by this Time Lord (maximum 12). */
    private int regenerationsUsed = 0;

    public int getRegenerationsUsed() {
        return regenerationsUsed;
    }

    public void setRegenerationsUsed(int count) {
        this.regenerationsUsed = Math.max(0, Math.min(12, count));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("RegenerationsUsed", regenerationsUsed);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        regenerationsUsed = Math.max(0, Math.min(12, nbt.getInt("RegenerationsUsed")));
    }
    public TimeLordEntity(EntityType<? extends TimeLordEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));

        // Time Lords recognise Cybermen and Daleks as enemies and actively hunt them.
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false,
                target -> isEnemyFaction(target)));
    }

    private boolean isEnemyFaction(LivingEntity target) {
        if (target == this) return false;
        EntityType<?> type = target.getType();
        return type == GallifreyEntities.MONDASIAN_CYBERMAN
                || type == GallifreyEntities.PROTO_CYBERMAN
                || type == GallifreyEntities.CYBERMAT
                || type == GallifreyEntities.SKARO_CITY_DALEK
                || type == GallifreyEntities.SKARO_CITY_DALEK_ALT
                || type == GallifreyEntities.SUPREME_COUNCIL_DALEK;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
