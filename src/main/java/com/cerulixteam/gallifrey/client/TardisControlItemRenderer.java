package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.TardisControlItem;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Draws the TARDIS monitor/throttle items with their block's GeckoLib model. */
public class TardisControlItemRenderer extends GeoItemRenderer<TardisControlItem> {
    public TardisControlItemRenderer(String modelName) {
        super(new DefaultedBlockGeoModel<>(GallifreyMod.id(modelName)));
    }
}
