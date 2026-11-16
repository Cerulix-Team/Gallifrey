package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.SpaceSuitArmorItem;

import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class SpaceSuitArmorRenderer extends GeoArmorRenderer<SpaceSuitArmorItem> {

    public SpaceSuitArmorRenderer() {
        super(new DefaultedItemGeoModel<>(
                GallifreyMod.id("armor/space_suit")
        ));
    }
}