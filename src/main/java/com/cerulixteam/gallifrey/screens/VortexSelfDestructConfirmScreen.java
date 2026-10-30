package com.cerulixteam.gallifrey.screens;

import com.cerulixteam.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

/** Confirmation screen shown before the Vortex Manipulator self-destructs. */
public class VortexSelfDestructConfirmScreen extends Screen {
    private static final int W = 500, H = 210;
    private static final int PANEL = 0xF0100707, PANEL_LIGHT = 0xFF21100E, PANEL_DARK = 0xFF060202;
    private static final int RED = 0xFFFF3B30, RED_DIM = 0xFF9E201A, RED_DARK = 0xFF4A0B08;
    private static final int TEXT = 0xFFF4E9E7, DIM = 0xFFC58D86, YELLOW = 0xFFFFC857;

    private final Screen parent;
    private int left, top;

    public VortexSelfDestructConfirmScreen(Screen parent) {
        super(Text.literal("Vortex Manipulator Self-Destruct"));
        this.parent = parent;
    }

    @Override protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        addDrawableChild(new ThemedButton(left + 16, top + 150, 226, 30, Text.literal("YES — SELF-DESTRUCT"), b -> confirm()));
        addDrawableChild(new ThemedButton(left + 258, top + 150, 226, 30, Text.literal("NO — GO BACK"), b -> client.setScreen(parent)));
    }

    private void confirm() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("SELF_DESTRUCT");
        ClientPlayNetworking.send(ModPackets.VM_PACKET, buf);
        client.setScreen(null);
    }

    @Override public void close() {
        client.setScreen(parent);
    }

    private float vortexGuiScale() {
        double guiScale = client.getWindow().getScaleFactor();
        float desired = (float) (3.5D / guiScale);
        float fit = Math.min((width - 20.0F) / W, (height - 20.0F) / H);
        return Math.min(desired, fit);
    }

    private int scaledMouseX(double mouseX) {
        float s = vortexGuiScale();
        return Math.round((float) (width * 0.5D + (mouseX - width * 0.5D) / s));
    }

    private int scaledMouseY(double mouseY) {
        float s = vortexGuiScale();
        return Math.round((float) (height * 0.5D + (mouseY - height * 0.5D) / s));
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x99000000);
        float s = vortexGuiScale();
        context.getMatrices().push();
        context.getMatrices().translate(width * 0.5F, height * 0.5F, 0.0F);
        context.getMatrices().scale(s, s, 1.0F);
        context.getMatrices().translate(-width * 0.5F, -height * 0.5F, 0.0F);
        context.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0x554A0B08);
        context.fill(left, top, left + W, top + H, PANEL);
        context.drawBorder(left, top, W, H, RED);
        context.drawBorder(left + 4, top + 4, W - 8, H - 8, RED_DARK);
        context.fill(left + 8, top + 8, left + W - 8, top + 28, PANEL_LIGHT);
        context.drawText(textRenderer, "VORTEX MANIPULATOR", left + 16, top + 12, RED, false);
        for (int x = left + 10; x < left + W - 10; x += 28) {
            context.fill(x, top + 31, Math.min(x + 14, left + W - 10), top + 37, YELLOW);
            context.fill(Math.min(x + 14, left + W - 10), top + 31, Math.min(x + 28, left + W - 10), top + 37, RED_DARK);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("⚠  SELF-DESTRUCT  ⚠"), left + W / 2, top + 52, YELLOW);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Arm the Vortex Manipulator self-destruct sequence?"), left + W / 2, top + 76, TEXT);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("YES starts a 10-second countdown."), left + W / 2, top + 99, RED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("You can cancel it before detonation."), left + W / 2, top + 114, RED);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("NO returns you to the control console."), left + W / 2, top + 129, DIM);
        super.render(context, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        context.getMatrices().pop();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float s = vortexGuiScale();
        return super.mouseDragged(scaledMouseX(mouseX), scaledMouseY(mouseY), button, deltaX / s, deltaY / s);
    }

    @Override public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(scaledMouseX(mouseX), scaledMouseY(mouseY));
    }

    private String fit(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String ellipsis = "…";
        int end = value.length();
        while (end > 0 && textRenderer.getWidth(value.substring(0, end) + ellipsis) > maxWidth) end--;
        return end <= 0 ? ellipsis : value.substring(0, end) + ellipsis;
    }

    private class ThemedButton extends ButtonWidget {
        ThemedButton(int x, int y, int w, int h, Text message, PressAction action) {
            super(x, y, w, h, message, action, DEFAULT_NARRATION_SUPPLIER);
        }
        @Override public void renderButton(DrawContext c, int mx, int my, float delta) {
            int bg = isHovered() ? 0xFF4A0B08 : PANEL_DARK;
            c.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            c.drawBorder(getX(), getY(), getWidth(), getHeight(), active ? RED : RED_DIM);
            Text visible = Text.literal(fit(getMessage().getString(), Math.max(12, getWidth() - 12)));
            c.drawCenteredTextWithShadow(textRenderer, visible, getX() + getWidth() / 2, getY() + getHeight() / 2 - 4, active ? TEXT : DIM);
        }
    }
}
