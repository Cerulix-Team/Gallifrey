package com.cerulixteam.gallifrey.datagen;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.When;
import net.minecraft.state.property.Properties;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModModelProvider extends FabricModelProvider {

    private static final String SKARO_TEXTURE_FOLDER = "block/skaro/";
    private record StoneSet(Block base, Block stairs, Block slab, Block wall) {}

    private static final List<StoneSet> SKARO_SETS = List.of(
            new StoneSet(GallifreyModBlocks.SKARO_STONE, GallifreyModBlocks.SKARO_STONE_STAIRS, GallifreyModBlocks.SKARO_STONE_SLAB, GallifreyModBlocks.SKARO_STONE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_COBBLESTONE, GallifreyModBlocks.SKARO_COBBLESTONE_STAIRS, GallifreyModBlocks.SKARO_COBBLESTONE_SLAB, GallifreyModBlocks.SKARO_COBBLESTONE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_ANDESITE, GallifreyModBlocks.SKARO_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_ANDESITE_SLAB, GallifreyModBlocks.SKARO_ANDESITE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_DIORITE, GallifreyModBlocks.SKARO_DIORITE_STAIRS, GallifreyModBlocks.SKARO_DIORITE_SLAB, GallifreyModBlocks.SKARO_DIORITE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_GRANITE, GallifreyModBlocks.SKARO_GRANITE_STAIRS, GallifreyModBlocks.SKARO_GRANITE_SLAB, GallifreyModBlocks.SKARO_GRANITE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_POLISHED_ANDESITE, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_SLAB, GallifreyModBlocks.SKARO_POLISHED_ANDESITE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_POLISHED_DIORITE, GallifreyModBlocks.SKARO_POLISHED_DIORITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_DIORITE_SLAB, GallifreyModBlocks.SKARO_POLISHED_DIORITE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_POLISHED_GRANITE, GallifreyModBlocks.SKARO_POLISHED_GRANITE_STAIRS, GallifreyModBlocks.SKARO_POLISHED_GRANITE_SLAB, GallifreyModBlocks.SKARO_POLISHED_GRANITE_WALL),
            new StoneSet(GallifreyModBlocks.POLISHED_SKARO_STONE, GallifreyModBlocks.POLISHED_SKARO_STONE_STAIRS, GallifreyModBlocks.POLISHED_SKARO_STONE_SLAB, GallifreyModBlocks.POLISHED_SKARO_STONE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_DEEPSLATE, GallifreyModBlocks.SKARO_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_SLAB, GallifreyModBlocks.SKARO_DEEPSLATE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_STAIRS, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_SLAB, GallifreyModBlocks.SKARO_COBBLED_DEEPSLATE_WALL),
            new StoneSet(GallifreyModBlocks.SKARO_DEEPSLATE_TILES, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_STAIRS, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_SLAB, GallifreyModBlocks.SKARO_DEEPSLATE_TILES_WALL)
    );

    private static final String CLASSIC_TEXTURE_FOLDER = "block/classic_minecraft/";
    private record Textured(Block block, String texture) {}
    private record Pane(Block pane, String texture, String edgeTexture) {}

    private static final List<Textured> CLASSIC_BUTTONS = List.of(
            new Textured(GallifreyModBlocks.CLASSIC_WOODEN_BUTTON, "classic_planks"),
            new Textured(GallifreyModBlocks.CLASSIC_STONE_BUTTON, "classic_stone")
    );

    private static final List<Textured> CLASSIC_PRESSURE_PLATES = List.of(
            new Textured(GallifreyModBlocks.CLASSIC_WOODEN_PRESSURE_PLATE, "classic_planks"),
            new Textured(GallifreyModBlocks.CLASSIC_STONE_PRESSURE_PLATE, "classic_stone")
    );

    private static final List<Pane> CLASSIC_PANES = List.of(
            new Pane(GallifreyModBlocks.CLASSIC_GLASS_PANE, "classic_glass", "classic_glass_pane_top")
    );

    private static Identifier classic(String texture) {
        return new Identifier("gallifrey", CLASSIC_TEXTURE_FOLDER + texture);
    }

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        for (StoneSet set : SKARO_SETS) {
            // The texture is named after the full block, e.g. block/skaro/skaro_stone.png
            Identifier texture = new Identifier("gallifrey",
                    SKARO_TEXTURE_FOLDER + Registries.BLOCK.getId(set.base()).getPath());

            registerStairs(generator, set.stairs(), texture);
            registerSlab(generator, set.slab(), set.base(), texture);
            registerWall(generator, set.wall(), texture);
        }

        for (Textured button : CLASSIC_BUTTONS) registerButton(generator, button.block(), classic(button.texture()));
        for (Textured plate : CLASSIC_PRESSURE_PLATES) registerPressurePlate(generator, plate.block(), classic(plate.texture()));
        for (Pane pane : CLASSIC_PANES) registerPane(generator, pane.pane(), classic(pane.texture()), classic(pane.edgeTexture()));
    }

    private static void registerButton(BlockStateModelGenerator generator, Block button, Identifier texture) {
        TextureMap textures = TextureMap.texture(texture);
        Identifier up = Models.BUTTON.upload(button, textures, generator.modelCollector);
        Identifier pressed = Models.BUTTON_PRESSED.upload(button, textures, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createButtonBlockState(button, up, pressed));
        // A placed button looks tiny in the hand, so the item uses the special inventory model.
        Identifier inventory = Models.BUTTON_INVENTORY.upload(button, textures, generator.modelCollector);
        generator.registerParentedItemModel(button, inventory);
    }

    private static void registerPressurePlate(BlockStateModelGenerator generator, Block plate, Identifier texture) {
        TextureMap textures = TextureMap.texture(texture);
        Identifier up = Models.PRESSURE_PLATE_UP.upload(plate, textures, generator.modelCollector);
        Identifier down = Models.PRESSURE_PLATE_DOWN.upload(plate, textures, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createPressurePlateBlockState(plate, up, down));
        // The item model (parent: the raised plate model) is generated automatically.
    }

    /** Same models and blockstate vanilla uses for glass panes, but with our own texture paths. */
    private static void registerPane(BlockStateModelGenerator generator, Block pane, Identifier texture, Identifier edge) {
        TextureMap textures = new TextureMap().put(TextureKey.PANE, texture).put(TextureKey.EDGE, edge);
        Identifier post = Models.TEMPLATE_GLASS_PANE_POST.upload(pane, textures, generator.modelCollector);
        Identifier side = Models.TEMPLATE_GLASS_PANE_SIDE.upload(pane, textures, generator.modelCollector);
        Identifier sideAlt = Models.TEMPLATE_GLASS_PANE_SIDE_ALT.upload(pane, textures, generator.modelCollector);
        Identifier noSide = Models.TEMPLATE_GLASS_PANE_NOSIDE.upload(pane, textures, generator.modelCollector);
        Identifier noSideAlt = Models.TEMPLATE_GLASS_PANE_NOSIDE_ALT.upload(pane, textures, generator.modelCollector);

        // The item is the flat glass picture, like vanilla's glass pane item.
        Models.GENERATED.upload(ModelIds.getItemModelId(pane.asItem()),
                new TextureMap().put(TextureKey.LAYER0, texture), generator.modelCollector);

        generator.blockStateCollector.accept(MultipartBlockStateSupplier.create(pane)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, post))
                .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, side))
                .with(When.create().set(Properties.EAST, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, sideAlt))
                .with(When.create().set(Properties.WEST, true), BlockStateVariant.create().put(VariantSettings.MODEL, sideAlt).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.create().set(Properties.NORTH, false), BlockStateVariant.create().put(VariantSettings.MODEL, noSide))
                .with(When.create().set(Properties.EAST, false), BlockStateVariant.create().put(VariantSettings.MODEL, noSideAlt))
                .with(When.create().set(Properties.SOUTH, false), BlockStateVariant.create().put(VariantSettings.MODEL, noSideAlt).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.create().set(Properties.WEST, false), BlockStateVariant.create().put(VariantSettings.MODEL, noSide).put(VariantSettings.Y, VariantSettings.Rotation.R270)));
    }

    private static TextureMap sides(Identifier texture) {
        return new TextureMap()
                .put(TextureKey.BOTTOM, texture)
                .put(TextureKey.TOP, texture)
                .put(TextureKey.SIDE, texture);
    }

    private static void registerStairs(BlockStateModelGenerator generator, Block stairs, Identifier texture) {
        TextureMap textures = sides(texture);
        Identifier inner = Models.INNER_STAIRS.upload(stairs, textures, generator.modelCollector);
        Identifier straight = Models.STAIRS.upload(stairs, textures, generator.modelCollector);
        Identifier outer = Models.OUTER_STAIRS.upload(stairs, textures, generator.modelCollector);
        generator.blockStateCollector.accept(
                BlockStateModelGenerator.createStairsBlockState(stairs, inner, straight, outer));
        // The item model (parent: the straight stairs model) is generated automatically.
    }

    private static void registerSlab(BlockStateModelGenerator generator, Block slab, Block base, Identifier texture) {
        TextureMap textures = sides(texture);
        Identifier bottom = Models.SLAB.upload(slab, textures, generator.modelCollector);
        Identifier top = Models.SLAB_TOP.upload(slab, textures, generator.modelCollector);
        // A double slab just uses the full block's own (hand-written) model.
        Identifier full = ModelIds.getBlockModelId(base);
        generator.blockStateCollector.accept(
                BlockStateModelGenerator.createSlabBlockState(slab, bottom, top, full));
        // The item model (parent: the bottom slab model) is generated automatically.
    }

    private static void registerWall(BlockStateModelGenerator generator, Block wall, Identifier texture) {
        TextureMap textures = new TextureMap().put(TextureKey.WALL, texture);
        Identifier post = Models.TEMPLATE_WALL_POST.upload(wall, textures, generator.modelCollector);
        Identifier low = Models.TEMPLATE_WALL_SIDE.upload(wall, textures, generator.modelCollector);
        Identifier tall = Models.TEMPLATE_WALL_SIDE_TALL.upload(wall, textures, generator.modelCollector);
        generator.blockStateCollector.accept(
                BlockStateModelGenerator.createWallBlockState(wall, post, low, tall));
        // Walls have no single block model, so the item uses the special inventory model.
        Identifier inventory = Models.WALL_INVENTORY.upload(wall, textures, generator.modelCollector);
        generator.registerParentedItemModel(wall, inventory);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
    }
}
