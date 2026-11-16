package com.cerulixteam.gallifrey.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/**
 * Classic Nether zombie pigman behaviour inspired by Alpha 1.2.6:
 * neutral until damaged, then angry at the attacker and nearby pigmen join in.
 */
public class AlphaZombiePigmanEntity extends ZombieEntity {
    private int classicAngerTicks;

    public AlphaZombiePigmanEntity(EntityType<? extends AlphaZombiePigmanEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 5;
        this.setBaby(false);
    }

    public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes();
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(9, new LookAroundGoal(this));
        // Deliberately no ActiveTargetGoal: Alpha pigmen were neutral until hit.
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged && !this.getWorld().isClient()) {
            LivingEntity attacker = source.getAttacker() instanceof LivingEntity living ? living : null;
            if (attacker != null && attacker != this && !(attacker instanceof AlphaZombiePigmanEntity)) {
                this.becomeAngryAt(attacker);
                Box nearby = this.getBoundingBox().expand(16.0D);
                for (AlphaZombiePigmanEntity pigman : this.getWorld().getEntitiesByClass(
                        AlphaZombiePigmanEntity.class, nearby, other -> other.isAlive() && other != this)) {
                    pigman.becomeAngryAt(attacker);
                }
            }
        }
        return damaged;
    }

    private void becomeAngryAt(LivingEntity target) {
        this.setTarget(target);
        this.classicAngerTicks = 600;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!this.getWorld().isClient() && this.classicAngerTicks > 0) {
            this.classicAngerTicks--;
            LivingEntity target = this.getTarget();
            if (target == null || !target.isAlive()) {
                this.classicAngerTicks = 0;
                this.setTarget(null);
            } else if (this.classicAngerTicks == 0) {
                this.setTarget(null);
            }
        }
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random,
                                 net.minecraft.world.LocalDifficulty difficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("minecraft", "entities/zombified_piglin");
    }
}
