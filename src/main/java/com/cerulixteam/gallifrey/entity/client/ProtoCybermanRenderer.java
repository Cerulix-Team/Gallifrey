package com.cerulixteam.gallifrey.entity.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.ProtoCybermanEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ProtoCybermanRenderer extends MobEntityRenderer<ProtoCybermanEntity, ProtoCybermanModel<ProtoCybermanEntity>> {
    private static final Identifier TEXTURE = new Identifier(GallifreyMod.MOD_ID, "textures/entity/proto_cyberman.png");

    public ProtoCybermanRenderer(EntityRendererFactory.Context context) {
        super(context, new ProtoCybermanModel<>(context.getPart(GallifreyCyberModelLayers.PROTO_CYBERMAN)), 0.55F);
    }

    @Override
    public Identifier getTexture(ProtoCybermanEntity entity) {
        return TEXTURE;
    }
}
