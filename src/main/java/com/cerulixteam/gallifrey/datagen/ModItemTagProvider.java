package com.cerulixteam.gallifrey.datagen;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ItemTags.WOODEN_BUTTONS).add(GallifreyModBlocks.CLASSIC_WOODEN_BUTTON.asItem());
        getOrCreateTagBuilder(ItemTags.WOODEN_PRESSURE_PLATES).add(GallifreyModBlocks.CLASSIC_WOODEN_PRESSURE_PLATE.asItem());
        getOrCreateTagBuilder(ItemTags.STONE_BUTTONS).add(GallifreyModBlocks.CLASSIC_STONE_BUTTON.asItem());

        getOrCreateTagBuilder(ItemTags.PLANKS)
                .add(GallifreyModBlocks.TARDIS_PLANKS.asItem())
                .add(GallifreyModBlocks.TREEBORG_PLANKS.asItem())
                .add(GallifreyModBlocks.ULANDA_PLANKS.asItem())
                .add(GallifreyModBlocks.ASH_PLANKS.asItem())
                .add(GallifreyModBlocks.MAPLE_PLANKS.asItem())
                .add(GallifreyModBlocks.MOONPINE_PLANKS.asItem())
                .add(GallifreyModBlocks.WASTED_PLANKS.asItem())
                .add(GallifreyModBlocks.PREHISTORIC_PLANKS.asItem());

        getOrCreateTagBuilder(ItemTags.WALLS)
                .add(GallifreyModBlocks.SKARO_STONE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_COBBLESTONE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_ANDESITE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_DIORITE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_GRANITE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL.asItem())
                .add(GallifreyModBlocks.POLISHED_SKARO_STONE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_DEEPSLATE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL.asItem())
                .add(GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL.asItem());

        getOrCreateTagBuilder(ItemTags.MUSIC_DISCS)
                .add(GallifreyModItems.DW_XIV_MUSIC_DISC)
                .add(GallifreyModItems.GALLIFREY_MUSIC_DISC)
                .add(GallifreyModItems.VALEDECEM_MUSIC_DISC)
                .add(GallifreyModItems.DUGGA_DOO_MUSIC_DISC);

        getOrCreateTagBuilder(ItemTags.CREEPER_DROP_MUSIC_DISCS)
                .add(GallifreyModItems.DW_XIV_MUSIC_DISC)
                .add(GallifreyModItems.GALLIFREY_MUSIC_DISC)
                .add(GallifreyModItems.VALEDECEM_MUSIC_DISC)
                .add(GallifreyModItems.DUGGA_DOO_MUSIC_DISC);
    }
}