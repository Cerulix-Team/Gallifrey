package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.client.TardisControlItemRenderer;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Block item for the TARDIS monitor and throttle that shows the 3D GeckoLib model in the
 * inventory and in hand (its item model is builtin/entity). The model is the block's own,
 * geo/block/<modelName>.geo.json.
 */
public class TardisControlItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    private final String modelName;

    public TardisControlItem(Block block, String modelName, Settings settings) {
        super(block, settings);
        this.modelName = modelName;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private TardisControlItemRenderer renderer;

            @Override
            public net.minecraft.client.render.item.BuiltinModelItemRenderer getCustomRenderer() {
                if (renderer == null) renderer = new TardisControlItemRenderer(modelName);
                return renderer;
            }
        });
    }

    @Override public Supplier<Object> getRenderProvider() { return renderProvider; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
