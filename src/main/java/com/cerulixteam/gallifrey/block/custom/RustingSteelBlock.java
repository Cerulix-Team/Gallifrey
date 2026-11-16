package com.cerulixteam.gallifrey.block.custom;

import com.cerulixteam.gallifrey.block.GallifreyModBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class RustingSteelBlock extends Block {

    private final int rustStage;

    public RustingSteelBlock(int rustStage, AbstractBlock.Settings settings) {
        super(settings.ticksRandomly());
        this.rustStage = rustStage;
    }

    public int getRustStage() {
        return rustStage;
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        // Stage 4 is fully rusted.
        if (rustStage >= 4) {
            return;
        }

        // Vanilla copper's random-tick gate.
        if (random.nextFloat() >= 0.05688889F) {
            return;
        }

        int younger = 0;
        int same = 0;

        // Check nearby steel, like vanilla copper.
        for (BlockPos nearby : BlockPos.iterateOutwards(pos, 4, 4, 4)) {
            if (nearby.equals(pos)
                    || nearby.getManhattanDistance(pos) > 4) {
                continue;
            }

            BlockState nearbyState = world.getBlockState(nearby);

            if (!(nearbyState.getBlock() instanceof RustingSteelBlock steel)) {
                continue;
            }

            if (steel.getRustStage() < rustStage) {
                younger++;
            } else if (steel.getRustStage() == rustStage) {
                same++;
            }
        }

        // Younger nearby steel prevents this block from rusting.
        if (younger > 0) {
            return;
        }

        float ratio = (same + 1.0F) / (same + younger + 1.0F);
        float probability = ratio * ratio;

        // Fresh steel uses copper's slower first-stage multiplier.
        if (rustStage == 0) {
            probability *= 0.75F;
        }

        if (random.nextFloat() < probability) {
            Block next = getNextBlock();

            if (next != null) {
                world.setBlockState(
                        pos,
                        next.getDefaultState(),
                        Block.NOTIFY_ALL
                );
            }
        }
    }

    private Block getNextBlock() {
        return switch (rustStage) {
            case 0 -> GallifreyModBlocks.TARNISHED_STEEL;
            case 1 -> GallifreyModBlocks.WEATHERED_STEEL_BLOCK;
            case 2 -> GallifreyModBlocks.CORRODED_STEEL_BLOCK;
            case 3 -> GallifreyModBlocks.RUSTED_STEEL_BLOCK;
            default -> null;
        };
    }
}