package com.cerulixteam.gallifrey.block.entity;

import com.cerulixteam.gallifrey.block.GallifreyModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Holds the GeckoLib model for the TARDIS throttle. Its state lives in the block state. */
public class TardisThrottleBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Client-only: the lever angle being shown, eased towards pulled/released. */
    public float leverAngle = Float.NaN;

    public TardisThrottleBlockEntity(BlockPos pos, BlockState state) {
        super(GallifreyModBlockEntities.TARDIS_THROTTLE, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
