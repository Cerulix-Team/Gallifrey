package com.cerulixteam.gallifrey.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

/**
 * The "Trustable" title screen background: the Trustable pattern wrapped round the inside of a
 * tunnel, spiralling in towards a glowing core.
 *
 * Drawn live (like VortexBackgroundRenderer) rather than played from frames, so it's sharp at any
 * window size: the pattern is one texture, mapped onto a ring mesh at screen resolution.
 * Pixels near the centre read the texture "far away" (depth = 1 / distance from the centre).
 *
 * - trustable.png: the pattern, mirrored into a seamless 2048 tile.
 * - trustable_far.png: a blurred copy, faded in towards the centre so the tiny far-off pattern
 *   doesn't shimmer.
 *
 * Every layer is sent one ring at a time (about 100 quads per draw call); big batches break the
 * game's output when Adventures in Time is installed (see VortexBackgroundRenderer).
 */
public final class TrustableVortexRenderer {
    private static final Identifier PATTERN = new Identifier("gallifrey", "textures/gui/title/trustable.png");
    private static final Identifier PATTERN_FAR = new Identifier("gallifrey", "textures/gui/title/trustable_far.png");

    // ---- Look ----
    /** How many times the pattern repeats around the tunnel. */
    private static final float AROUND = 4.0F;
    /** How much the pattern spirals as it goes deeper. */
    private static final float TWIST = 0.6F;
    /** How deep the tunnel looks. */
    private static final float DEPTH_SCALE = 0.6F;
    /** Tiles the pattern travels inwards, and turns round, per second. */
    private static final float FLOW_PER_SECOND = 2.0F / 3.0F;
    private static final float TURN_PER_SECOND = 1.0F / 3.0F;
    /** Inside this distance from the centre the blurred copy takes over. */
    private static final float FAR_START = 0.35F;
    private static final float FAR_FULL = 0.10F;

    // Glow at the far end, 0..1 colours
    private static final float[] CORE_WHITE = {1.0F, 1.0F, 1.0F};
    private static final float[] CORE_CYAN = {0.47F, 0.92F, 1.0F};
    private static final float[] CORE_PINK = {1.0F, 0.27F, 0.55F};

    // ---- Mesh ----
    private static final int SEGMENTS = 96;
    private static final int RINGS = 72;
    private static final float INNER_RADIUS = 0.01F;
    private static final float OUTER_RADIUS = 1.6F;   // in half-screen units; reaches past the corners

    /** Keeps the texture offsets small; every layer repeats after this many seconds. */
    private static final float LOOP_SECONDS = 300.0F;

    private static final float[] RING_RADIUS = new float[RINGS + 1];
    private static final float[] SEGMENT_COS = new float[SEGMENTS + 1];
    private static final float[] SEGMENT_SIN = new float[SEGMENTS + 1];

    static {
        for (int ring = 0; ring <= RINGS; ring++) {
            RING_RADIUS[ring] = (float) (INNER_RADIUS * Math.pow(OUTER_RADIUS / INNER_RADIUS, ring / (double) RINGS));
        }
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            double angle = 2.0 * Math.PI * segment / SEGMENTS;
            SEGMENT_COS[segment] = (float) Math.cos(angle);
            SEGMENT_SIN[segment] = (float) Math.sin(angle);
        }
    }

    private TrustableVortexRenderer() {}

    /** @param alpha 0..1, the title screen's fade-in */
    public static void render(DrawContext context, int width, int height, float alpha) {
        float seconds = (Util.getMeasuringTimeMs() % (long) (LOOP_SECONDS * 1000.0F)) / 1000.0F;
        float flow = (seconds * FLOW_PER_SECOND) % 1.0F;
        float turn = (seconds * TURN_PER_SECOND) % 1.0F;

        float cx = width / 2.0F;
        float cy = height / 2.0F;
        float unit = Math.max(width, height) / 2.0F;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // 1. Black behind everything (shows through the fogged far end and during the fade-in).
        //    Done first: DrawContext.fill switches blending off again when it's finished.
        context.fill(0, 0, width, height, ((int) (alpha * 255.0F) << 24));

        // Same state changes as the vanilla panorama, for AiT compatibility (see VortexBackgroundRenderer).
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 2. The blurred pattern everywhere, then the sharp pattern over it, fading out towards the centre.
        drawPattern(matrix, cx, cy, unit, PATTERN_FAR, alpha, flow, turn, false);
        drawPattern(matrix, cx, cy, unit, PATTERN, alpha, flow, turn, true);

        // 3. The glowing core, added on top.
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        float pulse = 1.0F + 0.08F * (float) Math.sin(seconds * Math.PI * 4.0 / 3.0);
        drawGlow(matrix, cx, cy, unit, 0.38F, CORE_PINK, 0.35F * alpha);
        drawGlow(matrix, cx, cy, unit, 0.14F, CORE_CYAN, 1.0F * alpha);
        drawGlow(matrix, cx, cy, unit, 0.05F, CORE_WHITE, Math.min(1.0F, pulse) * alpha);

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Brightness of the tunnel wall at this distance: dark at the far end and in the corners. */
    private static float brightness(float r) {
        float fog = clamp((r - 0.05F) / 0.7F);
        fog = (float) Math.pow(fog, 1.2);
        float vignette = (float) Math.pow(clamp(1.2F - r * 0.62F), 1.3);
        return fog * vignette;
    }

    /** How much of the sharp layer shows at this distance (0 near the centre, where the blurred one takes over). */
    private static float sharpAmount(float r) {
        return clamp((r - FAR_FULL) / (FAR_START - FAR_FULL));
    }

    private static void drawPattern(Matrix4f matrix, float cx, float cy, float unit, Identifier texture,
                                    float alpha, float flow, float turn, boolean sharp) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        for (int ring = 0; ring < RINGS; ring++) {
            float r0 = RING_RADIUS[ring];
            float r1 = RING_RADIUS[ring + 1];
            float b0 = brightness(r0);
            float b1 = brightness(r1);
            float a0 = alpha * (sharp ? sharpAmount(r0) : 1.0F);
            float a1 = alpha * (sharp ? sharpAmount(r1) : 1.0F);
            if ((b0 <= 0.0F && b1 <= 0.0F) || (a0 <= 0.0F && a1 <= 0.0F)) continue;

            float depth0 = DEPTH_SCALE / r0;
            float depth1 = DEPTH_SCALE / r1;
            float v0 = depth0 + flow;
            float v1 = depth1 + flow;

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float t0 = segment / (float) SEGMENTS * AROUND + turn;
                float t1 = (segment + 1) / (float) SEGMENTS * AROUND + turn;
                vertex(buffer, matrix, cx, cy, unit, r0, segment, t0 + TWIST * depth0, v0, b0, a0);
                vertex(buffer, matrix, cx, cy, unit, r1, segment, t0 + TWIST * depth1, v1, b1, a1);
                vertex(buffer, matrix, cx, cy, unit, r1, segment + 1, t1 + TWIST * depth1, v1, b1, a1);
                vertex(buffer, matrix, cx, cy, unit, r0, segment + 1, t1 + TWIST * depth0, v0, b0, a0);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float unit,
                               float radius, int segment, float u, float v, float bright, float alpha) {
        buffer.vertex(matrix, cx + SEGMENT_COS[segment] * radius * unit, cy + SEGMENT_SIN[segment] * radius * unit, 0.0F)
                .texture(u, v)
                .color(bright, bright, bright, alpha)
                .next();
    }

    /** A soft round glow (Gaussian fall-off) of the given size, added onto what's there. */
    private static void drawGlow(Matrix4f matrix, float cx, float cy, float unit, float size, float[] colour, float strength) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        int steps = 24;
        float outer = size * 2.5F;
        for (int step = 0; step < steps; step++) {
            buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            float rIn = outer * step / steps;
            float rOut = outer * (step + 1) / steps;
            float aIn = strength * (float) Math.exp(-(rIn / size) * (rIn / size));
            float aOut = strength * (float) Math.exp(-(rOut / size) * (rOut / size));
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float x0 = SEGMENT_COS[segment], y0 = SEGMENT_SIN[segment];
                float x1 = SEGMENT_COS[segment + 1], y1 = SEGMENT_SIN[segment + 1];
                // two triangles per cell of the ring
                glowVertex(buffer, matrix, cx + x0 * rIn * unit, cy + y0 * rIn * unit, colour, aIn);
                glowVertex(buffer, matrix, cx + x0 * rOut * unit, cy + y0 * rOut * unit, colour, aOut);
                glowVertex(buffer, matrix, cx + x1 * rOut * unit, cy + y1 * rOut * unit, colour, aOut);
                glowVertex(buffer, matrix, cx + x0 * rIn * unit, cy + y0 * rIn * unit, colour, aIn);
                glowVertex(buffer, matrix, cx + x1 * rOut * unit, cy + y1 * rOut * unit, colour, aOut);
                glowVertex(buffer, matrix, cx + x1 * rIn * unit, cy + y1 * rIn * unit, colour, aIn);
            }
            // one ring per draw call (AiT compatibility)
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    private static void glowVertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float[] colour, float alpha) {
        buffer.vertex(matrix, x, y, 0.0F).color(colour[0], colour[1], colour[2], alpha).next();
    }

    private static float clamp(float value) {
        return value < 0.0F ? 0.0F : (value > 1.0F ? 1.0F : value);
    }
}
