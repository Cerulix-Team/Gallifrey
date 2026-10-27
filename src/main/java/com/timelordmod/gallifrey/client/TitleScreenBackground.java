package com.timelordmod.gallifrey.client;

import com.timelordmod.gallifrey.GallifreyMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Tessellator;

/**
 * Hands the title screen's DrawContext to the panorama hook (RotatingCubeMapRendererMixin).
 *
 * TitleScreenVortexMixin fills this in for the duration of TitleScreen.render. While it's set,
 * the panorama renderer asks draw() whether to show Gallifrey's background instead of itself.
 */
public final class TitleScreenBackground {
    private TitleScreenBackground() {}

    private static DrawContext context;
    private static int width;
    private static int height;

    /** Set if the code-drawn vortex ever throws; from then on the normal panorama is shown instead. */
    private static boolean vortexFailed;

    public static void begin(DrawContext drawContext, int screenWidth, int screenHeight) {
        context = drawContext;
        width = screenWidth;
        height = screenHeight;
    }

    public static void end() {
        context = null;
    }

    /**
     * Called in place of the panorama. Draws the chosen vortex and returns true, or returns
     * false to let the normal panorama (vanilla's or another mod's, e.g. AiT's) draw.
     *
     * @param alpha the title screen's fade-in, 0..1
     */
    public static boolean draw(float alpha) {
        if (context == null) return false;
        MenuBackground background = GallifreyClientConfig.getMenuBackground();
        if (background == MenuBackground.VORTEX) {
            return drawVortex(alpha);
        }
        if (background.frameFolder() != null) {
            VideoVortexRenderer.render(context, width, height, alpha, background.frameFolder());
            return true;
        }
        return false;
    }

    private static boolean drawVortex(float alpha) {
        if (vortexFailed) return false;
        // Something else started drawing with the shared buffer and never finished; skip this
        // frame rather than crash.
        if (Tessellator.getInstance().getBuffer().isBuilding()) return false;
        try {
            VortexBackgroundRenderer.render(context, width, height, alpha);
            return true;
        } catch (Throwable t) {
            vortexFailed = true;
            GallifreyMod.LOGGER.error("[Gallifrey] The title screen vortex failed to draw, showing the panorama instead", t);
            return false;
        }
    }
}
