package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;

/**
 * Raid Omen (1.21). Bad Omen no longer starts a raid the moment you walk into a village: it turns into Raid
 * Omen, and the raid starts 30 seconds later. Milk clears it without a raid.
 */
public class RaidOmenEffect extends StatusEffect {
    public static final int DURATION = 600;

    public RaidOmenEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xDE4058);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration == 1;
    }

    /** The last tick: start the raid, with the Bad Omen level it came from. */
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayerEntity player) || player.isSpectator()) return;
        ServerWorld world = player.getServerWorld();
        if (world.getDifficulty() == Difficulty.PEACEFUL || !world.isNearOccupiedPointOfInterest(player.getBlockPos())) return;
        // Raid.start reads (and removes) the player's Bad Omen to set the raid's level.
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 2, amplifier, false, false, false));
        world.getRaidManager().startRaid(player);
        player.removeStatusEffect(StatusEffects.BAD_OMEN);
    }

    /** Called each server tick for players: Bad Omen in a village becomes Raid Omen (before vanilla starts a raid). */
    public static void convert(ServerPlayerEntity player) {
        StatusEffectInstance bad = player.getStatusEffect(StatusEffects.BAD_OMEN);
        if (bad == null || player.isSpectator() || player.hasStatusEffect(BackportExtras.RAID_OMEN)) return;
        ServerWorld world = player.getServerWorld();
        if (world.getDifficulty() == Difficulty.PEACEFUL || !world.getDimension().hasRaids()
                || world.getGameRules().getBoolean(net.minecraft.world.GameRules.DISABLE_RAIDS)
                || !world.isNearOccupiedPointOfInterest(player.getBlockPos())) return;
        if (world.getRaidAt(player.getBlockPos()) != null) return; // already a raid here: vanilla tops it up
        player.removeStatusEffect(StatusEffects.BAD_OMEN);
        player.addStatusEffect(new StatusEffectInstance(BackportExtras.RAID_OMEN, DURATION, bad.getAmplifier(), false, false, true));
        world.playSound(null, player.getBlockPos(), BackportSounds.EVENT_MOB_EFFECT_RAID_OMEN, net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
