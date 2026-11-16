package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.item.custom.SonicShadesSlotAccess;
import com.cerulixteam.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryGlassesSlotMixin {

    private static final Identifier GALLIFREY$INVENTORY_TEXTURE =
            new Identifier("minecraft", "textures/gui/container/inventory.png");

    private static final Identifier GALLIFREY$GLASSES_ICON =
            new Identifier("gallifrey", "textures/gui/empty_glasses_slot.png");

    // Keep these coordinates identical for drawing and clicking.
    // Intended position: immediately left of the offhand slot.
    private int gallifrey$getShadesSlotX() {
        return ((HandledScreenAccessor) this).gallifrey$getX() + 59;
    }

    private int gallifrey$getShadesSlotY() {
        return ((HandledScreenAccessor) this).gallifrey$getY() + 62;
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void gallifrey$drawCreativeGlassesSlot(
            DrawContext context,
            float delta,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        CreativeInventoryScreen screen =
                (CreativeInventoryScreen) (Object) this;

        if (!screen.isInventoryTabSelected()) {
            return;
        }

        int slotX = gallifrey$getShadesSlotX();
        int slotY = gallifrey$getShadesSlotY();

        // Draw the slot background.
        context.drawTexture(
                GALLIFREY$INVENTORY_TEXTURE,
                slotX,
                slotY,
                7,
                83,
                18,
                18,
                256,
                256
        );

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return;
        }

        ItemStack shades =
                ((SonicShadesSlotAccess) client.player)
                        .gallifrey$getSonicShades();

        if (shades.isEmpty()) {
            context.drawTexture(
                    GALLIFREY$GLASSES_ICON,
                    slotX + 1,
                    slotY + 1,
                    0,
                    0,
                    16,
                    16,
                    16,
                    16
            );
        } else {
            context.drawItem(shades, slotX + 1, slotY + 1);
            context.drawItemInSlot(
                    client.textRenderer,
                    shades,
                    slotX + 1,
                    slotY + 1
            );
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gallifrey$handleCreativeGlassesClick(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir
    ) {
        CreativeInventoryScreen screen =
                (CreativeInventoryScreen) (Object) this;

        if (!screen.isInventoryTabSelected() || button != 0) {
            return;
        }

        int slotX = gallifrey$getShadesSlotX();
        int slotY = gallifrey$getShadesSlotY();

        if (mouseX < slotX || mouseX >= slotX + 18
                || mouseY < slotY || mouseY >= slotY + 18) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            ClientPlayNetworking.send(
                    ModPackets.SONIC_SHADES_SLOT_SWAP,
                    PacketByteBufs.create()
            );
        }

        cir.setReturnValue(true);
    }
}