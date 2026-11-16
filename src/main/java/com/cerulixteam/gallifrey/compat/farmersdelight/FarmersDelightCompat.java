package com.cerulixteam.gallifrey.compat.farmersdelight;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.ModToolMaterials;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

/**
 * Farmer's Delight support (Farmer's Delight Refabricated, Fabric 1.20.1).
 *
 *  - Knives: steel, dalekanium, metalertanium and White Point Star knives. They only exist when
 *    Farmer's Delight is installed (otherwise these fields stay null and nothing is registered).
 *    Steel and dalekanium knives are crafted like FD's (ingot over a stick); metalertanium and
 *    White Point Star knives are smithed from the tier below, like the rest of those tools.
 *  - Cutting board: every Gallifrey log and wood strips on the cutting board with an axe, giving
 *    tree bark like vanilla logs. (data/gallifrey/recipes/compat/farmersdelight/)
 *
 * No compile-time dependency: the knives are plain Minecraft tools that FD recognises by tag,
 * and all the recipes and tags are data with load conditions.
 */
public final class FarmersDelightCompat {
    private FarmersDelightCompat() {}

    public static final String MOD_ID = "farmersdelight";

    public static Item STEEL_KNIFE;
    public static Item DALEKANIUM_KNIFE;
    public static Item METALERTANIUM_KNIFE;
    public static Item WHITE_POINT_STAR_KNIFE;

    public static boolean isLoaded() {
        return STEEL_KNIFE != null;
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) return;

        STEEL_KNIFE = knife("steel_knife", ModToolMaterials.STEEL, new FabricItemSettings());
        DALEKANIUM_KNIFE = knife("dalekanium_knife", ModToolMaterials.DALEKANIUM, new FabricItemSettings());
        METALERTANIUM_KNIFE = knife("metalertanium_knife", ModToolMaterials.METALERTANIUM,
                new FabricItemSettings().fireproof());
        WHITE_POINT_STAR_KNIFE = knife("white_point_star_knife", ModToolMaterials.WHITE_POINT_STAR,
                (FabricItemSettings) new FabricItemSettings().fireproof().rarity(Rarity.EPIC));

        GallifreyMod.LOGGER.info("[Gallifrey] Farmer's Delight found: added Gallifrey knives and cutting board recipes");
    }

    private static Item knife(String name, ModToolMaterials material, FabricItemSettings settings) {
        return Registry.register(Registries.ITEM, GallifreyMod.id(name), new GallifreyKnifeItem(material, settings));
    }
}
