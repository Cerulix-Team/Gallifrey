package com.cerulixteam.gallifrey;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/** Permanent lunar night sky with a huge Overworld globe and the sun behind it. */
public final class MoonSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {
    private static final Identifier EARTH = new Identifier("gallifrey", "textures/environment/moon_overworld.png");
    private static final Identifier SUN = new Identifier("minecraft", "textures/environment/sun.png");

    @Override public void render(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        float time = context.world().getTime() + context.tickDelta();
        RenderSystem.disableDepthTest(); RenderSystem.disableCull(); RenderSystem.depthMask(false);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.78F, 0.82F, 0.92F, 1.0F);
        drawStars(matrices, time);
        // Offset the sun so it peeks around the huge Overworld globe's edge.
        draw(matrices, 27.0F, 13.0F, 16.0F, SUN, 0.95F, 0.85F, 0.45F, 0.92F);
        draw(matrices, 0.0F, 13.0F, 35.0F, EARTH, 0.78F, 0.88F, 1.0F, 1.0F);
        RenderSystem.setShaderColor(1.0F,1.0F,1.0F,1.0F);
        RenderSystem.depthMask(true); RenderSystem.enableDepthTest(); RenderSystem.enableCull(); RenderSystem.disableBlend();
    }

    private void draw(MatrixStack matrices, float yaw, float pitch, float size, Identifier texture, float r, float g, float b, float a) {
        matrices.push(); matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + yaw)); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram); RenderSystem.setShaderTexture(0, texture); RenderSystem.setShaderColor(r,g,b,a);
        Matrix4f m=matrices.peek().getPositionMatrix(); BufferBuilder buf=Tessellator.getInstance().getBuffer(); buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(m,-size,100.0F,-size).texture(0,0).next(); buf.vertex(m,size,100.0F,-size).texture(1,0).next(); buf.vertex(m,size,100.0F,size).texture(1,1).next(); buf.vertex(m,-size,100.0F,size).texture(0,1).next(); BufferRenderer.drawWithGlobalProgram(buf.end()); matrices.pop();
    }
    private void drawStars(MatrixStack matrices, float time) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram); BufferBuilder buf=Tessellator.getInstance().getBuffer(); buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for(int i=0;i<220;i++){ long seed=0x9E3779B97F4A7C15L*(i+1L); float yaw=hash(seed)*360.0F; float pitch=8.0F+hash(seed+17L)*164.0F; float sz=0.18F+hash(seed+31L)*0.65F; float tw=0.72F+0.28F*(float)Math.sin(time*0.03F+i); matrices.push(); matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw)); matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch)); Matrix4f m=matrices.peek().getPositionMatrix(); float a=tw; buf.vertex(m,-sz,98,-sz).color(0.78F,0.84F,1.0F,a).next(); buf.vertex(m,sz,98,-sz).color(0.78F,0.84F,1.0F,a).next(); buf.vertex(m,sz,98,sz).color(0.78F,0.84F,1.0F,a).next(); buf.vertex(m,-sz,98,sz).color(0.78F,0.84F,1.0F,a).next(); matrices.pop(); } BufferRenderer.drawWithGlobalProgram(buf.end());
    }
    private static float hash(long v){v^=v>>>33;v*=0xff51afd7ed558ccdl;v^=v>>>33;v*=0xc4ceb9fe1a85ec53l;v^=v>>>33;return (v&0x7fffffff)/(float)0x7fffffff;}
}
