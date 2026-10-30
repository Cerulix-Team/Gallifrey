package com.cerulixteam.gallifrey.entity.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.entity.custom.ClassicVillagerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ClassicVillagerRenderer extends MobEntityRenderer<ClassicVillagerEntity, VillagerResemblingModel<ClassicVillagerEntity>> {

    public ClassicVillagerRenderer(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);
        // Old villagers had no hat layer. Only hide the hat (and the brim, which is attached to it);
        // vanilla's setHatVisible(false) would hide the whole head too.
        this.model.getHead().getChild("hat").visible = false;
        this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(ClassicVillagerEntity entity) {
        return new Identifier(GallifreyMod.MOD_ID,
                "textures/entity/classic_villager/" + entity.getProfession().texture + ".png");
    }

    @Override
    protected void scale(ClassicVillagerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(0.9375F, 0.9375F, 0.9375F);
    }
}