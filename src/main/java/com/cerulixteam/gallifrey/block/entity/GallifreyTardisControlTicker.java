package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import com.cerulixteam.gallifrey.block.TardisThrottleBlock;
import com.cerulixteam.gallifrey.tardis.TardisDimensionManager;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/** Server tickers for the physical TARDIS controls. */
public final class GallifreyTardisControlTicker {
    private GallifreyTardisControlTicker() {}

    /** Springs a pulled throttle back once its TARDIS has landed (checked once a second). */
    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.block.entity.BlockEntity> BlockEntityTicker<T> throttle(BlockEntityType<T> type) {
        if (type != GallifreyModBlockEntities.TARDIS_THROTTLE) return null;
        return (world, pos, state, blockEntity) -> {
            if (!state.get(TardisThrottleBlock.PULLED) || world.getTime() % 20 != 0) return;
            if (!(world instanceof ServerWorld serverWorld)) return;
            TardisExteriorBlockEntity tardis = TardisDimensionManager.findTardisForInterior(serverWorld);
            if (tardis == null || !tardis.isFlightPending()) {
                world.setBlockState(pos, state.with(TardisThrottleBlock.PULLED, false), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.6F, 0.5F);
            }
        };
    }
}
