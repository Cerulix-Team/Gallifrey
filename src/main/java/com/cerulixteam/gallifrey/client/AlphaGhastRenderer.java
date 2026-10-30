package com.cerulixteam.gallifrey.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.GhastEntityRenderer;
import net.minecraft.util.Identifier;

/** Renderer hook for the Classic Nether ghast texture. */
public class AlphaGhastRenderer extends GhastEntityRenderer {
    private static final Identifier TEXTURE = new Identifier("gallifrey", "textures/entity/alpha_ghast.png");

    public AlphaGhastRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(net.minecraft.entity.mob.GhastEntity entity) {
        return MinecraftClient.getInstance().getResourceManager().getResource(TEXTURE).isPresent()
                ? TEXTURE
                : new Identifier("minecraft", "textures/entity/ghast/ghast.png");
    }
}
