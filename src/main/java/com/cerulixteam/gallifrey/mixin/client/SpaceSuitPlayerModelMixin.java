package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.item.GallifreyModItems;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides parts of the player's own model under the space suit and the Timelord robes,
 * so they don't poke through or flicker against the GeckoLib armour model.
 *
 * Space suit: the suit fully encloses the player, so the body, arms and legs are hidden.
 *
 * Timelord robes: the robe model sits slightly outside the player's limbs, and its
 * sleeves and trouser legs stop short on purpose. So the player's real arms and legs
 * stay visible (their hands and feet show at the cuffs) and only the skin's outer
 * layers are hidden, plus the body, which the robe covers completely.
 *
 * Only ever HIDES parts. Anything not covered keeps whatever vanilla set it to, so the
 * player's skin-customisation settings (jacket, sleeves, trousers, hat) still work.
 *
 * Goes by the armour that's drawn (VisibleArmor), so it also works with Cosmetic Armor.
 *
 * Skipped while the first-person hand is being drawn: renderArm() calls setModelPose()
 * and then draws the arm directly, so hiding it there made the hand vanish.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class SpaceSuitPlayerModelMixin {

    @Unique
    private static boolean gallifrey$renderingFirstPersonArm;

    @Inject(method = "renderArm", at = @At("HEAD"))
    private void gallifrey$beginFirstPersonArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                               int light, AbstractClientPlayerEntity player,
                                               ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        gallifrey$renderingFirstPersonArm = true;
    }

    @Inject(method = "renderArm", at = @At("RETURN"))
    private void gallifrey$endFirstPersonArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                             int light, AbstractClientPlayerEntity player,
                                             ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        gallifrey$renderingFirstPersonArm = false;
    }

    @Inject(method = "setModelPose", at = @At("TAIL"))
    private void gallifrey$hideSuitCoveredParts(AbstractClientPlayerEntity player, CallbackInfo ci) {
        if (gallifrey$renderingFirstPersonArm) return;

        PlayerEntityModel<AbstractClientPlayerEntity> model =
                ((LivingEntityRendererAccessor<
                        AbstractClientPlayerEntity,
                        PlayerEntityModel<AbstractClientPlayerEntity>
                        >) (Object) this).gallifrey$getModel();

        ItemStack chest = com.cerulixteam.gallifrey.client.VisibleArmor.get(player, EquipmentSlot.CHEST);
        ItemStack legs = com.cerulixteam.gallifrey.client.VisibleArmor.get(player, EquipmentSlot.LEGS);

        boolean suitChest = chest.isOf(GallifreyModItems.SPACE_SUIT_CHESTPLATE);
        boolean suitLegs = legs.isOf(GallifreyModItems.SPACE_SUIT_LEGGINGS);
        boolean robeChest = chest.isOf(GallifreyModItems.TIMELORD_ROBE_CHESTPLATE);
        boolean robeLegs = legs.isOf(GallifreyModItems.TIMELORD_ROBE_LEGGINGS);
        // The lab coat covers the whole body, arms and legs, so both skin layers are hidden.
        boolean labCoat = chest.isOf(GallifreyModItems.LAB_COAT_CHESTPLATE);
        boolean labTrousers = legs.isOf(GallifreyModItems.LAB_COAT_LEGGINGS);

        if (labCoat) {
            model.body.visible = false;
            model.jacket.visible = false;
            model.leftArm.visible = false;
            model.rightArm.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        }
        if (labTrousers) {
            model.leftLeg.visible = false;
            model.rightLeg.visible = false;
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }

        if (suitChest || robeChest) {
            model.body.visible = false;
            model.jacket.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        }
        if (suitChest) {
            model.leftArm.visible = false;
            model.rightArm.visible = false;
        }

        if (suitLegs || robeLegs) {
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }
        if (suitLegs) {
            model.leftLeg.visible = false;
            model.rightLeg.visible = false;
        }
    }
}