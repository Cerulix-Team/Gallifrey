package com.cerulixteam.gallifrey.client.backport;

import com.cerulixteam.gallifrey.block.backport.trial.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** The item spinning inside a Vault: a taste of what the key will get you. */
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
    public VaultRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(VaultBlockEntity vault, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light, int overlay) {
        ItemStack stack = vault.getDisplay();
        if (stack.isEmpty()) return;
        matrices.push();
        matrices.translate(0.5, 0.4, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, vault.lastSpin, vault.spin)));
        matrices.scale(0.75F, 0.75F, 0.75F);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, consumers,
                vault.getWorld(), 0);
        matrices.pop();
    }
}
