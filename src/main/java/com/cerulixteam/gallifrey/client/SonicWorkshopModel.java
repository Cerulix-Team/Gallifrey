package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.entity.SonicWorkshopBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SonicWorkshopModel extends GeoModel<SonicWorkshopBlockEntity> {
    private static final Identifier MODEL = new Identifier("gallifrey", "geo/block/sonic_workshop.geo.json");
    private static final Identifier TEXTURE = new Identifier("gallifrey", "textures/block/sonic_workshop.png");

    @Override
    public Identifier getModelResource(SonicWorkshopBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(SonicWorkshopBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(SonicWorkshopBlockEntity animatable) {
        return new Identifier("gallifrey", "animations/block/sonic_workshop.animation.json");
    }
}
