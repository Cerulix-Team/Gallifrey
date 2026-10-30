package com.cerulixteam.gallifrey.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.util.Identifier;

/** Renderer hook for the Classic Nether pigman texture. */
public class AlphaZombiePigmanRenderer extends ZombieEntityRenderer {
    private static final Identifier TEXTURE = new Identifier("gallifrey", "textures/entity/alpha_pigman.png");

    public AlphaZombiePigmanRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(net.minecraft.entity.mob.ZombieEntity entity) {
        return MinecraftClient.getInstance().getResourceManager().getResource(TEXTURE).isPresent()
                ? TEXTURE
                : new Identifier("minecraft", "textures/entity/piglin/zombified_piglin.png");
    }
}
