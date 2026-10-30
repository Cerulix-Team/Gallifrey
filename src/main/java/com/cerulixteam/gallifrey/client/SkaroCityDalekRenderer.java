package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.entity.custom.SkaroCityDalekEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

/** Renderer for the standard Skaro City Dalek. */
public class SkaroCityDalekRenderer extends DalekRenderer<SkaroCityDalekEntity> {
    public SkaroCityDalekRenderer(EntityRendererFactory.Context context) {
        super(context, "skaro_city_dalek");
    }
}
