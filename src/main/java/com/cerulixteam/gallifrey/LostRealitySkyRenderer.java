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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Lost Reality's deliberately unstable sky: warped nebula bands, drifting stars,
 * a distorted sun by day and a pale cyan moon by night.
 */
public class LostRealitySkyRenderer implements DimensionRenderingRegistry.SkyRenderer {
    private static final Identifier WARP_TEXTURE =
            new Identifier("gallifrey", "textures/environment/lost_reality_warp.png");
    private static final Identifier SUN_TEXTURE =
            new Identifier("gallifrey", "textures/environment/lost_reality_sun_warped.png");
    private static final Identifier MOON_TEXTURE =
            new Identifier("gallifrey", "textures/environment/lost_reality_moon_merged.png");

    @Override
    public void render(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        ClientWorld world = (ClientWorld) context.world();
        float tickDelta = context.tickDelta();
        float skyAngle = world.getSkyAngle(tickDelta);
        float angleDegrees = skyAngle * 360.0F;
        float cycle = MathHelper.cos(skyAngle * MathHelper.TAU);
        float day = MathHelper.clamp((cycle + 1.0F) * 0.5F, 0.0F, 1.0F);
        float night = 1.0F - day;
        float time = world.getTime() + tickDelta;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        // Three offset warp passes make the sky feel folded rather than like a flat starbox.
        drawWarp(matrices, time * 0.0022F, 0.34F, 0.56F, 0.95F, 0.58F, 0.26F);
        drawWarp(matrices, -time * 0.0014F + 37.0F, 0.48F, 0.30F, 0.92F, 0.46F, 0.20F);
        drawWarp(matrices, time * 0.0009F + 119.0F, 0.64F, 0.18F, 0.82F, 0.82F, 0.16F);

        // Stars become dominant at night but remain faintly visible during the day.
        drawStars(matrices, time * 0.0018F, 0.22F + night * 0.78F);
        drawStars(matrices, -time * 0.0031F + 51.0F, 0.10F + night * 0.60F);

        // Daytime sun and nighttime moon occupy opposite positions on the same orbit.
        // The Lost Reality sun is deliberately unstable: layered, offset copies of the warped star
        // make its silhouette shimmer and appear to fold in on itself.
        drawCelestial(matrices, angleDegrees, 0.0F, 28.0F, SUN_TEXTURE, 1.10F, day);
        drawCelestial(matrices, angleDegrees + 1.8F, 2.5F, 20.0F, SUN_TEXTURE, 0.55F, day * 0.82F);
        // The moon is a warped composite of several planets/moons, so its surface never reads as a
        // normal celestial body. Two translucent offsets sell the "everything merged together" effect.
        drawCelestial(matrices, angleDegrees + 180.0F, 0.0F, 21.0F, MOON_TEXTURE, 0.92F, night);
        drawCelestial(matrices, angleDegrees + 182.4F, -3.0F, 16.0F, MOON_TEXTURE, 0.42F, night * 0.9F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawWarp(MatrixStack matrices, float rotation, float elevation,
                          float red, float green, float blue, float alpha) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(elevation * 90.0F));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, WARP_TEXTURE);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        float size = 125.0F;
        buffer.vertex(matrix, -size, 100.0F, -size).texture(0.0F, 0.0F).next();
        buffer.vertex(matrix, size, 100.0F, -size).texture(1.0F, 0.0F).next();
        buffer.vertex(matrix, size, 100.0F, size).texture(1.0F, 1.0F).next();
        buffer.vertex(matrix, -size, 100.0F, size).texture(0.0F, 1.0F).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        matrices.pop();
    }

    private void drawStars(MatrixStack matrices, float rotation, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 180; i++) {
            long seed = 0x9E3779B97F4A7C15L * (i + 1L);
            float yaw = hash(seed) * 360.0F;
            float pitch = 8.0F + hash(seed + 17L) * 164.0F;
            float size = 0.25F + hash(seed + 31L) * 0.9F;
            float twinkle = 0.68F + hash(seed + 43L) * 0.32F;
            float r = 0.72F + hash(seed + 59L) * 0.28F;
            float g = 0.70F + hash(seed + 71L) * 0.30F;
            float b = 0.86F + hash(seed + 83L) * 0.14F;
            float a = alpha * twinkle;

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation + yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            buffer.vertex(matrix, -size, 98.0F, -size).color(r, g, b, a).next();
            buffer.vertex(matrix, size, 98.0F, -size).color(r, g, b, a).next();
            buffer.vertex(matrix, size, 98.0F, size).color(r, g, b, a).next();
            buffer.vertex(matrix, -size, 98.0F, size).color(r, g, b, a).next();
            matrices.pop();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawCelestial(MatrixStack matrices, float angleDegrees, float headingOffset,
                               float size, Identifier texture, float brightness, float alpha) {
        if (alpha <= 0.01F) return;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + headingOffset));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angleDegrees));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(brightness, brightness, brightness, alpha);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, -size, 100.0F, -size).texture(0.0F, 0.0F).next();
        buffer.vertex(matrix, size, 100.0F, -size).texture(1.0F, 0.0F).next();
        buffer.vertex(matrix, size, 100.0F, size).texture(1.0F, 1.0F).next();
        buffer.vertex(matrix, -size, 100.0F, size).texture(0.0F, 1.0F).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        matrices.pop();
    }

    private static float hash(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53l;
        value ^= value >>> 33;
        return (value & 0x7fffffffL) / (float) 0x7fffffffL;
    }
}
