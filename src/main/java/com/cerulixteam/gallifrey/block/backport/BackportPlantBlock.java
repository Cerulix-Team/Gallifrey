package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** The simple 1.21.5 plants: Bush, Firefly Bush, Short and Tall Dry Grass, and the Cactus Flower. */
public class BackportPlantBlock extends PlantBlock {
    public enum Soil {
        /** Grass, dirt, farmland and the like. */
        DIRT,
        /** Also sand and terracotta, for the dry grasses. */
        DRY,
        /** On top of a cactus, farmland, or anything with a solid top. */
        CACTUS
    }

    private final VoxelShape shape;
    private final Soil soil;

    public BackportPlantBlock(Soil soil, VoxelShape shape, Settings settings) {
        super(settings);
        this.soil = soil;
        this.shape = shape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shape;
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return switch (this.soil) {
            case DIRT -> super.canPlantOnTop(floor, world, pos);
            case DRY -> super.canPlantOnTop(floor, world, pos) || floor.isIn(BlockTags.SAND) || floor.isIn(BlockTags.TERRACOTTA);
            case CACTUS -> floor.isOf(Blocks.CACTUS) || floor.isOf(Blocks.FARMLAND) || floor.isSideSolidFullSquare(world, pos, Direction.UP);
        };
    }

    public static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.createCuboidShape(x1, y1, z1, x2, y2, z2);
    }
}
