package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.custom.HeadwearItem;
import com.cerulixteam.gallifrey.item.custom.SonicShadesItem;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The flat 2D icons for the fezzes, the Dalek eyestalk and anything else worn on the head.
 * Each one is models/item/&lt;item id&gt;_2d.json. They're shown in inventories and item frames,
 * while hands and the head keep the 3D Blockbench model.
 *
 * The extra models have to be loaded by their plain model id (gallifrey:item/fez_2d). Asking for
 * gallifrey:item/fez_2d#inventory makes Minecraft treat it as an item called "item/fez_2d" and look
 * for models/item/item/fez_2d.json, which doesn't exist.
 */
public final class HeadwearFlatModels {
    private HeadwearFlatModels() {}

    private static final String[] FLAT_MODELS = {
            "bluefez_2d", "darkbluefez_2d", "fancyfez_2d", "fez_2d",
            "greenfez_2d", "greyfez_2d", "orangefez_2d", "pinkfez_2d",
            "purplefez_2d", "yellowfez_2d", "eyestalk_2d"
    };

    /** Call once from the client initializer. */
    public static void register() {
        ModelLoadingPlugin.register(context -> {
            for (String model : FLAT_MODELS) {
                context.addModels(new Identifier("gallifrey", "item/" + model));
            }
        });
    }

    /** The flat model for this stack, or null if it isn't headwear or has no flat model. */
    @Nullable
    public static BakedModel flatModelFor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof HeadwearItem || stack.getItem() instanceof SonicShadesItem)) {
            return null;
        }
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        BakedModelManager models = MinecraftClient.getInstance().getBakedModelManager();
        BakedModel flat = models.getModel(new Identifier(itemId.getNamespace(), "item/" + itemId.getPath() + "_2d"));
        // Never swap a working 3D model for the missing model.
        if (flat == null || flat == models.getMissingModel()) return null;
        return flat;
    }
}
