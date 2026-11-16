package com.cerulixteam.gallifrey.mixin.client;

import com.cerulixteam.gallifrey.item.custom.AccessorySlotIndices;
import com.cerulixteam.gallifrey.item.custom.AccessorySlotLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The creative inventory tab lays out the player's slots by number, and anything past the offhand
 * slot lands on the hotbar row. The Sonic Shades slot ended up on top of hotbar slot 2, so clicks
 * went to one or the other at random. This moves the Sonic Shades and fez slots to the free space
 * right of the armour slots (under Trinkets' row when Trinkets is installed, see
 * AccessorySlotLayout) and draws their slot backgrounds.
 */
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryAccessorySlotsMixin {
    @Unique private static final Identifier GALLIFREY$INVENTORY_TEXTURE = new Identifier("minecraft", "textures/gui/container/inventory.png");

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void gallifrey$placeAccessorySlots(ItemGroup group, CallbackInfo ci) {
        if (group.getType() != ItemGroup.Type.INVENTORY) return;
        if (com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.usesTrinkets()) return; // Trinkets' own slots instead
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        AccessorySlotIndices indices = (AccessorySlotIndices) client.player.playerScreenHandler;
        // The creative handler holds one wrapper per player slot, in the same order.
        ScreenHandler handler = ((HandledScreen<?>) (Object) this).getScreenHandler();
        gallifrey$move(handler, indices.gallifrey$shadesSlotIndex(),
                AccessorySlotLayout.CREATIVE_SHADES_X, AccessorySlotLayout.CREATIVE_SHADES_Y);
        gallifrey$move(handler, indices.gallifrey$fezSlotIndex(),
                AccessorySlotLayout.CREATIVE_FEZ_X, AccessorySlotLayout.CREATIVE_FEZ_Y);
    }

    @Unique
    private static void gallifrey$move(ScreenHandler handler, int index, int x, int y) {
        if (index < 0 || index >= handler.slots.size()) return;
        Slot slot = handler.slots.get(index);
        ((SlotAccessor) slot).gallifrey$setX(x);
        ((SlotAccessor) slot).gallifrey$setY(y);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void gallifrey$drawAccessorySlots(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        ItemGroup tab = CreativeInventoryScreenAccessor.gallifrey$getSelectedTab();
        if (tab == null || tab.getType() != ItemGroup.Type.INVENTORY) return;
        if (com.cerulixteam.gallifrey.item.custom.GallifreyAccessories.usesTrinkets()) return; // Trinkets' own slots instead
        HandledScreenAccessor screen = (HandledScreenAccessor) this;
        int x = screen.gallifrey$getX() - 1;
        int y = screen.gallifrey$getY() - 1;
        context.drawTexture(GALLIFREY$INVENTORY_TEXTURE,
                x + AccessorySlotLayout.CREATIVE_SHADES_X, y + AccessorySlotLayout.CREATIVE_SHADES_Y,
                7.0F, 83.0F, 18, 18, 256, 256);
        context.drawTexture(GALLIFREY$INVENTORY_TEXTURE,
                x + AccessorySlotLayout.CREATIVE_FEZ_X, y + AccessorySlotLayout.CREATIVE_FEZ_Y,
                7.0F, 83.0F, 18, 18, 256, 256);
    }
}
