package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Plays one of the looping vortex animations behind the title screen, at full resolution.
 *
 * The built-in animations are 90 JPEG frames (3 seconds at 30 frames a second, 720x720) in
 * assets/gallifrey/menu_vortex/<folder>/00.jpg .. 89.jpg. Custom ones (CustomMenuBackgrounds) can
 * use any folder, frame count, frame rate and jpg/png; frames are numbered 00, 01, ... 99, 100, ... Only the compressed frames are kept in
 * memory (a few MB); each frame is decoded on a background thread just before it's needed and
 * uploaded into a single texture, so there's only ever one frame in video memory. The square is
 * scaled to cover the whole screen, centred, with smooth filtering.
 */
public final class VideoVortexRenderer {
    private VideoVortexRenderer() {}

    /** Frame count and frame rate of the built-in animations (and the defaults for custom ones). */
    public static final int FRAMES = 90;
    public static final float DEFAULT_FPS = 30.0F;
    private static final Identifier TEXTURE_ID = new Identifier(GallifreyMod.MOD_ID, "dynamic/menu_vortex");

    private static String loadedFolder;
    private static int frameCount = FRAMES;
    private static float framesPerSecond = DEFAULT_FPS;
    private static byte[][] frames;
    private static NativeImageBackedTexture texture;
    private static int textureSize;
    private static int shownFrame = -1;
    private static CompletableFuture<NativeImage> pending;
    private static int pendingFrame = -1;

    /** @param alpha 0..1, the title screen's fade-in */
    public static void render(DrawContext context, int width, int height, float alpha, String folder) {
        render(context, width, height, alpha, new Identifier(GallifreyMod.MOD_ID, "menu_vortex/" + folder), FRAMES, DEFAULT_FPS, "jpg");
    }

    /**
     * Plays the frames in {@code folder} (e.g. gallifrey:menu_vortex/time_war): 00.jpg, 01.jpg, ...
     *
     * @param format "jpg" or "png"
     */
    public static void render(DrawContext context, int width, int height, float alpha,
                              Identifier folder, int totalFrames, float fps, String format) {
        context.fill(0, 0, width, height, ((int) (alpha * 255.0F) << 24)); // black behind, for the fade-in and first frame
        String key = folder + "|" + totalFrames + "|" + format;
        if (!key.equals(loadedFolder)) {
            unload();
            frameCount = totalFrames;
            framesPerSecond = fps;
            load(key, folder, format);
        }
        if (frames == null) return;

        // Take a finished frame, if there is one.
        if (pending != null && pending.isDone()) {
            NativeImage image = pending.getNow(null);
            pending = null;
            if (image != null) show(image, pendingFrame);
        }

        long loopMs = Math.max(1L, (long) (frameCount * 1000.0F / framesPerSecond));
        int wanted = (int) ((Util.getMeasuringTimeMs() % loopMs) / 1000.0F * framesPerSecond) % frameCount;
        if (pending == null && wanted != shownFrame) {
            byte[] jpeg = frames[wanted];
            pendingFrame = wanted;
            pending = CompletableFuture.supplyAsync(() -> decode(jpeg), Util.getMainWorkerExecutor());
        }

        if (texture == null) return;
        int size = Math.max(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        context.drawTexture(TEXTURE_ID, x, y, size, size, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void load(String key, Identifier folder, String format) {
        loadedFolder = key;
        byte[][] loaded = new byte[frameCount][];
        var resources = MinecraftClient.getInstance().getResourceManager();
        for (int i = 0; i < frameCount; i++) {
            Identifier id = new Identifier(folder.getNamespace(), String.format("%s/%02d.%s", folder.getPath(), i, format));
            Optional<Resource> resource = resources.getResource(id);
            if (resource.isEmpty()) {
                GallifreyMod.LOGGER.warn("[Gallifrey] Missing title screen vortex frame {}", id);
                return;
            }
            try (InputStream in = resource.get().getInputStream()) {
                loaded[i] = in.readAllBytes();
            } catch (IOException e) {
                GallifreyMod.LOGGER.warn("[Gallifrey] Couldn't read title screen vortex frame {}", id, e);
                return;
            }
        }
        frames = loaded;
    }

    /** Runs on a worker thread: JPEG/PNG bytes to a NativeImage ready for upload. */
    private static NativeImage decode(byte[] jpeg) {
        try {
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(jpeg));
            if (decoded == null) return null;
            int w = decoded.getWidth();
            int h = decoded.getHeight();
            int[] argb = decoded.getRGB(0, 0, w, h, null, 0, w);
            NativeImage image = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = argb[y * w + x];
                    // NativeImage stores ABGR.
                    image.setColor(x, y, 0xFF000000 | (c & 0x0000FF00) | ((c & 0x00FF0000) >>> 16) | ((c & 0x000000FF) << 16));
                }
            }
            return image;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** Render thread: puts a decoded frame on screen. */
    private static void show(NativeImage image, int frame) {
        if (frames == null) {
            image.close();
            return;
        }
        if (texture == null) {
            texture = new NativeImageBackedTexture(image);
            textureSize = image.getWidth();
            texture.setFilter(true, false); // smooth when scaled up to the screen
            MinecraftClient.getInstance().getTextureManager().registerTexture(TEXTURE_ID, texture);
        } else {
            texture.setImage(image); // closes the previous frame
            texture.upload();
            texture.setFilter(true, false); // upload() switches back to blocky filtering
        }
        shownFrame = frame;
    }

    /** Frees everything: the frame data, the texture and any frame still being decoded. */
    public static void unload() {
        if (pending != null) {
            pending.thenAccept(image -> { if (image != null) image.close(); });
            pending = null;
        }
        if (texture != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(TEXTURE_ID);
            texture = null;
        }
        frames = null;
        loadedFolder = null;
        shownFrame = -1;
        pendingFrame = -1;
    }
}
