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
 * Skaro's cinematic sky: one sun by day and three distinct moons by night.
 * The moon artwork is deliberately pixel-art friendly while keeping the
 * eerie, realistic silhouettes associated with classic/modern Doctor Who.
 */
public class SkaroSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {

    private static final Identifier SUN_TEXTURE = new Identifier("textures/environment/sun.png");
    private static final Identifier FLIDOR_TEXTURE = new Identifier("gallifrey", "textures/environment/skaro_flidor_moon.png");
    private static final Identifier FALKUS_TEXTURE = new Identifier("gallifrey", "textures/environment/skaro_falkus_moon.png");
    private static final Identifier OMEGA_TEXTURE = new Identifier("gallifrey", "textures/environment/skaro_omega_mysterium_moon.png");

    @Override
    public void render(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickDelta();
        ClientWorld world = (ClientWorld) context.world();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float skyAngle = world.getSkyAngle(tickDelta);
        float dayAngle = skyAngle * 360.0F;
        float nightStrength = MathHelper.clamp(-MathHelper.cos(skyAngle * MathHelper.TAU), 0.0F, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        // Celestial bodies are sky objects. Do not let one moon's quad
        // depth-test against another moon or the terrain depth buffer.
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // Skaro has a twin-sun system. Both suns follow the same day/night cycle,
        // but the secondary sun has a separate orbital heading and is slightly smaller.
        float sunAlpha = 1.0F - nightStrength;
        drawCelestialBody(matrices, buffer, tessellator, dayAngle, 0.0F, 30.0F,
                SUN_TEXTURE, sunAlpha);
        drawCelestialBody(matrices, buffer, tessellator, dayAngle, 40.0F, 20.0F,
                SUN_TEXTURE, sunAlpha);

        // Keep all three moons above the horizon while their horizontal positions
        // continue to orbit at different speeds. This makes the three-moon system
        // visibly present throughout the Skaro night instead of allowing an orbit
        // to put a moon below the player's horizon.
        drawMoon(matrices, buffer, tessellator, dayAngle * 0.95F + 12.0F,
                18.0F, 23.0F, FLIDOR_TEXTURE, nightStrength);

        // Falkus — artificial moon; visibly faster orbit, inspired by its rapid cycle.
        drawMoon(matrices, buffer, tessellator, dayAngle * 3.0F + 145.0F,
                10.0F, 15.0F, FALKUS_TEXTURE, nightStrength);

        // Omega Mysterium — distant and small, with a much slower apparent motion.
        drawMoon(matrices, buffer, tessellator, dayAngle * 0.35F + 260.0F,
                26.0F, 9.0F, OMEGA_TEXTURE, nightStrength);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawMoon(MatrixStack matrices, BufferBuilder buffer, Tessellator tessellator,
                           float orbitYawDegrees, float elevationDegrees, float size,
                           Identifier texture, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        matrices.push();
        // A fixed positive elevation keeps each moon in the visible night sky;
        // the yaw provides the actual orbital movement around Skaro.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + orbitYawDegrees));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(elevationDegrees));

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, -size, 100.0F, -size).texture(0.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F, -size).texture(1.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F,  size).texture(1.0F, 1.0F).next();
        buffer.vertex(matrix, -size, 100.0F,  size).texture(0.0F, 1.0F).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
    }

    private void drawCelestialBody(MatrixStack matrices, BufferBuilder buffer, Tessellator tessellator,
                                   float angleDegrees, float headingOffsetDegrees, float size,
                                   Identifier texture, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + headingOffsetDegrees));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angleDegrees));

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

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
