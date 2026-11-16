package com.cerulixteam.gallifrey.entity.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.DiamondMinecartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;


public class DiamondMinecartRenderer extends MinecartEntityRenderer<DiamondMinecartEntity> {
    private static final Identifier TEXTURE = new Identifier(GallifreyMod.MOD_ID, "textures/entity/diamond_minecart.png");

    public DiamondMinecartRenderer(EntityRendererFactory.Context context) {
        super(context, EntityModelLayers.MINECART);
    }

    @Override
    public Identifier getTexture(DiamondMinecartEntity entity) {
        return TEXTURE;
    }
}
