package com.cerulixteam.gallifrey.block;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;

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