package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.entity.custom.SupremeCouncilDalekEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;

/** Renderer for the Supreme Council Dalek. */
public class SupremeCouncilDalekRenderer extends DalekRenderer<SupremeCouncilDalekEntity> {
    public SupremeCouncilDalekRenderer(EntityRendererFactory.Context context) {
        super(context, "supreme_council_dalek");
    }
}
