package com.cerulixteam.gallifrey.tardis;

import com.cerulixteam.gallifrey.GallifreyMod;
import net.minecraft.util.Identifier;

public final class TardisDimensionManager {

    private TardisDimensionManager() {
    }

    public static Identifier getDimensionId(long tardisId) {
        return new Identifier(
                GallifreyMod.MOD_ID,
                "tardis_" + tardisId
        );
    }
}

