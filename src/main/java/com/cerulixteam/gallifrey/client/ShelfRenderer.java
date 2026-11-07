package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.backport.ShelfBlock;
import com.cerulixteam.gallifrey.block.backport.ShelfBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

/** Draws a shelf's three items, standing on its ledge and facing out. */
public class ShelfRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
    public ShelfRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ShelfBlockEntity shelf, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Direction facing = shelf.getCachedState().get(ShelfBlock.FACING);
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation() + 180));
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = shelf.getStack(slot);
            if (stack.isEmpty()) continue;
            matrices.push();
            // Slot 0 is on the left looking at the front.
            matrices.translate((slot - 1) * (5.0 / 16.0), 0.0, -2.5 / 16.0);
            matrices.scale(0.3F, 0.3F, 0.3F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV,
                    matrices, vertexConsumers, shelf.getWorld(), (int) shelf.getPos().asLong() + slot);
            matrices.pop();
        }
        matrices.pop();
    }
}
