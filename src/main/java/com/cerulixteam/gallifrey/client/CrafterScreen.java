package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.block.backport.CrafterScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** The Crafter's screen: click an empty grid slot (with nothing held) to switch it off or back on. */
public class CrafterScreen extends HandledScreen<CrafterScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("gallifrey", "textures/gui/container/crafter.png");
    private static final Identifier DISABLED = new Identifier("gallifrey", "textures/gui/container/crafter/disabled_slot.png");
    private static final Identifier POWERED = new Identifier("gallifrey", "textures/gui/container/crafter/powered_redstone.png");
    private static final Identifier UNPOWERED = new Identifier("gallifrey", "textures/gui/container/crafter/unpowered_redstone.png");

    public CrafterScreen(CrafterScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slot != null && slot.id < 9 && !slot.hasStack() && this.handler.getCursorStack().isEmpty() && this.client != null && this.client.interactionManager != null) {
            this.client.interactionManager.clickButton(this.handler.syncId, slot.id);
            return;
        }
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        for (int i = 0; i < 9; i++) {
            if (this.handler.isDisabled(i)) {
                Slot slot = this.handler.slots.get(i);
                context.drawTexture(DISABLED, x + slot.x - 1, y + slot.y - 1, 0, 0, 18, 18, 18, 18);
            }
        }
        context.drawTexture(this.handler.isPowered() ? POWERED : UNPOWERED, x + 97, y + 35, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
