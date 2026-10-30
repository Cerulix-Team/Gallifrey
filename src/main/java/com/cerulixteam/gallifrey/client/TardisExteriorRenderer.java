package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.TardisExteriorBlock;
import com.cerulixteam.gallifrey.block.entity.TardisExteriorBlockEntity;
import com.cerulixteam.gallifrey.model.TardisModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class TardisExteriorRenderer implements BlockEntityRenderer<TardisExteriorBlockEntity> {

    // Temporary texture - the UV template hasn't been painted yet, this is
    // the same file the block's own (now-unused) JSON model pointed at
    private static final Identifier TEXTURE =
            new Identifier("gallifrey", "textures/block/tardis/tardis_exterior.png");

    public static final EntityModelLayer TARDIS_EXTERIOR_LAYER =
            new EntityModelLayer(new Identifier("gallifrey", "tardis_exterior"), "main");

    private final TardisModel model;

    public TardisExteriorRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new TardisModel(context.getLayerModelPart(TARDIS_EXTERIOR_LAYER));
    }

    @Override
    public void render(
            TardisExteriorBlockEntity blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        matrices.push();
        matrices.translate(0.5, 1.5, 0.5);

        int rotation = blockEntity.getCachedState().get(TardisExteriorBlock.ROTATION);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation * 45.0F));

        matrices.scale(-1.0F, -1.0F, 1.0F);

        model.render(
                matrices,
                vertexConsumers.getBuffer(model.getLayer(TEXTURE)),
                light,
                overlay,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        matrices.pop();
    }
}