package com.cerulixteam.gallifrey.fluid;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.GallifreyModItems;
import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class GallifreyFluids {
    public static final FlowableFluid STILL_RADIATION = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "radiation"),
            new Still()
    );

    public static final FlowableFluid FLOWING_RADIATION = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "flowing_radiation"),
            new Flowing()
    );

    public static final FlowableFluid STILL_CLASSIC_LAVA = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "classic_lava"),
            new ClassicLavaStill()
    );

    public static final FlowableFluid FLOWING_CLASSIC_LAVA = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "flowing_classic_lava"),
            new ClassicLavaFlowing()
    );

    public static final FlowableFluid STILL_CLASSIC_WATER = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "classic_water"),
            new ClassicWaterStill()
    );

    public static final FlowableFluid FLOWING_CLASSIC_WATER = Registry.register(
            Registries.FLUID,
            new Identifier(GallifreyMod.MOD_ID, "flowing_classic_water"),
            new ClassicWaterFlowing()
    );

    public static final FluidBlock CLASSIC_LAVA = Registry.register(
            Registries.BLOCK,
            new Identifier(GallifreyMod.MOD_ID, "classic_nether_lava"),
            new FluidBlock(STILL_CLASSIC_LAVA, FabricBlockSettings.copyOf(net.minecraft.block.Blocks.LAVA)
                    .strength(100.0F)
                    .noCollision()
                    .dropsNothing()
                    .replaceable())
    );

    public static final FluidBlock CLASSIC_WATER = Registry.register(
            Registries.BLOCK,
            new Identifier(GallifreyMod.MOD_ID, "classic_water"),
            new FluidBlock(STILL_CLASSIC_WATER, FabricBlockSettings.copyOf(net.minecraft.block.Blocks.WATER)
                    .strength(100.0F)
                    .noCollision()
                    .dropsNothing()
                    .replaceable())
    );

    public static final FluidBlock RADIATION = Registry.register(
            Registries.BLOCK,
            new Identifier(GallifreyMod.MOD_ID, "radiation"),
            new FluidBlock(STILL_RADIATION, FabricBlockSettings.copyOf(net.minecraft.block.Blocks.WATER)
                    .mapColor(net.minecraft.block.MapColor.DARK_GREEN)
                    .strength(100.0F)
                    .noCollision()
                    .dropsNothing()
                    .replaceable()
                    .sounds(BlockSoundGroup.SLIME)) {
                @Override
                public void onEntityCollision(BlockState state, net.minecraft.world.World world, BlockPos pos, Entity entity) {
                    super.onEntityCollision(state, world, pos, entity);
                    // Radiation remains swimmable, but it must still burn anything
                    // that enters it. Apply the fire after vanilla water collision
                    // handling so the water tag cannot immediately extinguish it.
                    entity.setOnFireFor(2);
                }
            }
    );

    public static void registerModFluids() {
        GallifreyMod.LOGGER.info("Registering ModFluids for " + GallifreyMod.MOD_ID);
    }

    public abstract static class RadiationFluid extends FlowableFluid {
        @Override
        public Fluid getStill() {
            return STILL_RADIATION;
        }

        @Override
        public Fluid getFlowing() {
            return FLOWING_RADIATION;
        }

        @Override
        public Item getBucketItem() {
            return GallifreyModItems.RADIATION_BUCKET;
        }



        @Override
        protected void beforeBreakingBlock(net.minecraft.world.WorldAccess world, BlockPos pos, BlockState state) {
            BlockEntityAccessor.dropStacks(state, world, pos);
        }





        @Override
        protected BlockState toBlockState(FluidState state) {
            return GallifreyFluids.RADIATION.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
        }

        @Override
        public boolean matchesType(Fluid fluid) {
            return fluid == STILL_RADIATION || fluid == FLOWING_RADIATION;
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1;
        }

        @Override
        protected int getFlowSpeed(WorldView world) {
            return 1;
        }

        @Override
        public boolean isInfinite(net.minecraft.world.World world) {
            return false;
        }

        @Override
        protected float getBlastResistance() {
            return 100.0F;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, net.minecraft.world.BlockView world, BlockPos pos, Fluid fluid, net.minecraft.util.math.Direction direction) {
            return false;
        }

        @Override
        public int getTickRate(WorldView world) {
            return 5;
        }

    }

    public static class Flowing extends RadiationFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(Properties.LEVEL_1_8);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(Properties.LEVEL_1_8);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends RadiationFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    public abstract static class ClassicLavaFluid extends LavaFluid {
        @Override
        public Fluid getStill() {
            return STILL_CLASSIC_LAVA;
        }

        @Override
        public Fluid getFlowing() {
            return FLOWING_CLASSIC_LAVA;
        }



        @Override
        protected void beforeBreakingBlock(net.minecraft.world.WorldAccess world, BlockPos pos, BlockState state) {
            Block.dropStacks(state, world, pos, state.hasBlockEntity() ? world.getBlockEntity(pos) : null);
        }


        @Override
        public BlockState toBlockState(FluidState state) {
            return CLASSIC_LAVA.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
        }

        @Override
        protected void flow(net.minecraft.world.WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
            if (direction == Direction.DOWN) {
                FluidState below = world.getFluidState(pos);
                if (below.isIn(FluidTags.WATER) && state.getBlock() instanceof FluidBlock) {
                    BlockState result = fluidState.isStill()
                            ? GallifreyModBlocks.CLASSIC_OBSIDIAN.getDefaultState()
                            : (fluidState.getLevel() >= 5
                                ? GallifreyModBlocks.CLASSIC_STONE.getDefaultState()
                                : GallifreyModBlocks.CLASSIC_COBBLE.getDefaultState());
                    world.setBlockState(pos, result, 3);
                    return;
                }
            }
            super.flow(world, pos, state, direction, fluidState);
        }

        @Override
        public void onScheduledTick(net.minecraft.world.World world, BlockPos pos, FluidState state) {
            // Classic water/lava interaction: source lava becomes obsidian;
            // stronger flowing lava becomes stone; weaker flowing lava becomes cobblestone.
            for (Direction direction : Direction.values()) {
                if (world.getFluidState(pos.offset(direction)).isIn(FluidTags.WATER)) {
                    BlockState result = state.isStill()
                            ? GallifreyModBlocks.CLASSIC_OBSIDIAN.getDefaultState()
                            : (state.getLevel() >= 5
                                ? GallifreyModBlocks.CLASSIC_STONE.getDefaultState()
                                : GallifreyModBlocks.CLASSIC_COBBLE.getDefaultState());
                    world.setBlockState(pos, result, 3);
                    return;
                }
            }
            super.onScheduledTick(world, pos, state);
        }

        @Override
        public boolean matchesType(Fluid fluid) {
            return fluid == STILL_CLASSIC_LAVA || fluid == FLOWING_CLASSIC_LAVA;
        }

    }

    public static class ClassicLavaFlowing extends ClassicLavaFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(Properties.LEVEL_1_8);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(Properties.LEVEL_1_8);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class ClassicLavaStill extends ClassicLavaFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    private static final class BlockEntityAccessor {
        private static void dropStacks(BlockState state, net.minecraft.world.WorldAccess world, BlockPos pos) {
            net.minecraft.block.Block.dropStacks(state, world, pos, state.hasBlockEntity() ? world.getBlockEntity(pos) : null);
        }
    }
    public abstract static class ClassicWaterFluid extends WaterFluid {
        @Override
        public Fluid getStill() { return STILL_CLASSIC_WATER; }

        @Override
        public Fluid getFlowing() { return FLOWING_CLASSIC_WATER; }

        @Override
        public Item getBucketItem() { return GallifreyModItems.CLASSIC_WATER_BUCKET; }

        @Override
        public BlockState toBlockState(FluidState state) {
            return CLASSIC_WATER.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
        }

        @Override
        public boolean matchesType(Fluid fluid) {
            return fluid == STILL_CLASSIC_WATER || fluid == FLOWING_CLASSIC_WATER;
        }

        @Override
        protected void flow(net.minecraft.world.WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
            for (Direction side : Direction.values()) {
                FluidState other = world.getFluidState(pos.offset(side));
                if (other.isOf(STILL_CLASSIC_LAVA) || other.isOf(FLOWING_CLASSIC_LAVA) || other.isIn(FluidTags.LAVA)) {
                    BlockState result = other.isStill()
                            ? GallifreyModBlocks.CLASSIC_OBSIDIAN.getDefaultState()
                            : (other.getLevel() >= 5
                                ? GallifreyModBlocks.CLASSIC_STONE.getDefaultState()
                                : GallifreyModBlocks.CLASSIC_COBBLE.getDefaultState());
                    world.setBlockState(pos.offset(side), result, 3);
                    return;
                }
            }
            super.flow(world, pos, state, direction, fluidState);
        }

        @Override
        public void onScheduledTick(net.minecraft.world.World world, BlockPos pos, FluidState state) {
            for (Direction side : Direction.values()) {
                FluidState other = world.getFluidState(pos.offset(side));
                if (other.isOf(STILL_CLASSIC_LAVA) || other.isOf(FLOWING_CLASSIC_LAVA) || other.isIn(FluidTags.LAVA)) {
                    BlockState result = other.isStill()
                            ? GallifreyModBlocks.CLASSIC_OBSIDIAN.getDefaultState()
                            : (other.getLevel() >= 5
                                ? GallifreyModBlocks.CLASSIC_STONE.getDefaultState()
                                : GallifreyModBlocks.CLASSIC_COBBLE.getDefaultState());
                    world.setBlockState(pos.offset(side), result, 3);
                    return;
                }
            }
            super.onScheduledTick(world, pos, state);
        }
    }

    public static class ClassicWaterFlowing extends ClassicWaterFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(Properties.LEVEL_1_8);
        }

        @Override
        public int getLevel(FluidState state) { return state.get(Properties.LEVEL_1_8); }

        @Override
        public boolean isStill(FluidState state) { return false; }
    }

    public static class ClassicWaterStill extends ClassicWaterFluid {
        @Override
        public int getLevel(FluidState state) { return 8; }

        @Override
        public boolean isStill(FluidState state) { return true; }
    }

}
