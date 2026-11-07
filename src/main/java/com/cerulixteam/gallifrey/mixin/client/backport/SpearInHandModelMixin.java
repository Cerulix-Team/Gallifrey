package com.cerulixteam.gallifrey.mixin.client.backport;

import com.cerulixteam.gallifrey.client.BackportClient;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Spears (1.21.11) use a long "in hand" model everywhere except the inventory, the ground and item
 * frames, the way the trident does. 1.21.11 does that with item model definitions, which 1.20.1 doesn't have.
 */
@Mixin(ItemRenderer.class)
public abstract class SpearInHandModelMixin {
    @ModifyVariable(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"), argsOnly = true)
    private BakedModel gallifrey$spearInHand(BakedModel model, ItemStack stack, ModelTransformationMode mode, boolean leftHanded,
                                             MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (mode == ModelTransformationMode.GUI || mode == ModelTransformationMode.GROUND || mode == ModelTransformationMode.FIXED) return model;
        Identifier inHand = BackportClient.inHandModel(stack);
        if (inHand == null) return model;
        BakedModel swapped = BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), inHand);
        return swapped != null ? swapped : model;
    }
}
