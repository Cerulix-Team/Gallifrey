package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisConsoleBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisInteriorDoorBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class GallifreyModBlockEntities {

// =========================================================
// TARDIS EXTERIOR
// =========================================================

    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    GallifreyMod.id("tardis_exterior"),
                    FabricBlockEntityTypeBuilder.create(
                            TardisExteriorBlockEntity::new,
                            GallifreyModBlocks.TARDIS_EXTERIOR
                    ).build()
            );

    public static final BlockEntityType<TardisConsoleBlockEntity> TARDIS_CONSOLE =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    GallifreyMod.id("tardis_console"),
                    FabricBlockEntityTypeBuilder.create(
                            TardisConsoleBlockEntity::new,
                            GallifreyModBlocks.TARDIS_CONSOLE
                    ).build()
            );

    public static final BlockEntityType<com.cerulixteam.gallifrey.block.entity.TardisMonitorBlockEntity> TARDIS_MONITOR =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, GallifreyMod.id("tardis_monitor"),
                    FabricBlockEntityTypeBuilder.create(com.cerulixteam.gallifrey.block.entity.TardisMonitorBlockEntity::new,
                            GallifreyModBlocks.TARDIS_MONITOR).build());

    public static final BlockEntityType<com.cerulixteam.gallifrey.block.entity.TardisThrottleBlockEntity> TARDIS_THROTTLE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, GallifreyMod.id("tardis_throttle"),
                    FabricBlockEntityTypeBuilder.create(com.cerulixteam.gallifrey.block.entity.TardisThrottleBlockEntity::new,
                            GallifreyModBlocks.TARDIS_THROTTLE).build());

    public static final BlockEntityType<TardisInteriorDoorBlockEntity> TARDIS_INTERIOR_DOOR =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    GallifreyMod.id("tardis_interior_door"),
                    FabricBlockEntityTypeBuilder.create(
                            TardisInteriorDoorBlockEntity::new,
                            GallifreyModBlocks.TARDIS_INTERIOR_DOOR
                    ).build()
            );

// =========================================================
// SONIC WORKSHOP
// =========================================================

    public static final BlockEntityType<SonicWorkshopBlockEntity> SONIC_WORKSHOP_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    GallifreyMod.id("sonic_workshop"),
                    FabricBlockEntityTypeBuilder.create(
                            SonicWorkshopBlockEntity::new,
                            GallifreyModBlocks.SONIC_WORKSHOP
                    ).build()
            );

// =========================================================
// REGISTER
// =========================================================

    public static void register() {

        GallifreyMod.LOGGER.info(
                "Registering Block Entities for "
                        + GallifreyMod.MOD_ID
        );
    }


}