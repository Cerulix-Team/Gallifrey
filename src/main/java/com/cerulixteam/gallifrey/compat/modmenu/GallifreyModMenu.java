package com.cerulixteam.gallifrey.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration: gives Gallifrey a working "Config" button in the mods list.
 *
 * Mod Menu finds this through the "modmenu" entrypoint in fabric.mod.json, so it's only loaded
 * when Mod Menu is installed. Nothing else in the mod may reference this package. The links and
 * badges Mod Menu shows come from the "contact" and "custom.modmenu" parts of fabric.mod.json.
 */
public class GallifreyModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GallifreyConfigScreen::new;
    }
}
