package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.MinecraftClient;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SonicWorkshopBlockEntityRenderer extends GeoBlockRenderer<SonicWorkshopBlockEntity> {

    public SonicWorkshopBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new DefaultedBlockGeoModel<>(GallifreyMod.id("sonic_workshop")));
    }

    @Override
    public void actuallyRender(MatrixStack matrices, SonicWorkshopBlockEntity animatable,
                                software.bernie.geckolib.cache.object.BakedGeoModel model,
                                net.minecraft.client.render.RenderLayer renderType,
                                VertexConsumerProvider bufferSource, VertexConsumer buffer,
                                boolean isReRender, float partialTick, int packedLight,
                                int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(matrices, animatable, model, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (isReRender || animatable.getSonic().isEmpty()) return;

        // The model's sonic_holder is a physical socket on the gold port.  The
        // installed Sonic is rendered with Minecraft's normal item renderer so
        // its existing casing predicates and 3D models are retained.
        matrices.push();
        matrices.translate(0.67D, 0.56D, 0.73D);
        matrices.scale(0.42F, 0.42F, 0.42F);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(90));

        ItemStack sonic = animatable.getSonic();
        MinecraftClient.getInstance().getItemRenderer().renderItem(
                sonic,
                ModelTransformationMode.GROUND,
                packedLight,
                packedOverlay,
                matrices,
                bufferSource,
                animatable.getWorld(),
                (int) animatable.getPos().asLong()
        );
        matrices.pop();
    }
}
