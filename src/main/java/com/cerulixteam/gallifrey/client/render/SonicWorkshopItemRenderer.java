package com.cerulixteam.gallifrey.client.render;

import com.cerulixteam.gallifrey.client.SonicWorkshopItemModel;
import com.cerulixteam.gallifrey.item.custom.SonicWorkshopItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renders the actual workshop Geo model as the held/inventory item. */
public class SonicWorkshopItemRenderer extends GeoItemRenderer<SonicWorkshopItem> {
    public SonicWorkshopItemRenderer() {
        super(new SonicWorkshopItemModel());
        // The source model is authored at roughly one block wide.  Item
        // contexts need a smaller presentation than a world block.
        withScale(0.62F);
    }
}
