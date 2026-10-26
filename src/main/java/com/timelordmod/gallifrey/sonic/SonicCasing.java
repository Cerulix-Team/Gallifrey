package com.timelordmod.gallifrey.sonic;

public enum SonicCasing {

    SECOND_DOCTOR(
            "second_doctor",
            "Troughton"
    ),

    THIRD_DOCTOR(
            "third_doctor",
            "Pertwee"
    ),

    FOURTH_DOCTOR(
            "fourth_doctor",
            "T.Baker"
    ),

    SIXTH_DOCTOR(
            "sixth_doctor",
            "C.Baker"
    ),

    EIGHTH_DOCTOR(
            "eighth_doctor",
            "Mcgann"
    ),

    WAR_DOCTOR(
            "war_doctor",
            "War Doctor"
    ),

    TENTH_DOCTOR(
            "tenth_doctor",
            "Tennant"
    ),

    WAR_MASTER(
            "war_master",
            "War Master"
    ),

    ELEVENTH_DOCTOR(
            "eleventh_doctor",
            "Smith"
    ),

    ELEVENTH_CANE(
            "eleventh_cane",
            "Copper Cane"
    ),

    TWELTH_DOCTOR(
            "twelth_doctor",
            "Capaldi"
    ),

    THIRTEENTH_DOCTOR(
            "therteenth_doctor",
            "Whittaker"
    ),

    FOURTEENTH_DOCTOR(
            "fourteenth_doctor",
            "Tennant Alt"
    ),

    FIFTEENTH_DOCTOR(
            "fifteenth_doctor",
            "Gatwa"
    ),

    FIFTEENTH_DOCTOR_GREEN(
            "fifteenth_doctor_green",
            "Gatwa"
    ),

    FIFTEENTH_DOCTOR_RED(
            "fifteenth_doctor_red",
            "Gatwa"
    ),

    FIFTEENTH_DOCTOR_YELLOW(
            "fifteenth_doctor_yellow",
            "Gatwa"
    ),


    FUGITIVE_DOCTOR(
            "fugutitive_doctor",
            "Fugutitive"
    ),

    LASER_SONIC(
            "laser_sonic",
            "Laser Sonic"
    ),

    RANI_SONIC(
            "rani_sonic",
            "Rani"
    ),

    MISSYS_UMBRELLA(
            "missys_umbrella",
            "Missy Umbrella"
    ),

    BLUE_SONIC(
            "blue_sonic",
            "Blue's Sonic"
    ),

    BLUNT_SONIC(
            "blunt_sonic",
            "Blunt"
    ),

    ALASTOR_STAFF(
            "alastor_staff",
            "Alastor's Staff"
    );

    private final String id;
    private final String displayName;

    SonicCasing(
            String id,
            String displayName
    ) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SonicCasing fromId(String id) {

        for (SonicCasing casing : values()) {

            if (casing.id.equals(id)) {
                return casing;
            }
        }

        // Default Sonic
        return THIRD_DOCTOR;
    }
}
