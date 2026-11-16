package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.TardisThrottleBlock;
import com.cerulixteam.gallifrey.block.entity.TardisThrottleBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * GeckoLib renderer for the TARDIS throttle (geo/block/tardis_throttle.geo.json). The "lever" bone
 * swings forward when the throttle is pulled and back when it's released, easing between the two.
 */
public class TardisThrottleRenderer extends GeoBlockRenderer<TardisThrottleBlockEntity> {
    /** Lever angles in radians: pulled back (idle) and pushed forward (in flight). */
    private static final float RELEASED = -0.45F;
    private static final float PULLED = 0.45F;
    /** Fraction of the remaining swing covered each frame. */
    private static final float EASE = 0.18F;

    public TardisThrottleRenderer(BlockEntityRendererFactory.Context context) {
        super(new DefaultedBlockGeoModel<>(GallifreyMod.id("tardis_throttle")) {
            @Override
            public void setCustomAnimations(TardisThrottleBlockEntity throttle, long instanceId,
                                            AnimationState<TardisThrottleBlockEntity> animationState) {
                CoreGeoBone lever = getAnimationProcessor().getBone("lever");
                if (lever == null) return;
                boolean pulled = throttle.getCachedState().contains(TardisThrottleBlock.PULLED)
                        && throttle.getCachedState().get(TardisThrottleBlock.PULLED);
                float target = pulled ? PULLED : RELEASED;
                if (Float.isNaN(throttle.leverAngle)) throttle.leverAngle = target;
                throttle.leverAngle += (target - throttle.leverAngle) * EASE;
                lever.setRotX(throttle.leverAngle);
            }
        });
    }
}
