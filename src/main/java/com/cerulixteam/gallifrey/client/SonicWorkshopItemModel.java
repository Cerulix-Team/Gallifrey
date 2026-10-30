package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.item.custom.SonicWorkshopItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

/** Geo model used when the Sonic Workshop is held/in inventory. */
public class SonicWorkshopItemModel extends GeoModel<SonicWorkshopItem> {
    private static final Identifier MODEL = new Identifier("gallifrey", "geo/block/sonic_workshop.geo.json");
    private static final Identifier TEXTURE = new Identifier("gallifrey", "textures/block/sonic_workshop.png");
    private static final Identifier ANIMATION = new Identifier("gallifrey", "animations/block/sonic_workshop.animation.json");

    @Override
    public Identifier getModelResource(SonicWorkshopItem animatable) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(SonicWorkshopItem animatable) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(SonicWorkshopItem animatable) {
        return ANIMATION;
    }
}
