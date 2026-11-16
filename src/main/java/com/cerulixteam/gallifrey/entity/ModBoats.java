package com.cerulixteam.gallifrey.entity;

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;
import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModBoats {
    public static final Identifier TARDIS_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "tardis_boat");
    public static final Identifier TARDIS_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "tardis_chest_boat");

    public static final Identifier ULANDA_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "ulanda_boat");
    public static final Identifier ULANDA_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "ulanda_chest_boat");

    public static final Identifier TREEBORG_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "treeborg_boat");
    public static final Identifier TREEBORG_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "treeborg_chest_boat");

    public static final Identifier ASH_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "ash_boat");
    public static final Identifier ASH_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "ash_chest_boat");

    public static final Identifier MAPLE_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "maple_boat");
    public static final Identifier MAPLE_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "maple_chest_boat");

    public static final Identifier MOONPINE_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "moonpine_boat");
    public static final Identifier MOONPINE_CHEST_BOAT_ID = new Identifier(GallifreyMod.MOD_ID, "moonpine_chest_boat");




    public static final RegistryKey<TerraformBoatType> TARDIS_BOAT_KEY = TerraformBoatTypeRegistry.createKey(TARDIS_BOAT_ID);
    public static final RegistryKey<TerraformBoatType> ULANDA_BOAT_KEY = TerraformBoatTypeRegistry.createKey(ULANDA_BOAT_ID);
    public static final RegistryKey<TerraformBoatType> TREEBORG_BOAT_KEY = TerraformBoatTypeRegistry.createKey(TREEBORG_BOAT_ID);
    public static final RegistryKey<TerraformBoatType> ASH_BOAT_KEY = TerraformBoatTypeRegistry.createKey(ASH_BOAT_ID);
    public static final RegistryKey<TerraformBoatType> MAPLE_BOAT_KEY = TerraformBoatTypeRegistry.createKey(MAPLE_BOAT_ID);
    public static final RegistryKey<TerraformBoatType> MOONPINE_BOAT_KEY = TerraformBoatTypeRegistry.createKey(MOONPINE_BOAT_ID);


    public static void registerBoats() {
        TerraformBoatType tardisBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.TARDIS_BOAT)
                .chestItem(GallifreyModItems.TARDIS_CHEST_BOAT)
                .planks(GallifreyModBlocks.TARDIS_PLANKS.asItem())
                .build();

        TerraformBoatType ulandaBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.ULANDA_BOAT)
                .chestItem(GallifreyModItems.ULANDA_CHEST_BOAT)
                .planks(GallifreyModBlocks.ULANDA_PLANKS.asItem())
                .build();

        TerraformBoatType treeborgBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.TREEBORG_BOAT)
                .chestItem(GallifreyModItems.TREEBORG_CHEST_BOAT)
                .planks(GallifreyModBlocks.TREEBORG_PLANKS.asItem())
                .build();

        TerraformBoatType ashBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.ASH_BOAT)
                .chestItem(GallifreyModItems.ASH_CHEST_BOAT)
                .planks(GallifreyModBlocks.ASH_PLANKS.asItem())
                .build();

        TerraformBoatType mapleBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.MAPLE_BOAT)
                .chestItem(GallifreyModItems.MAPLE_CHEST_BOAT)
                .planks(GallifreyModBlocks.MAPLE_PLANKS.asItem())
                .build();

        TerraformBoatType moonpineBoat = new TerraformBoatType.Builder()
                .item(GallifreyModItems.MOONPINE_BOAT)
                .chestItem(GallifreyModItems.MOONPINE_CHEST_BOAT)
                .planks(GallifreyModBlocks.MOONPINE_PLANKS.asItem())
                .build();


        Registry.register(TerraformBoatTypeRegistry.INSTANCE, ULANDA_BOAT_KEY, ulandaBoat);
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, TARDIS_BOAT_KEY, tardisBoat);
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, TREEBORG_BOAT_KEY, treeborgBoat);
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, ASH_BOAT_KEY, ashBoat);
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, MAPLE_BOAT_KEY, mapleBoat);
        Registry.register(TerraformBoatTypeRegistry.INSTANCE, MOONPINE_BOAT_KEY, moonpineBoat);
    }
}
