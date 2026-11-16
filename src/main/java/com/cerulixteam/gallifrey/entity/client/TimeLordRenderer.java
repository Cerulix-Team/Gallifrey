package com.cerulixteam.gallifrey.entity.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.TimeLordEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** Uses the standard Minecraft player model with the supplied SteveLord skin. */
public class TimeLordRenderer extends MobEntityRenderer<TimeLordEntity, PlayerEntityModel<TimeLordEntity>> {
    public TimeLordRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public Identifier getTexture(TimeLordEntity entity) {
        return new Identifier(GallifreyMod.MOD_ID, "textures/entity/player/stevelord.png");
    }
}
