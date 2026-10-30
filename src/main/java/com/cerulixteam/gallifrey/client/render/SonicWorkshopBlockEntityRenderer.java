package com.cerulixteam.gallifrey.client.render;

import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.cerulixteam.gallifrey.client.SonicWorkshopModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * Renders the Sonic Workshop Geo model plus the installed Sonic in its socket.
 *
 * The Sonic is NOT rendered while GeckoLib is still drawing bones. Rendering an item
 * mid-model switches the shared vertex buffer, and every bone drawn after the socket
 * then goes into the wrong buffer (garbled/missing parts, or a "Not building!" crash).
 * Instead we remember where the socket is while the bones render, and draw the Sonic
 * once the whole model is finished.
 */
public class SonicWorkshopBlockEntityRenderer extends GeoBlockRenderer<SonicWorkshopBlockEntity> {

    private static final String SOCKET_BONE = "sonic_holder";

    // Where the socket was this frame. Reused every frame - no per-frame allocation.
    private final Matrix4f socketPose = new Matrix4f();
    private final Matrix3f socketNormal = new Matrix3f();
    private boolean socketCaptured;

    public SonicWorkshopBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new SonicWorkshopModel());
    }

    @Override
    public void renderRecursively(MatrixStack matrices, SonicWorkshopBlockEntity blockEntity, GeoBone bone,
                                  RenderLayer renderType, VertexConsumerProvider vertexConsumers, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (!isReRender && SOCKET_BONE.equals(bone.getName()) && !blockEntity.getSonic().isEmpty()) {
            // Same transform GeckoLib applies to the bone, stopping at the socket's pivot point.
            matrices.push();
            RenderUtils.translateMatrixToBone(matrices, bone);
            RenderUtils.translateToPivotPoint(matrices, bone);
            RenderUtils.rotateMatrixAroundBone(matrices, bone);
            RenderUtils.scaleMatrixForBone(matrices, bone);
            this.socketPose.set(matrices.peek().getPositionMatrix());
            this.socketNormal.set(matrices.peek().getNormalMatrix());
            this.socketCaptured = true;
            matrices.pop();
        }

        // Normal world lighting (the block is non-opaque now, so this is no longer pitch black).
        super.renderRecursively(matrices, blockEntity, bone, renderType, vertexConsumers, buffer,
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void actuallyRender(MatrixStack matrices, SonicWorkshopBlockEntity blockEntity, BakedGeoModel model,
                               RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.socketCaptured = false;

        super.actuallyRender(matrices, blockEntity, model, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (isReRender || !this.socketCaptured) {
            return;
        }

        ItemStack sonic = blockEntity.getSonic();
        if (sonic.isEmpty()) {
            return;
        }

        // The whole model is drawn, so it's now safe to use other buffers.
        MatrixStack sonicMatrices = new MatrixStack();
        sonicMatrices.peek().getPositionMatrix().set(this.socketPose);
        sonicMatrices.peek().getNormalMatrix().set(this.socketNormal);

        // Lie the Sonic along the 53-degree sloped holder and shrink it to socket size.
        sonicMatrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(54.0F));
        sonicMatrices.translate(0.0D, -0.02D, 0.0D);
        sonicMatrices.scale(0.50F, 0.50F, 0.50F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                sonic,
                ModelTransformationMode.FIXED,
                packedLight,
                packedOverlay,
                sonicMatrices,
                bufferSource,
                blockEntity.getWorld(),
                (int) blockEntity.getPos().asLong());
    }
}
