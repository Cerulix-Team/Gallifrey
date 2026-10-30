package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Dalekanium, metalertanium and White Point Star boots let you float down gently and never take
 * fall damage.
 *
 *  - While you're falling in them you get Slow Falling, with no particles and no icon, so it just
 *    feels like a property of the boots. It's topped up every tick and wears off half a second
 *    after you land or take them off.
 *  - Hold sneak to drop at normal speed instead (handy for going down a shaft quickly). You
 *    still take no fall damage when you land.
 *  - Fall damage is cancelled outright while wearing them, so landing never hurts even if the
 *    slow falling was skipped (sneaking, an elytra dive, teleporting mid-air).
 *
 * Players only for the slow falling; any mob wearing the boots also ignores fall damage.
 */
public final class HoverBoots {
    private HoverBoots() {}

    /** Short, so it ends soon after landing; refreshed every tick while falling. */
    private static final int EFFECT_TICKS = 10;

    public static boolean isWearing(LivingEntity entity) {
        Item boots = entity.getEquippedStack(EquipmentSlot.FEET).getItem();
        return boots == GallifreyModItems.DALEKANIUM_BOOTS
                || boots == GallifreyModItems.METALERTANIUM_BOOTS
                || boots == GallifreyModItems.WHITE_POINT_STAR_BOOTS;
    }

    public static void register() {
        // No fall damage in the boots.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(source.isIn(DamageTypeTags.IS_FALL) && isWearing(entity)));

        // Gentle descent while falling.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (shouldFloat(player)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SLOW_FALLING, EFFECT_TICKS, 0,
                            true,    // ambient
                            false,   // no particles
                            false)); // no HUD icon
                }
            }
        });
    }

    private static boolean shouldFloat(ServerPlayerEntity player) {
        return isWearing(player)
                && !player.isOnGround()
                && player.getVelocity().y < 0
                && !player.isSneaking()
                && !player.isFallFlying()
                && !player.getAbilities().flying
                && !player.isTouchingWater()
                && !player.isInLava()
                && !player.isClimbing();
    }
}
