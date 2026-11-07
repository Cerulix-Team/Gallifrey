package com.cerulixteam.gallifrey.block.backport;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * The Mace (1.21). Hit something while falling (more than 1.5 blocks) for a smash attack:
 * +4 damage for each of the first 3 blocks fallen, +2 for each of the next 5, and +1 for each after that.
 * A smash knocks back everything nearby and cancels your fall damage.
 * <ul>
 *   <li>Density adds 0.5 damage per block fallen per level.</li>
 *   <li>Breach makes armour 15% less effective per level.</li>
 *   <li>Wind Burst launches you back up after a smash.</li>
 * </ul>
 */
public class MaceItem extends Item {
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public MaceItem(Settings settings) {
        super(settings);
        this.modifiers = ImmutableMultimap.of(
                EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", 5.0, EntityAttributeModifier.Operation.ADDITION),
                EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -3.4, EntityAttributeModifier.Operation.ADDITION));
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return ingredient.isOf(BackportCombat.BREEZE_ROD);
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    public static boolean canSmash(LivingEntity attacker) {
        return attacker.fallDistance > 1.5F && !attacker.isFallFlying();
    }

    private static float smashBonus(float fall, int density) {
        float bonus;
        if (fall <= 3) bonus = 4 * fall;
        else if (fall <= 8) bonus = 12 + 2 * (fall - 3);
        else bonus = 22 + (fall - 8);
        return bonus + 0.5F * density * fall;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        if (!(attacker.getWorld() instanceof ServerWorld world)) return true;

        int breach = EnchantmentHelper.getLevel(BackportCombat.BREACH, stack);
        float extra = 0;
        if (canSmash(attacker)) {
            float fall = attacker.fallDistance;
            extra = smashBonus(fall, EnchantmentHelper.getLevel(BackportCombat.DENSITY, stack));

            // Cancel the fall, and bounce back up with Wind Burst.
            attacker.fallDistance = 0;
            int windBurst = EnchantmentHelper.getLevel(BackportCombat.WIND_BURST, stack);
            Vec3d v = attacker.getVelocity();
            attacker.setVelocity(v.x, windBurst > 0 ? 0.7 + 0.35 * (windBurst - 1) : 0.01, v.z);
            attacker.velocityModified = true;
            if (attacker instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
            }
            if (windBurst > 0) WindBurst.burst(world, attacker.getPos(), 2.0, 0.0, null);

            // Knock back everything else close by.
            for (Entity other : world.getOtherEntities(attacker, new Box(target.getBlockPos()).expand(3.5), e -> e != target && e instanceof LivingEntity)) {
                Vec3d away = other.getPos().subtract(target.getPos());
                double distance = away.length();
                if (distance > 3.5) continue;
                double power = (3.5 - distance) * 0.7 * (fall > 5 ? 2 : 1);
                Vec3d push = away.lengthSquared() < 1.0E-6 ? Vec3d.ZERO : away.normalize().multiply(power);
                other.addVelocity(push.x, 0.7, push.z);
                other.velocityModified = true;
            }
            world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, world.getBlockState(target.getBlockPos().down())),
                    target.getX(), target.getY(), target.getZ(), 40, 1.0, 0.1, 1.0, 0.15);
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    fall > 5 ? com.cerulixteam.gallifrey.block.backport.BackportSounds.ITEM_MACE_SMASH_GROUND_HEAVY : com.cerulixteam.gallifrey.block.backport.BackportSounds.ITEM_MACE_SMASH_GROUND, SoundCategory.PLAYERS, 1.0F, 0.8F);
        }
        if (breach > 0) {
            // Armour takes roughly armour/25 of a hit; Breach gives back 15% of that per level.
            float base = (float) attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + extra;
            extra += base * Math.min(20, target.getArmor()) / 25.0F * 0.15F * breach;
        }
        if (extra > 0) {
            DamageSource source = attacker instanceof PlayerEntity player ? world.getDamageSources().playerAttack(player) : world.getDamageSources().mobAttack(attacker);
            target.timeUntilRegen = 0;
            target.damage(source, extra);
        }
        return true;
    }
}
