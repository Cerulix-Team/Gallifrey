package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.item.GallifreyModItems;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class SpaceSuitInventoryPreviewMixin {

    @Inject(method = "drawEntity", at = @At("HEAD"))
    private static void gallifrey$hideSuitPreviewLayers(
            DrawContext context,
            int x,
            int y,
            int size,
            float mouseX,
            float mouseY,
            LivingEntity entity,
            CallbackInfo ci
    ) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) {
            return;
        }

        ItemStack chest = com.cerulixteam.gallifrey.client.VisibleArmor.get(player, EquipmentSlot.CHEST);
        ItemStack legs = com.cerulixteam.gallifrey.client.VisibleArmor.get(player, EquipmentSlot.LEGS);
        ItemStack feet = com.cerulixteam.gallifrey.client.VisibleArmor.get(player, EquipmentSlot.FEET);

        boolean suitChest = chest.isOf(
                GallifreyModItems.SPACE_SUIT_CHESTPLATE);
        boolean suitLegs = legs.isOf(
                GallifreyModItems.SPACE_SUIT_LEGGINGS);
        boolean suitBoots = feet.isOf(
                GallifreyModItems.SPACE_SUIT_BOOTS);

        EntityRenderDispatcher dispatcher =
                MinecraftClient.getInstance().getEntityRenderDispatcher();

        if (!(dispatcher.getRenderer(player)
                instanceof PlayerEntityRenderer renderer)) {
            return;
        }

        PlayerEntityModel<AbstractClientPlayerEntity> model =
                ((LivingEntityRendererAccessor<
                        AbstractClientPlayerEntity,
                        PlayerEntityModel<AbstractClientPlayerEntity>
                        >) (Object) renderer).gallifrey$getModel();

        // Hide outer skin layers covered by the chestplate.
        model.jacket.visible = !suitChest;
        model.leftSleeve.visible = !suitChest;
        model.rightSleeve.visible = !suitChest;

        // Hide outer skin layers covered by the leggings.
        model.leftPants.visible = !suitLegs;
        model.rightPants.visible = !suitLegs;

        // Keep the head and hat visible.
        model.head.visible = true;
        model.hat.visible = true;

        // Hide the base body beneath equipped armor.
        model.body.visible = !suitChest;
        model.leftArm.visible = !suitChest;
        model.rightArm.visible = !suitChest;

        model.leftLeg.visible = !suitLegs;
        model.rightLeg.visible = !suitLegs;
    }
}