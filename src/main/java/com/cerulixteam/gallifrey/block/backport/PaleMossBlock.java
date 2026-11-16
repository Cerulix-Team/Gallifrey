package com.cerulixteam.gallifrey.block.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

/** 1.21.4's pale moss block: bone meal spreads pale moss, carpets and grass around it, like the moss block. */
public class PaleMossBlock extends Block implements Fertilizable {
    private static final RegistryKey<ConfiguredFeature<?, ?>> BONEMEAL_PATCH =
            RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, new Identifier("gallifrey", "pale_moss_patch_bonemeal"));

    public PaleMossBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return world.getBlockState(pos.up()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).getOrEmpty(BONEMEAL_PATCH).ifPresent(patch ->
                patch.generate(world, world.getChunkManager().getChunkGenerator(), random, pos.up()));
    }
}
