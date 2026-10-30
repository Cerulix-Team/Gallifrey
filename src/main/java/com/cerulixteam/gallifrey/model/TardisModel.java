package com.cerulixteam.gallifrey.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;


public class TardisModel extends Model {

    private final ModelPart tardis;

    public TardisModel(ModelPart root) {
        super(RenderLayer::getEntityCutout);
        this.tardis = root.getChild("tardis");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData tardis = root.addChild(
                "tardis",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-11.0F, -1.0F, -12.0F, 23.0F, 1.0F, 23.0F)

                        .uv(64, 43)
                        .cuboid(-7.0F, -32.0F, -8.0F, 15.0F, 1.0F, 15.0F)

                        .uv(84, 90)
                        .cuboid(-10.0F, -37.0F, 7.0F, 3.0F, 36.0F, 3.0F)

                        .uv(96, 59)
                        .cuboid(8.0F, -37.0F, -11.0F, 3.0F, 36.0F, 3.0F)

                        .uv(96, 98)
                        .cuboid(-10.0F, -37.0F, -11.0F, 3.0F, 36.0F, 3.0F)

                        .uv(92, 0)
                        .cuboid(8.0F, -37.0F, 7.0F, 3.0F, 36.0F, 3.0F)

                        .uv(68, 39)
                        .cuboid(-7.0F, -33.0F, -10.0F, 15.0F, 2.0F, 2.0F)

                        .uv(108, 66)
                        .cuboid(-7.0F, -33.0F, 7.0F, 15.0F, 2.0F, 2.0F)

                        .uv(104, 0)
                        .cuboid(-9.0F, -33.0F, -8.0F, 2.0F, 2.0F, 15.0F)

                        .uv(104, 17)
                        .cuboid(8.0F, -33.0F, -8.0F, 2.0F, 2.0F, 15.0F)

                        .uv(0, 43)
                        .cuboid(8.55F, -31.0F, -8.0F, 1.0F, 30.0F, 15.0F)

                        .uv(32, 43)
                        .cuboid(-8.525F, -31.0F, -8.0F, 1.0F, 30.0F, 15.0F)

                        .uv(64, 59)
                        .cuboid(-7.0F, -31.0F, 7.55F, 15.0F, 30.0F, 1.0F)

                        .uv(34, 108)
                        .cuboid(-9.0F, -31.0F, -1.0F, 1.0F, 30.0F, 1.0F)

                        .uv(38, 108)
                        .cuboid(9.0F, -31.0F, -1.0F, 1.0F, 30.0F, 1.0F)

                        .uv(108, 70)
                        .cuboid(0.0F, -31.0F, 8.0F, 1.0F, 30.0F, 1.0F)

                        .uv(104, 34)
                        .cuboid(-8.0F, -36.0F, -12.0F, 17.0F, 3.0F, 4.0F)

                        .uv(108, 59)
                        .cuboid(-8.0F, -36.0F, 7.0F, 17.0F, 3.0F, 4.0F)

                        .uv(0, 88)
                        .cuboid(-11.0F, -36.0F, -9.0F, 4.0F, 3.0F, 17.0F)

                        .uv(42, 90)
                        .cuboid(8.0F, -36.0F, -9.0F, 4.0F, 3.0F, 17.0F)

                        .uv(0, 24)
                        .cuboid(-8.0F, -38.0F, -9.0F, 17.0F, 2.0F, 17.0F)

                        .uv(68, 24)
                        .cuboid(-2.0F, -39.0F, -2.5F, 5.0F, 1.0F, 5.0F)

                        .uv(70, 110)
                        .cuboid(-1.0F, -43.0F, -1.5F, 3.0F, 4.0F, 3.0F)

                        .uv(68, 30)
                        .cuboid(-2.0F, -44.0F, -2.5F, 5.0F, 1.0F, 5.0F)

                        .uv(112, 70)
                        .cuboid(-1.0F, -45.0F, -1.5F, 3.0F, 1.0F, 3.0F),

                ModelTransform.pivot(0.0F, 24.0F, 0.0F)
        );

        tardis.addChild(
                "cube_r1",
                ModelPartBuilder.create()
                        .uv(56, 110)
                        .cuboid(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 7.0F),
                ModelTransform.of(
                        3.0F, -39.0F, -2.5F,
                        0.0F, -0.7854F, 0.0F
                )
        );

        tardis.addChild(
                "cube_r2",
                ModelPartBuilder.create()
                        .uv(42, 110)
                        .cuboid(0.0F, -4.0F, 0.0F, 0.0F, 4.0F, 7.0F),
                ModelTransform.of(
                        3.0F, -39.0F, 2.5F,
                        0.0F, -2.3562F, 0.0F
                )
        );

        ModelPartData rightDoor = tardis.addChild(
                "right_door",
                ModelPartBuilder.create()
                        .uv(108, 101)
                        .cuboid(7.0F, -30.0F, -2.0F, 1.0F, 30.0F, 1.0F)
                        .uv(0, 108)
                        .cuboid(0.0F, -30.0F, -1.65F, 8.0F, 30.0F, 1.0F),
                ModelTransform.pivot(-7.0F, -1.0F, -8.0F)
        );

        ModelPartData leftDoor = tardis.addChild(
                "left_door",
                ModelPartBuilder.create(),
                ModelTransform.of(
                        8.0F, -1.0F, -8.0F,
                        0.0F, 1.6144F, 0.0F
                )
        );

        leftDoor.addChild(
                "cube_r3",
                ModelPartBuilder.create()
                        .uv(18, 108)
                        .cuboid(-6.0F, -30.0F, -1.0F, 7.0F, 30.0F, 1.0F),
                ModelTransform.of(
                        0.6F, 0.0F, -1.0F,
                        0.0F, -1.6144F, 0.0F
                )
        );

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        tardis.render(
                matrices,
                vertices,
                light,
                overlay,
                red,
                green,
                blue,
                alpha
        );
    }
}