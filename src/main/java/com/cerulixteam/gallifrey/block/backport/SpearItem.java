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
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;

/**
 * Spears (1.21.11). Two attacks:
 * <ul>
 *   <li><b>Jab</b>: a normal hit. With Lunge it also throws you forward (costs hunger; not while
 *   riding, gliding or in water, and you need enough food to sprint).</li>
 *   <li><b>Charge</b>: hold use to lower the spear, then run (or ride) into something within 4.5 blocks.
 *   The faster you're going, the harder it hits. For the first 5 seconds a charge also knocks the
 *   target off its mount; up to 10 seconds it knocks back; after that it still hits, but without knockback.</li>
 * </ul>
 */
public class SpearItem extends ToolItem {
    private static final double REACH = 4.5;
    private static final double MIN_SPEED = 0.2; // blocks per tick
    private static final java.util.Map<LivingEntity, Vec3d> LAST_POS = new java.util.WeakHashMap<>();
    private final Multimap<EntityAttribute, EntityAttributeModifier> modifiers;
    private final float chargeMultiplier;

    public SpearItem(ToolMaterial material, float jabDamage, float attackSpeed, float chargeMultiplier, Settings settings) {
        super(material, settings);
        this.chargeMultiplier = chargeMultiplier;
        this.modifiers = ImmutableMultimap.of(
                EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", jabDamage - 1.0, EntityAttributeModifier.Operation.ADDITION),
                EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed - 4.0, EntityAttributeModifier.Operation.ADDITION));
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.modifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        lunge(stack, attacker);
        return true;
    }

    private static void lunge(ItemStack stack, LivingEntity user) {
        int level = EnchantmentHelper.getLevel(BackportCombat.LUNGE, stack);
        if (level <= 0 || user.hasVehicle() || user.isFallFlying() || user.isTouchingWater()) return;
        if (user instanceof PlayerEntity player) {
            if (!player.isCreative() && player.getHungerManager().getFoodLevel() <= 6) return;
            player.addExhaustion(4.0F * level);
        }
        Vec3d look = user.getRotationVector();
        Vec3d flat = new Vec3d(look.x, 0, look.z);
        if (flat.lengthSquared() < 1.0E-6) return;
        flat = flat.normalize().multiply(0.458 * level);
        user.addVelocity(flat.x, 0.25, flat.z);
        user.velocityModified = true;
        if (user instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(player));
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient) return;
        int held = this.getMaxUseTime(stack) - remainingUseTicks;
        // Players' positions arrive from the client between ticks, so prevX is no use on the server:
        // remember where the charger (or what it's riding) was last tick instead.
        Entity mover = user.hasVehicle() ? user.getRootVehicle() : user;
        Vec3d now = mover.getPos();
        Vec3d last = LAST_POS.put(user, now);
        if (held < 10 || last == null) return;
        Vec3d motion = now.subtract(last);
        Vec3d look = user.getRotationVector();
        Vec3d eye = user.getEyePos();
        Vec3d end = eye.add(look.multiply(REACH));

        LivingEntity target = world.getEntitiesByClass(LivingEntity.class, new Box(eye, end).expand(1.0),
                        e -> e != user && e.isAlive() && !e.isSpectator() && e != user.getVehicle()
                                && e.getBoundingBox().expand(0.3).raycast(eye, end).isPresent())
                .stream().min(Comparator.comparingDouble(e -> e.squaredDistanceTo(user))).orElse(null);
        if (target == null) return;

        Vec3d targetMotion = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ);
        double speed = motion.subtract(targetMotion).dotProduct(look);
        if (speed < MIN_SPEED) return;

        float damage = (float) (speed * 20.0 * this.chargeMultiplier);
        DamageSource source = user instanceof PlayerEntity player ? world.getDamageSources().playerAttack(player) : world.getDamageSources().mobAttack(user);
        if (target.damage(source, damage)) {
            if (held < 100 && target.hasVehicle()) target.stopRiding();
            if (held < 200) target.takeKnockback(Math.min(2.0, speed * 2.0), -look.x, -look.z);
            world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_TRIDENT_HIT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            stack.damage(1, user, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        user.stopUsingItem();
        LAST_POS.remove(user);
        if (user instanceof PlayerEntity player) player.getItemCooldownManager().set(this, 20);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        LAST_POS.remove(user);
    }
}
