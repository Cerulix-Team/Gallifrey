package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.client.HeadwearFlatModels;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Item frames (FIXED), plus GUI drawing that doesn't go through DrawContext (JEI, for one), use the
 * flat 2D headwear model. Hands, dropped items and the worn model keep the 3D Blockbench model.
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererHeadwearMixin {

    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedModel gallifrey$use2dHeadwearModel(BakedModel originalModel, ItemStack stack,
                                                    ModelTransformationMode renderMode, boolean leftHanded,
                                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                    int light, int overlay, BakedModel bakedModel) {
        if (renderMode != ModelTransformationMode.GUI && renderMode != ModelTransformationMode.FIXED) {
            return originalModel;
        }
        BakedModel flat = HeadwearFlatModels.flatModelFor(stack);
        return flat != null ? flat : originalModel;
    }
}
