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
 * The time vortex shown behind the title screen.
 *
 * Nothing here is a video. The screen is covered with a fan of rings centred
 * on the middle of the tunnel, and two repeating textures are scrolled across
 * it. Pixels near the centre read the texture "far away" (depth = 1 / distance
 * from the centre), which is what makes flat textures look like the walls of a
 * tunnel rushing past.
 *
 * Tuning: every number that changes the look is a constant at the top.
 */
public final class VortexBackgroundRenderer {
    private static final Identifier RED_TEXTURE =
            new Identifier("gallifrey", "textures/gui/title/vortex_red.png");
    private static final Identifier CYAN_TEXTURE =
            new Identifier("gallifrey", "textures/gui/title/vortex_cyan.png");

    // ---- Look ----
    /** Overall speed. 1 = as designed, 0.5 = half speed, 2 = double. */
    private static final float SPEED = 1.0F;
    /** How deep the tunnel looks. Bigger = walls rush past faster and look further away. */
    private static final float DEPTH_SCALE = 0.42F;
    /** Where the centre of the tunnel sits, as a fraction of the screen height. */
    private static final float CENTRE_Y = 0.52F;
    /** Distance from the centre (in half screen heights) at which the walls are fully visible. */
    private static final float FOG_END = 0.58F;

    // Colours, 0..1
    private static final float[] RED = {0.98F, 0.09F, 0.16F};
    private static final float[] CYAN = {0.04F, 0.84F, 1.00F};
    private static final float[] BACKGROUND_CENTRE = {0.125F, 0.016F, 0.086F};
    private static final float[] BACKGROUND_EDGE = {0.024F, 0.016F, 0.267F};

    // ---- Mesh detail ----
    private static final int SEGMENTS = 96;      // around the tunnel
    private static final int RINGS = 72;         // from the centre outwards
    private static final float INNER_RADIUS = 0.012F;
    private static final float OUTER_RADIUS = 4.5F;

    /** After this many seconds every layer is back where it started, so the loop is seamless. */
    private static final float LOOP_SECONDS = 200.0F;

    private static final float[] RING_RADIUS = new float[RINGS + 1];
    private static final float[] SEGMENT_COS = new float[SEGMENTS + 1];
    private static final float[] SEGMENT_SIN = new float[SEGMENTS + 1];

    static {
        for (int ring = 0; ring <= RINGS; ring++) {
            // Rings bunch up towards the centre, where the texture changes fastest.
            RING_RADIUS[ring] = (float) (INNER_RADIUS * Math.pow(OUTER_RADIUS / INNER_RADIUS, ring / (double) RINGS));
        }
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            double angle = 2.0 * Math.PI * segment / SEGMENTS;
            SEGMENT_COS[segment] = (float) Math.cos(angle);
            SEGMENT_SIN[segment] = (float) Math.sin(angle);
        }
    }

    private VortexBackgroundRenderer() {}

    /**
     * @param alpha 0..1, the title screen's fade-in
     */
    public static void render(DrawContext context, int width, int height, float alpha) {
        float seconds = ((Util.getMeasuringTimeMs() % (long) (LOOP_SECONDS * 1000.0F / SPEED)) / 1000.0F) * SPEED;

        float centreX = width / 2.0F;
        float centreY = height * CENTRE_Y;
        float unit = height / 2.0F;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Adventures in Time compatibility:
        // - Every layer is sent to the GPU one ring at a time (about 100 quads per draw call)
        //   instead of in one big batch of ~7000 quads. With AiT installed, the big batches broke
        //   the game's output for good: frames kept rendering but the window stayed on the red
        //   loading-screen colour, even after switching to another background.
        // - Only the state changes the vanilla panorama makes (CubeMapRenderer): no depth writes,
        //   no culling, blending on. The depth test is left alone and nothing uses a triangle fan.
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 1. Dark backdrop: deep red-black in the middle, navy towards the corners.
        RenderSystem.defaultBlendFunc();
        drawBackdrop(matrix, centreX, centreY, unit, alpha);

        // 2. Two red layers sliding over each other. Normal blending, so they also
        //    cover the navy behind them and stay red instead of turning pink.
        drawLayer(matrix, centreX, centreY, unit, RED_TEXTURE, RED, alpha,
                2.0F, 0.10F, 0.020F * seconds, 0.55F, 0.33F * seconds);
        drawLayer(matrix, centreX, centreY, unit, RED_TEXTURE, RED, alpha * 0.55F,
                -1.0F, 0.05F, -0.015F * seconds + 0.37F, 0.35F, 0.21F * seconds + 0.5F);

        // 3. Cyan lightning on top, added to what is there so it glows.
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        drawLayer(matrix, centreX, centreY, unit, CYAN_TEXTURE, CYAN, alpha,
                1.0F, 0.16F, 0.030F * seconds + 0.2F, 0.45F, 0.50F * seconds);

        // 4. The bright point at the far end of the tunnel.
        drawCore(matrix, centreX, centreY, unit, alpha);

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** How visible the tunnel walls are at this distance from the centre (0 at the far end). */
    private static float fog(float radius) {
        float t = (radius - 0.03F) / (FOG_END - 0.03F);
        if (t <= 0.0F) {
            return 0.0F;
        }
        if (t >= 1.0F) {
            return 1.0F;
        }
        return (float) Math.pow(t, 0.9);
    }

    private static void drawBackdrop(Matrix4f matrix, float cx, float cy, float unit, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (int ring = 0; ring < RINGS; ring++) {
            if (ring > 0) { // one ring per draw call, see the note in render()
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            }
            float r0 = ring == 0 ? 0.0F : RING_RADIUS[ring];
            float r1 = RING_RADIUS[ring + 1];
            for (int segment = 0; segment < SEGMENTS; segment++) {
                backdropVertex(buffer, matrix, cx, cy, unit, r0, segment, alpha);
                backdropVertex(buffer, matrix, cx, cy, unit, r1, segment, alpha);
                backdropVertex(buffer, matrix, cx, cy, unit, r1, segment + 1, alpha);
                backdropVertex(buffer, matrix, cx, cy, unit, r0, segment + 1, alpha);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void backdropVertex(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float unit,
                                       float radius, int segment, float alpha) {
        float centreAmount = Math.max(0.0F, 1.0F - radius / 1.2F);
        float edgeAmount = Math.min(1.0F, radius / 1.9F);
        float red = BACKGROUND_EDGE[0] + (BACKGROUND_CENTRE[0] - BACKGROUND_EDGE[0]) * centreAmount;
        float green = BACKGROUND_EDGE[1];
        float blue = BACKGROUND_CENTRE[2] + (BACKGROUND_EDGE[2] - BACKGROUND_CENTRE[2]) * edgeAmount;
        buffer.vertex(matrix, cx + SEGMENT_COS[segment] * radius * unit, cy + SEGMENT_SIN[segment] * radius * unit, 0.0F)
                .color(red, green, blue, alpha)
                .next();
    }

    /**
     * Draws one scrolling texture layer.
     *
     * @param around    how many times the texture wraps around the tunnel (negative = mirrored)
     * @param twist     how much the pattern spirals as it goes deeper
     * @param uOffset   rotation of the layer (changes with time)
     * @param depthTile how many times the texture repeats per unit of depth
     * @param vOffset   travel along the tunnel (changes with time)
     */
    private static void drawLayer(Matrix4f matrix, float cx, float cy, float unit, Identifier texture, float[] colour,
                                  float alpha, float around, float twist, float uOffset, float depthTile, float vOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture);
        VertexFormat format = VertexFormats.POSITION_TEXTURE_COLOR;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, format);
        int ringsInBatch = 0;

        for (int ring = 0; ring < RINGS; ring++) {
            float r0 = RING_RADIUS[ring];
            float r1 = RING_RADIUS[ring + 1];
            float fog0 = fog(r0);
            float fog1 = fog(r1);
            if (fog0 <= 0.0F && fog1 <= 0.0F) {
                continue; // still inside the dark far end, nothing to see
            }
            if (ringsInBatch++ > 0) { // one ring per draw call, see the note in render()
                BufferRenderer.drawWithGlobalProgram(buffer.end());
                buffer.begin(VertexFormat.DrawMode.QUADS, format);
            }
            float depth0 = DEPTH_SCALE / r0;
            float depth1 = DEPTH_SCALE / r1;
            float v0 = depth0 * depthTile + vOffset;
            float v1 = depth1 * depthTile + vOffset;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float turn0 = segment / (float) SEGMENTS;
                float turn1 = (segment + 1) / (float) SEGMENTS;
                layerVertex(buffer, matrix, cx, cy, unit, r0, segment, turn0 * around + twist * depth0 + uOffset, v0, colour, alpha * fog0);
                layerVertex(buffer, matrix, cx, cy, unit, r1, segment, turn0 * around + twist * depth1 + uOffset, v1, colour, alpha * fog1);
                layerVertex(buffer, matrix, cx, cy, unit, r1, segment + 1, turn1 * around + twist * depth1 + uOffset, v1, colour, alpha * fog1);
                layerVertex(buffer, matrix, cx, cy, unit, r0, segment + 1, turn1 * around + twist * depth0 + uOffset, v0, colour, alpha * fog0);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void layerVertex(BufferBuilder buffer, Matrix4f matrix, float cx, float cy, float unit,
                                    float radius, int segment, float u, float v, float[] colour, float alpha) {
        buffer.vertex(matrix, cx + SEGMENT_COS[segment] * radius * unit, cy + SEGMENT_SIN[segment] * radius * unit, 0.0F)
                .texture(u, v)
                .color(colour[0], colour[1], colour[2], alpha)
                .next();
    }

    /** The bright point at the far end: a small disc that fades out from the centre (plain triangles). */
    private static void drawCore(Matrix4f matrix, float cx, float cy, float unit, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        float radius = 0.05F * unit;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            buffer.vertex(matrix, cx, cy, 0.0F).color(1.0F, 0.30F, 0.40F, alpha).next();
            buffer.vertex(matrix, cx + SEGMENT_COS[segment] * radius, cy + SEGMENT_SIN[segment] * radius, 0.0F)
                    .color(1.0F, 0.20F, 0.35F, 0.0F).next();
            buffer.vertex(matrix, cx + SEGMENT_COS[segment + 1] * radius, cy + SEGMENT_SIN[segment + 1] * radius, 0.0F)
                    .color(1.0F, 0.20F, 0.35F, 0.0F).next();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
