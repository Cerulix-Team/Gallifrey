package com.cerulixteam.gallifrey;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class GallifreySounds {

    public static final SoundEvent VM_TAKE_OFF = registerSound("vm_take_off");
    public static final SoundEvent VM_LAND = registerSound("vm_land");
    public static final SoundEvent SONIC = registerSound("sonic");

    public static final SoundEvent DWXIV = registerSound("dw_xiv_music");
    public static final SoundEvent GALLIFREY = registerSound("gallifrey_music");
    public static final SoundEvent MONDAS_BLIZZARD_WIND = registerSound("mondas_blizzard_wind");

    // Classic Minecraft sound set
    public static final SoundEvent CLASSIC_STONE_STEP_0 = registerSound("classic_stone_step_0");
    public static final SoundEvent CLASSIC_STONE_STEP_1 = registerSound("classic_stone_step_1");
    public static final SoundEvent CLASSIC_STONE_STEP_2 = registerSound("classic_stone_step_2");
    public static final SoundEvent CLASSIC_STONE_STEP_3 = registerSound("classic_stone_step_3");
    public static final SoundEvent CLASSIC_GRASS_STEP_0 = registerSound("classic_grass_step_0");
    public static final SoundEvent CLASSIC_GRASS_STEP_1 = registerSound("classic_grass_step_1");
    public static final SoundEvent CLASSIC_GRASS_STEP_2 = registerSound("classic_grass_step_2");
    public static final SoundEvent CLASSIC_GRASS_STEP_3 = registerSound("classic_grass_step_3");
    public static final SoundEvent CLASSIC_WOOD_STEP_0 = registerSound("classic_wood_step_0");
    public static final SoundEvent CLASSIC_WOOD_STEP_1 = registerSound("classic_wood_step_1");
    public static final SoundEvent CLASSIC_WOOD_STEP_2 = registerSound("classic_wood_step_2");
    public static final SoundEvent CLASSIC_WOOD_STEP_3 = registerSound("classic_wood_step_3");
    public static final SoundEvent CLASSIC_GRAVEL_STEP_0 = registerSound("classic_gravel_step_0");
    public static final SoundEvent CLASSIC_GRAVEL_STEP_1 = registerSound("classic_gravel_step_1");
    public static final SoundEvent CLASSIC_GRAVEL_STEP_2 = registerSound("classic_gravel_step_2");
    public static final SoundEvent CLASSIC_GRAVEL_STEP_3 = registerSound("classic_gravel_step_3");
    public static final SoundEvent CLASSIC_HURT = registerSound("classic_hurt");
    public static final SoundEvent CLASSIC_EXPLOSION = registerSound("classic_explosion");
    public static final SoundEvent DALEK_CLASSIC_ATTACK = registerSound("dalek_classic_attack");

    public static final SoundEvent BLOOP = registerSound("bloop");
    public static final SoundEvent CLASSIC1 = registerSound("classic1");
    public static final SoundEvent CLASSIC2 = registerSound("classic2");
    public static final SoundEvent CYBERMAN_SHOOT = registerSound("cyberman_shoot");
    public static final SoundEvent CYBERMAN_STEP = registerSound("cyberman_step");
    public static final SoundEvent DALEK_ACTION_AMBIENT_0 = registerSound("dalek_action_ambient_0");
    public static final SoundEvent DALEK_ACTION_AMBIENT_1 = registerSound("dalek_action_ambient_1");
    public static final SoundEvent DALEK_ACTION_ATTACK_0 = registerSound("dalek_action_attack_0");
    public static final SoundEvent DALEK_ACTION_ATTACK_1 = registerSound("dalek_action_attack_1");
    public static final SoundEvent DALEK_ACTION_ATTACK_2 = registerSound("dalek_action_attack_2");
    public static final SoundEvent DALEK_BEAM_SHOOT = registerSound("dalek_beam_shoot");
    public static final SoundEvent DALEK_BULLET_SHOOT = registerSound("dalek_bullet_shoot");
    public static final SoundEvent DALEK_CANNON_CHARGE = registerSound("dalek_cannon_charge");
    public static final SoundEvent DALEK_CANNON_SHOOT = registerSound("dalek_cannon_shoot");
    public static final SoundEvent DALEK_DOOR_CLOSE = registerSound("dalek_door_close");
    public static final SoundEvent DALEK_DOOR_OPEN = registerSound("dalek_door_open");
    public static final SoundEvent DALEK_EYESTALK_0 = registerSound("dalek_eyestalk_0");
    public static final SoundEvent DALEK_EYESTALK_1 = registerSound("dalek_eyestalk_1");
    public static final SoundEvent DALEK_EYESTALK_2 = registerSound("dalek_eyestalk_2");
    public static final SoundEvent DALEK_EYESTALK_3 = registerSound("dalek_eyestalk_3");
    public static final SoundEvent DALEK_FLAME_THROWER_SHOOT = registerSound("dalek_flame_thrower_shoot");
    public static final SoundEvent DALEK_GLIDE = registerSound("dalek_glide");
    public static final SoundEvent DALEK_GLIDE_START = registerSound("dalek_glide_start");
    public static final SoundEvent DALEK_GUNSTICK_CHARGE = registerSound("dalek_gunstick_charge");
    public static final SoundEvent DALEK_GUNSTICK_SHOOT = registerSound("dalek_gunstick_shoot");
    public static final SoundEvent DALEK_HOVER = registerSound("dalek_hover");
    public static final SoundEvent DALEK_HOVER_START = registerSound("dalek_hover_start");
    public static final SoundEvent DALEK_HURT_0 = registerSound("dalek_hurt_0");
    public static final SoundEvent DALEK_HURT_1 = registerSound("dalek_hurt_1");
    public static final SoundEvent DALEK_HURT_2 = registerSound("dalek_hurt_2");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE = registerSound("dalek_imperial_exterminate");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_2 = registerSound("dalek_imperial_exterminate_2");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_3 = registerSound("dalek_imperial_exterminate_3");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_4 = registerSound("dalek_imperial_exterminate_4");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_5 = registerSound("dalek_imperial_exterminate_5");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_6 = registerSound("dalek_imperial_exterminate_6");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_7 = registerSound("dalek_imperial_exterminate_7");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_8 = registerSound("dalek_imperial_exterminate_8");
    public static final SoundEvent DALEK_IMPERIAL_EXTERMINATE_9 = registerSound("dalek_imperial_exterminate_9");
    public static final SoundEvent DALEK_IMPERIAL_STAY_WHERE_YOU_ARE = registerSound("dalek_imperial_stay_where_you_are");
    public static final SoundEvent DALEK_LASER_SHOOT = registerSound("dalek_laser_shoot");
    public static final SoundEvent DALEK_PARADIGM_GLIDE = registerSound("dalek_paradigm_glide");
    public static final SoundEvent DALEK_PARADIGM_HOVER = registerSound("dalek_paradigm_hover");
    public static final SoundEvent DALEK_PARADIGM_LASER_SHOOT = registerSound("dalek_paradigm_laser_shoot");
    public static final SoundEvent DALEK_ROTATE_0 = registerSound("dalek_rotate_0");
    public static final SoundEvent DALEK_ROTATE_1 = registerSound("dalek_rotate_1");
    public static final SoundEvent DALEK_SKARO_AMBIENT_0 = registerSound("dalek_skaro_ambient_0");
    public static final SoundEvent DALEK_SKARO_AMBIENT_1 = registerSound("dalek_skaro_ambient_1");
    public static final SoundEvent DALEK_SKARO_AMBIENT_2 = registerSound("dalek_skaro_ambient_2");
    public static final SoundEvent DALEK_SKARO_AMBIENT_3 = registerSound("dalek_skaro_ambient_3");
    public static final SoundEvent DALEK_SKARO_AMBIENT_4 = registerSound("dalek_skaro_ambient_4");
    public static final SoundEvent DALEK_SKARO_AMBIENT_5 = registerSound("dalek_skaro_ambient_5");
    public static final SoundEvent DALEK_SKARO_ATTACK_0 = registerSound("dalek_skaro_attack_0");
    public static final SoundEvent DALEK_SKARO_ATTACK_1 = registerSound("dalek_skaro_attack_1");
    public static final SoundEvent DALEK_SKARO_ATTACK_2 = registerSound("dalek_skaro_attack_2");
    public static final SoundEvent DALEK_SKARO_ATTACK_3 = registerSound("dalek_skaro_attack_3");
    public static final SoundEvent DALEK_SKARO_ATTACK_4 = registerSound("dalek_skaro_attack_4");
    public static final SoundEvent DALEK_SKARO_ATTACK_5 = registerSound("dalek_skaro_attack_5");
    public static final SoundEvent DALEK_SKARO_ATTACK_6 = registerSound("dalek_skaro_attack_6");
    public static final SoundEvent DALEK_SMOKE_SHOOT = registerSound("dalek_smoke_shoot");
    public static final SoundEvent DALEK_SPARK_SHOOT = registerSound("dalek_spark_shoot");
    public static final SoundEvent DALEK_SPECIAL_WEAPONS_CHARGE = registerSound("dalek_special_weapons_charge");
    public static final SoundEvent DALEK_SPECIAL_WEAPONS_SHOOT = registerSound("dalek_special_weapons_shoot");
    public static final SoundEvent DALEK_TIME_WAR_ATTACK_0 = registerSound("dalek_time_war_attack_0");
    public static final SoundEvent DALEK_TIME_WAR_ATTACK_1 = registerSound("dalek_time_war_attack_1");
    public static final SoundEvent DALEK_TIME_WAR_ATTACK_2 = registerSound("dalek_time_war_attack_2");
    public static final SoundEvent DALEK_TIME_WAR_ATTACK_3 = registerSound("dalek_time_war_attack_3");
    public static final SoundEvent DALEK_TIME_WAR_ATTACK_4 = registerSound("dalek_time_war_attack_4");
    public static final SoundEvent DOCTORWHO1411 = registerSound("doctorwho1411");
    public static final SoundEvent DOCTORWHOXV = registerSound("doctorwhoxv");
    public static final SoundEvent DRWHOVALE = registerSound("drwhovale");
    public static final SoundEvent DUGGA_DOO = registerSound("dugga_doo");
    public static final SoundEvent DUGGAO_DOO = registerSound("duggao_doo");
    public static final SoundEvent EMERGENCY_LAND = registerSound("emergency_land");
    public static final SoundEvent PARADOXMAT = registerSound("paradoxmat");
    public static final SoundEvent POLICE_BOX_DOOR_CLOSE = registerSound("police_box_door_close");
    public static final SoundEvent POLICE_BOX_DOOR_OPEN = registerSound("police_box_door_open");
    public static final SoundEvent SONIC_ELEVENTH = registerSound("sonic_eleventh");
    public static final SoundEvent SONIC_FIFTH = registerSound("sonic_fifth");
    public static final SoundEvent SONIC_FOURTEEN = registerSound("sonic_fourteen");
    public static final SoundEvent SONIC_FOURTH = registerSound("sonic_fourth");
    public static final SoundEvent SONIC_NINTH = registerSound("sonic_ninth");
    public static final SoundEvent SONIC_SECOND = registerSound("sonic_second");
    public static final SoundEvent SONIC_SEVEN = registerSound("sonic_seven");
    public static final SoundEvent SONIC_SIXTH = registerSound("sonic_sixth");
    public static final SoundEvent SONIC_THIRD = registerSound("sonic_third");
    public static final SoundEvent SONIC_THIRTEEN = registerSound("sonic_thirteen");
    public static final SoundEvent TYPE70DEMAT = registerSound("type70demat");
    public static final SoundEvent TYPE70FLIGHT = registerSound("type70flight");
    public static final SoundEvent TYPE70MAT = registerSound("type70mat");

    private static SoundEvent registerSound(String name) {
        Identifier id = new Identifier(GallifreyMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
        // Registers the sounds
    }
}

