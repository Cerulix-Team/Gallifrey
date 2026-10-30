package com.cerulixteam.gallifrey.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Compact Vortex Manipulator destination reference for built-in Minecraft/Gallifrey dimensions. */
public class VortexDimensionsScreen extends Screen {
    private static final int W = 540, H = 360;
    private static final int PANEL = 0xF0091118, PANEL_LIGHT = 0xFF101D25, PANEL_DARK = 0xFF080D12;
    private static final int TEXT = 0xFFE7FBFF, DIM = 0xFF75AAB5;
    private static final int AMBER = 0xFFFFC857, AMBER_DIM = 0xFF9A6A1F;
    private static final int SILVER = 0xFFD7DCE0, STEEL = 0xFF7D858C;

    private static final List<Destination> DESTINATIONS = List.of(
            new Destination("Overworld", "minecraft:overworld"),
            new Destination("Nether", "minecraft:the_nether"),
            new Destination("The End", "minecraft:the_end"),
            new Destination("Gallifrey", "gallifrey:gallifrey"),
            new Destination("Skaro", "gallifrey:skaro"),
            new Destination("Mars", "gallifrey:mars"),
            new Destination("Moon", "gallifrey:moon"),
            new Destination("Mondas", "gallifrey:mondas"),
            new Destination("Prehistoric", "gallifrey:prehistoric"),
            new Destination("Classic", "gallifrey:classic"),
            new Destination("Classic Nether", "gallifrey:classic_nether"),
            new Destination("Pete's World", "gallifrey:petes_world")
    );

    private final VortexManipulatorScreen parent;
    private int left, top;

    public VortexDimensionsScreen(VortexManipulatorScreen parent) {
        super(Text.literal("Vortex Manipulator - Dimensions"));
        this.parent = parent;
    }

    @Override protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        int rowY = top + 70;
        for (int i = 0; i < DESTINATIONS.size(); i++) {
            Destination d = DESTINATIONS.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = left + 18 + col * 252;
            int y = rowY + row * 38;
            addDrawableChild(button(x, y, 240, 30, d.name + "  •  " + d.id, b -> select(d.id)));
        }

        addDrawableChild(button(left + 18, top + H - 34, 110, 22, "◀ BACK", b -> client.setScreen(parent)));
    }

    private void select(String id) {
        client.setScreen(parent);
        parent.setDimensionValue(id);
    }

    private ButtonWidget button(int x, int y, int w, int h, String label, ButtonWidget.PressAction action) {
        return new ThemedButtonWidget(x, y, w, h, Text.literal(label), action);
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

    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x99000000);
        float s = vortexGuiScale();
        c.getMatrices().push();
        c.getMatrices().translate(width * 0.5F, height * 0.5F, 0.0F);
        c.getMatrices().scale(s, s, 1.0F);
        c.getMatrices().translate(-width * 0.5F, -height * 0.5F, 0.0F);
        c.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0x55373E44);
        c.fill(left, top, left + W, top + H, PANEL);
        c.drawBorder(left, top, W, H, SILVER);
        c.drawBorder(left + 4, top + 4, W - 8, H - 8, STEEL);
        c.fill(left + 10, top + 10, left + W - 10, top + 34, PANEL_LIGHT);
        c.fill(left + 10, top + 33, left + W - 10, top + 34, AMBER_DIM);
        c.drawText(textRenderer, "VORTEX MANIPULATOR", left + 20, top + 17, AMBER, false);
        c.drawText(textRenderer, "DIMENSION DIRECTORY", left + 20, top + 45, AMBER, false);
        c.drawText(textRenderer, "SELECT A DESTINATION", left + 300, top + 45, DIM, false);
        super.render(c, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        c.drawText(textRenderer, "Other mods: enter their dimension ID as namespace:modid (example: othermod:dimension_name).",
                left + 18, top + H - 58, DIM, false);
        c.drawText(textRenderer, "The namespace is normally the mod ID; use the exact ID supplied by that mod.",
                left + 18, top + H - 47, DIM, false);
        c.getMatrices().pop();
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

    private class ThemedButtonWidget extends ButtonWidget {
        ThemedButtonWidget(int x, int y, int w, int h, Text message, PressAction action) {
            super(x, y, w, h, message, action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override public void renderButton(DrawContext c, int mx, int my, float delta) {
            int bg = isHovered() ? 0xFF123744 : PANEL_DARK;
            c.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            c.drawBorder(getX(), getY(), getWidth(), getHeight(), isHovered() ? AMBER : STEEL);
            c.drawCenteredTextWithShadow(textRenderer, getMessage(), getX() + getWidth() / 2,
                    getY() + getHeight() / 2 - 4, TEXT);
        }
    }

    private record Destination(String name, String id) {}
}
