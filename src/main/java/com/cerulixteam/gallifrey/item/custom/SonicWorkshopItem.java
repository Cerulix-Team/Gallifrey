package com.cerulixteam.gallifrey.item.custom;

import com.cerulixteam.gallifrey.client.render.SonicWorkshopItemRenderer;
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
 * The inventory/hand form of the Sonic Workshop is also a GeckoLib item.
 * This is important because a normal BlockItem would only use the flat
 * item model and would not render the supplied .geo.json model in hand.
 */
public class SonicWorkshopItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public SonicWorkshopItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private SonicWorkshopItemRenderer renderer;

            @Override
            public net.minecraft.client.render.item.BuiltinModelItemRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new SonicWorkshopItemRenderer();
                }
                return renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // The workshop model is static.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
