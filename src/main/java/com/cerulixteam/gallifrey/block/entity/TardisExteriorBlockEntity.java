package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class TardisExteriorBlockEntity extends BlockEntity {

    private Identifier tardisDimension;

    private boolean interiorGenerated = false;

    public TardisExteriorBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                GallifreyModBlockEntities.TARDIS_EXTERIOR,
                pos,
                state
        );
    }

    public Identifier getTardisDimension() {
        return tardisDimension;
    }

    public void setTardisDimension(
            Identifier dimension
    ) {
        this.tardisDimension = dimension;
        markDirty();
    }

    public boolean isInteriorGenerated() {
        return interiorGenerated;
    }

    public void generateInterior(ServerWorld world) {

        // Don't generate the interior more than once.
        if (interiorGenerated) {
            return;
        }

        Identifier structureId =
                new Identifier(
                        "gallifrey",
                        "interiors/tardis_platform"
                );

        Optional<StructureTemplate> structureOptional =
                world.getStructureTemplateManager()
                        .getTemplate(structureId);

        // Structure wasn't found.
        if (structureOptional.isEmpty()) {
            System.out.println(
                    "[GALLIFREY] ERROR: Could not find TARDIS structure!"
            );
            return;
        }

        StructureTemplate structure =
                structureOptional.get();

        // Position where the structure will be placed.
        BlockPos structurePos =
                new BlockPos(0, 64, 0);

        StructurePlacementData placementData =
                new StructurePlacementData();

        structure.place(
                world,
                structurePos,
                structurePos,
                placementData,
                world.getRandom(),
                2
        );

        interiorGenerated = true;

        markDirty();

        System.out.println(
                "[GALLIFREY] TARDIS interior generated!"
        );
    }

    @Override
    protected void writeNbt(
            NbtCompound nbt
    ) {
        super.writeNbt(nbt);

        if (tardisDimension != null) {
            nbt.putString(
                    "TardisDimension",
                    tardisDimension.toString()
            );
        }

        nbt.putBoolean(
                "InteriorGenerated",
                interiorGenerated
        );
    }

    @Override
    public void readNbt(
            NbtCompound nbt
    ) {
        super.readNbt(nbt);

        if (nbt.contains("TardisDimension")) {
            tardisDimension =
                    new Identifier(
                            nbt.getString(
                                    "TardisDimension"
                            )
                    );
        }

        interiorGenerated =
                nbt.getBoolean(
                        "InteriorGenerated"
                );
    }
}
