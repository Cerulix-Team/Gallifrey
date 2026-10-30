package com.cerulixteam.gallifrey.world.dimension;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

/** Repairs the Classic dimension's y=1 bedrock layer when existing chunks are loaded.
 * Newly generated chunks receive the layer from classic.json's noise surface rule.
 */
public final class ClassicBedrockLayer {
    private ClassicBedrockLayer() {}

    public static void register() {
        // Fabric API 0.92.x has CHUNK_LOAD, but no CHUNK_GENERATE event.
        // The dimension's worldgen surface rule handles fresh chunks; this repairs old saves.
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world.getRegistryKey().equals(ModDimensions.CLASSIC_LEVEL_KEY)) {
                enforce(chunk);
            }
        });
    }

    private static void enforce(WorldChunk chunk) {
        int minX = chunk.getPos().getStartX();
        int minZ = chunk.getPos().getStartZ();
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                BlockPos pos = new BlockPos(x, 1, z);
                if (!chunk.getBlockState(pos).isOf(GallifreyModBlocks.CLASSIC_BEDROCK)) {
                    chunk.setBlockState(pos, GallifreyModBlocks.CLASSIC_BEDROCK.getDefaultState(), false);
                }
            }
        }
    }
}
