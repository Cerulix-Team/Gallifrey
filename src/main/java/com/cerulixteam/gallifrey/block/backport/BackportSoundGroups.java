package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import static com.cerulixteam.gallifrey.block.backport.BackportSounds.*;

/** Block sound groups for the backported blocks, from their own 1.20.2+ sounds (see BackportSounds). */
public final class BackportSoundGroups {
    private BackportSoundGroups() {}

    private static BlockSoundGroup of(SoundEvent breakSound, SoundEvent step, SoundEvent place, SoundEvent hit, SoundEvent fall) {
        return new BlockSoundGroup(1.0F, 1.0F, breakSound, step, place, hit, fall);
    }

    public static final BlockSoundGroup POLISHED_TUFF = of(BLOCK_POLISHED_TUFF_BREAK, BLOCK_POLISHED_TUFF_STEP, BLOCK_POLISHED_TUFF_PLACE,
            BLOCK_POLISHED_TUFF_HIT, BLOCK_POLISHED_TUFF_FALL);
    public static final BlockSoundGroup TUFF_BRICKS = of(BLOCK_TUFF_BRICKS_BREAK, BLOCK_TUFF_BRICKS_STEP, BLOCK_TUFF_BRICKS_PLACE,
            BLOCK_TUFF_BRICKS_HIT, BLOCK_TUFF_BRICKS_FALL);
    public static final BlockSoundGroup COPPER_BULB = of(BLOCK_COPPER_BULB_BREAK, BLOCK_COPPER_BULB_STEP, BLOCK_COPPER_BULB_PLACE,
            BLOCK_COPPER_BULB_HIT, BLOCK_COPPER_BULB_FALL);
    public static final BlockSoundGroup COPPER_GRATE = of(BLOCK_COPPER_GRATE_BREAK, BLOCK_COPPER_GRATE_STEP, BLOCK_COPPER_GRATE_PLACE,
            BLOCK_COPPER_GRATE_HIT, BLOCK_COPPER_GRATE_FALL);
    public static final BlockSoundGroup HEAVY_CORE = of(BLOCK_HEAVY_CORE_BREAK, BLOCK_HEAVY_CORE_STEP, BLOCK_HEAVY_CORE_PLACE,
            BLOCK_HEAVY_CORE_HIT, BLOCK_HEAVY_CORE_FALL);
    public static final BlockSoundGroup RESIN = of(BLOCK_RESIN_BREAK, BLOCK_RESIN_STEP, BLOCK_RESIN_PLACE, SoundEvents.BLOCK_MUD_HIT, BLOCK_RESIN_FALL);
    public static final BlockSoundGroup RESIN_BRICKS = of(BLOCK_RESIN_BRICKS_BREAK, BLOCK_RESIN_BRICKS_STEP, BLOCK_RESIN_BRICKS_PLACE,
            BLOCK_RESIN_BRICKS_HIT, BLOCK_RESIN_BRICKS_FALL);
    public static final BlockSoundGroup CREAKING_HEART = of(BLOCK_CREAKING_HEART_BREAK, BLOCK_CREAKING_HEART_STEP, BLOCK_CREAKING_HEART_PLACE,
            BLOCK_CREAKING_HEART_HIT, BLOCK_CREAKING_HEART_FALL);
    public static final BlockSoundGroup TRIAL_SPAWNER = of(BLOCK_TRIAL_SPAWNER_BREAK, BLOCK_TRIAL_SPAWNER_STEP, BLOCK_TRIAL_SPAWNER_PLACE,
            BLOCK_TRIAL_SPAWNER_HIT, BLOCK_TRIAL_SPAWNER_FALL);
    public static final BlockSoundGroup VAULT = of(BLOCK_VAULT_BREAK, BLOCK_VAULT_STEP, BLOCK_VAULT_PLACE, BLOCK_VAULT_HIT, BLOCK_VAULT_FALL);
    public static final BlockSoundGroup LEAF_LITTER = of(BLOCK_LEAF_LITTER_BREAK, BLOCK_LEAF_LITTER_STEP, BLOCK_LEAF_LITTER_PLACE,
            BLOCK_LEAF_LITTER_HIT, BLOCK_LEAF_LITTER_FALL);
    public static final BlockSoundGroup IRON = of(BLOCK_IRON_BREAK, BLOCK_IRON_STEP, BLOCK_IRON_PLACE, BLOCK_IRON_HIT, BLOCK_IRON_FALL);
}
