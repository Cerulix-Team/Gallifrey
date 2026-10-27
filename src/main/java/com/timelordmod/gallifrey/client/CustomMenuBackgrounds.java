package com.timelordmod.gallifrey.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.timelordmod.gallifrey.GallifreyMod;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Title screen backgrounds added by resource packs and addon mods.
 *
 * Any resource pack or mod can add one with a JSON file at
 *   assets/<namespace>/gallifrey_backgrounds/<name>.json
 * and a folder of frames (00.jpg, 01.jpg, ...). Every field is optional:
 *
 *   {
 *     "name": "Menu: My Vortex",                    // button text, or a translation key
 *     "frames": "<namespace>:menu_vortex/<name>",   // the frame folder (this is the default)
 *     "frame_count": 90,                            // default 90
 *     "fps": 30,                                    // default 30
 *     "format": "jpg"                               // "jpg" or "png", default "jpg"
 *   }
 *
 * They're read again on every resource reload, so turning a pack on or off updates the list. They
 * come after the built-in ones when cycling, ordered by id ("namespace:name"). The README has a
 * full how-to.
 */
public final class CustomMenuBackgrounds {
    private CustomMenuBackgrounds() {}

    /** One custom background. {@code id} is "namespace:name", which is what the settings file saves. */
    public record Entry(String id, Text name, Identifier frames, int frameCount, float fps, String format) {}

    public static final String FOLDER = "gallifrey_backgrounds";

    private static List<Entry> entries = List.of();
    private static boolean scanned;

    /** Called once from GallifreyModClient: re-reads the backgrounds on every resource reload. */
    public static void register() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(GallifreyMod.MOD_ID, "custom_menu_backgrounds");
            }

            @Override
            public void reload(ResourceManager manager) {
                scan(manager);
            }
        });
    }

    /** Every custom background currently available, in cycling order. */
    public static List<Entry> all() {
        if (!scanned) scan(MinecraftClient.getInstance().getResourceManager());
        return entries;
    }

    @Nullable
    public static Entry get(String id) {
        for (Entry entry : all()) {
            if (entry.id().equals(id)) return entry;
        }
        return null;
    }

    private static void scan(ResourceManager manager) {
        scanned = true;
        List<Entry> found = new ArrayList<>();
        Map<Identifier, Resource> files = manager.findResources(FOLDER, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> file : files.entrySet()) {
            Identifier fileId = file.getKey();
            String name = fileId.getPath().substring(FOLDER.length() + 1, fileId.getPath().length() - ".json".length());
            String namespace = fileId.getNamespace();
            try (Reader reader = file.getValue().getReader()) {
                JsonElement parsed = JsonParser.parseReader(reader);
                JsonObject json = parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();

                String label = json.has("name") ? json.get("name").getAsString()
                        : "gui." + namespace + ".menu_background." + name;
                Identifier frames = json.has("frames") ? new Identifier(json.get("frames").getAsString())
                        : new Identifier(namespace, "menu_vortex/" + name);
                int frameCount = json.has("frame_count") ? json.get("frame_count").getAsInt() : VideoVortexRenderer.FRAMES;
                float fps = json.has("fps") ? json.get("fps").getAsFloat() : VideoVortexRenderer.DEFAULT_FPS;
                String format = json.has("format") ? json.get("format").getAsString().toLowerCase(Locale.ROOT) : "jpg";

                if (frameCount < 1 || frameCount > 1000) throw new IllegalArgumentException("frame_count must be 1-1000");
                if (fps <= 0.0F || fps > 120.0F) throw new IllegalArgumentException("fps must be more than 0 and at most 120");
                if (format.equals("jpeg")) format = "jpg";
                if (!format.equals("jpg") && !format.equals("png")) throw new IllegalArgumentException("format must be \"jpg\" or \"png\"");

                found.add(new Entry(namespace + ":" + name, Text.translatable(label), frames, frameCount, fps, format));
            } catch (Exception e) {
                GallifreyMod.LOGGER.warn("[Gallifrey] Skipping title screen background {}: {}", fileId, e.toString());
            }
        }
        found.sort(Comparator.comparing(Entry::id));
        entries = List.copyOf(found);
        if (!entries.isEmpty()) {
            GallifreyMod.LOGGER.info("[Gallifrey] Found {} custom title screen background(s): {}",
                    entries.size(), entries.stream().map(Entry::id).toList());
        }
    }
}
