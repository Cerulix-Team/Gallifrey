package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.entity.custom.SkaroCityDalekAltEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

/** Renderer for the alternate Skaro City Dalek. */
public class SkaroCityDalekAltRenderer extends DalekRenderer<SkaroCityDalekAltEntity> {
    public SkaroCityDalekAltRenderer(EntityRendererFactory.Context context) {
        super(context, "skaro_city_dalek_alt");
    }
}
