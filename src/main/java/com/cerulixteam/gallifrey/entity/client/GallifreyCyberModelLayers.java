package com.cerulixteam.gallifrey.entity.client;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
public final class GallifreyCyberModelLayers {
 private GallifreyCyberModelLayers() {}
 public static final EntityModelLayer CYBERMAT = new EntityModelLayer(new Identifier("gallifrey", "cybermat"), "main");
 public static final EntityModelLayer MONDASIAN_CYBERMAN = new EntityModelLayer(new Identifier("gallifrey", "mondasian_cyberman"), "main");
 public static final EntityModelLayer PROTO_CYBERMAN = new EntityModelLayer(new Identifier("gallifrey", "proto_cyberman"), "main");
}
