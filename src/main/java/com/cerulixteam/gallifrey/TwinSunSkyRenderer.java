package com.cerulixteam.gallifrey;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class TwinSunSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {

    private static final Identifier SUN_TEXTURE = new Identifier("textures/environment/sun.png");

    @Override
    public void render(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickDelta();
        ClientWorld world = (ClientWorld) context.world();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask(false);

        float angle = world.getSkyAngle(tickDelta) * 360.0F;

        // Primary sun — same heading vanilla uses
        drawSun(matrices, buffer, tessellator, angle, 0.0F, 30.0F, SUN_TEXTURE);

        // Second sun — offset heading puts it on a different orbital path, still time-driven
        drawSun(matrices, buffer, tessellator, angle, 40.0F, 20.0F, SUN_TEXTURE);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private void drawSun(MatrixStack matrices, BufferBuilder buffer, Tessellator tessellator,
                         float angleDegrees, float headingOffsetDegrees, float size, Identifier texture) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + headingOffsetDegrees));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angleDegrees));

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, -size, 100.0F, -size).texture(0.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F, -size).texture(1.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F,  size).texture(1.0F, 1.0F).next();
        buffer.vertex(matrix, -size, 100.0F,  size).texture(0.0F, 1.0F).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
    }
}