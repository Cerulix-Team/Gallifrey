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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Mondas' permanent night sky.
 *
 * The sky is deliberately dynamic: layered star fields, galaxies and planets
 * drift at different rates so the sky reads as a planet travelling through
 * deep space rather than a static star box.
 */
public class MondasSkyRenderer implements DimensionRenderingRegistry.SkyRenderer {

    private static final Identifier GALAXY_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_galaxy.png");
    private static final Identifier PLANET_BLUE_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_blue.png");
    private static final Identifier PLANET_RED_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_red.png");
    private static final Identifier PLANET_RINGED_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_ringed.png");
    private static final Identifier PLANET_PURPLE_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_purple.png");
    private static final Identifier PLANET_ICE_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_ice.png");
    private static final Identifier PLANET_GOLD_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_gold.png");
    private static final Identifier PLANET_GREEN_TEXTURE =
            new Identifier("gallifrey", "textures/environment/mondas_planet_green.png");

    private static final int STAR_COUNT = 240;

    @Override
    public void render(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        ClientWorld world = (ClientWorld) context.world();
        float tickDelta = context.tickDelta();
        float time = world.getTime() + tickDelta;

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // A very dark midnight base lets the moving celestial objects remain readable.
        RenderSystem.setShaderColor(0.055F, 0.065F, 0.095F, 1.0F);

        // Three star layers create parallax: distant stars barely move while nearer
        // stars drift a little faster, selling Mondas' journey through space.
        drawStarLayer(matrices, time * 0.0012F, 0.85F, 0.70F, 0.78F, 0.90F, 0.75F, 1.0F);
        drawStarLayer(matrices, time * -0.0020F + 37.0F, 1.0F, 0.92F, 0.98F, 1.0F, 1.0F, 0.75F);
        drawStarLayer(matrices, time * 0.0032F + 91.0F, 1.45F, 0.70F, 0.82F, 1.0F, 1.0F, 0.55F);
        drawStarLayer(matrices, time * -0.0046F + 143.0F, 0.58F, 0.62F, 0.76F, 1.0F, 0.72F, 0.95F);
        drawStarLayer(matrices, time * 0.0065F + 319.0F, 0.42F, 1.0F, 0.88F, 0.70F, 0.62F, 1.0F);

        // Large distant galaxies slide slowly through the sky.
        drawCelestialTexture(matrices, time * 0.00075F + 25.0F,
                18.0F, 34.0F, GALAXY_TEXTURE, 0.58F, 0.70F, 0.95F, 0.38F);
        drawCelestialTexture(matrices, time * -0.00052F + 205.0F,
                32.0F, 24.0F, GALAXY_TEXTURE, 0.75F, 0.55F, 0.95F, 0.28F);
        drawCelestialTexture(matrices, time * 0.00034F + 315.0F,
                52.0F, 18.0F, GALAXY_TEXTURE, 0.55F, 0.80F, 1.0F, 0.20F);
        drawCelestialTexture(matrices, time * -0.00027F + 118.0F,
                67.0F, 15.0F, GALAXY_TEXTURE, 1.0F, 0.45F, 0.70F, 0.18F);

        // Different planets move at different apparent speeds and headings.
        drawCelestialTexture(matrices, time * 0.0025F + 70.0F,
                16.0F, 8.0F, PLANET_BLUE_TEXTURE, 0.78F, 0.88F, 1.0F, 0.95F);
        drawCelestialTexture(matrices, time * -0.0017F + 160.0F,
                28.0F, 11.0F, PLANET_RED_TEXTURE, 0.95F, 0.68F, 0.60F, 0.82F);
        drawCelestialTexture(matrices, time * 0.0011F + 275.0F,
                38.0F, 6.0F, PLANET_RINGED_TEXTURE, 0.90F, 0.82F, 0.68F, 0.72F);
        drawCelestialTexture(matrices, time * -0.00135F + 35.0F,
                22.0F, 5.0F, PLANET_PURPLE_TEXTURE, 0.92F, 0.62F, 1.0F, 0.80F);
        drawCelestialTexture(matrices, time * 0.00082F + 232.0F,
                47.0F, 7.5F, PLANET_ICE_TEXTURE, 0.72F, 0.90F, 1.0F, 0.78F);
        drawCelestialTexture(matrices, time * -0.00068F + 330.0F,
                58.0F, 4.5F, PLANET_GOLD_TEXTURE, 1.0F, 0.72F, 0.34F, 0.76F);
        drawCelestialTexture(matrices, time * 0.00165F + 188.0F,
                72.0F, 9.0F, PLANET_GREEN_TEXTURE, 0.55F, 1.0F, 0.72F, 0.74F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawStarLayer(MatrixStack matrices, float rotation,
                               float size, float red, float green, float blue,
                               float alpha, float brightness) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Deterministic pseudo-random placement. The same stars remain in the same
        // pattern between frames; the layer rotation makes them drift smoothly.
        for (int i = 0; i < STAR_COUNT; i++) {
            long seed = 0x9E3779B97F4A7C15L * (i + 1L);
            float yaw = hash(seed) * 360.0F;
            float pitch = 10.0F + hash(seed + 17L) * 155.0F;
            float twinkle = 0.72F + hash(seed + 31L) * 0.28F;

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation + yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

            float starSize = size * (0.35F + hash(seed + 53L) * 0.85F);
            float a = alpha * brightness * twinkle;

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            buffer.vertex(matrix, -starSize, 100.0F, -starSize)
                    .color(red, green, blue, a).next();
            buffer.vertex(matrix,  starSize, 100.0F, -starSize)
                    .color(red, green, blue, a).next();
            buffer.vertex(matrix,  starSize, 100.0F,  starSize)
                    .color(red, green, blue, a).next();
            buffer.vertex(matrix, -starSize, 100.0F,  starSize)
                    .color(red, green, blue, a).next();
            matrices.pop();
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawCelestialTexture(MatrixStack matrices, float orbitDegrees,
                                      float elevation, float size, Identifier texture,
                                      float red, float green, float blue, float alpha) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F + orbitDegrees));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(elevation));

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, -size, 100.0F, -size).texture(0.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F, -size).texture(1.0F, 0.0F).next();
        buffer.vertex(matrix,  size, 100.0F,  size).texture(1.0F, 1.0F).next();
        buffer.vertex(matrix, -size, 100.0F,  size).texture(0.0F, 1.0F).next();
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
