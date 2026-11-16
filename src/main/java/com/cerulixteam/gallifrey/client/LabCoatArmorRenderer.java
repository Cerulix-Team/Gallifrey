package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.custom.LabCoatArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class LabCoatArmorRenderer extends GeoArmorRenderer<LabCoatArmorItem> {
    public LabCoatArmorRenderer() {
        super(new DefaultedItemGeoModel<>(GallifreyMod.id("armor/lab_coat")));
    }
}
