package com.cerulixteam.gallifrey.entity.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.CavemanEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** Slim-armed player model (the caveman skin is a slim skin); BipedEntityRenderer draws the stone tool in hand. */
public class CavemanRenderer extends BipedEntityRenderer<CavemanEntity, PlayerEntityModel<CavemanEntity>> {
    private static final Identifier TEXTURE =
            new Identifier(GallifreyMod.MOD_ID, "textures/entity/player/caveman.png");

    public CavemanRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public Identifier getTexture(CavemanEntity entity) {
        return TEXTURE;
    }
}
