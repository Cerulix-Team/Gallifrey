package com.cerulixteam.gallifrey.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class VortexSelfDestructScreen extends VortexManipulatorSubScreen {
    @Override protected int accent() { return 0xFFFF3B30; }
    @Override protected int accentDim() { return 0xFF9E201A; }
    @Override protected int accentDark() { return 0xFF4A0B08; }
    @Override protected int panel() { return 0xF0100707; }
    @Override protected int panelLight() { return 0xFF21100E; }
    @Override protected int panelDark() { return 0xFF060202; }
    @Override protected int dimColor() { return 0xFFC58D86; }
    public VortexSelfDestructScreen() { super("Vortex Manipulator - Self Destruct"); }

    @Override protected void build() {
        if (selfDestructTicks > 0) {
            int seconds = (selfDestructTicks + 19) / 20;
            addDrawableChild(btn(left + 24, top + 115, 492, 42,
                    "SELF-DESTRUCT ARMED — " + seconds + "s",
                    b -> selfDestructCommand("CANCEL_SELF_DESTRUCT")));
            addDrawableChild(btn(left + 24, top + 175, 492, 32, "CANCEL SELF-DESTRUCT",
                    b -> selfDestructCommand("CANCEL_SELF_DESTRUCT")));
        } else {
            ButtonWidget arm = btn(left + 24, top + 125, 492, 42, "ARM SELF-DESTRUCT (10 SECONDS)",
                    b -> selfDestructCommand("SELF_DESTRUCT"));
            arm.active = owner;
            addDrawableChild(arm);
        }
        addDrawableChild(btn(left + 24, top + 240, 492, 30, "BACK TO MAIN",
                b -> client.setScreen(new VortexManipulatorScreen())));
        buildBackButton();
    }

    @Override protected void renderVortexChrome(DrawContext c, int mx, int my, float delta) {
        buildHeader(c, "");
        // Hazard-stripe warning bands.
        for (int x = left + 14; x < left + W - 14; x += 28) {
            c.fill(x, top + 48, Math.min(x + 14, left + W - 14), top + 54, 0xFFFFC857);
            c.fill(Math.min(x + 14, left + W - 14), top + 48, Math.min(x + 28, left + W - 14), top + 54, 0xFF21100E);
            c.fill(x, top + 286, Math.min(x + 14, left + W - 14), top + 292, 0xFFFFC857);
            c.fill(Math.min(x + 14, left + W - 14), top + 286, Math.min(x + 28, left + W - 14), top + 292, 0xFF21100E);
        }
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("⚠ SELF-DESTRUCT WARNING ⚠"), left + W / 2, top + 66, 0xFFFFC857);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("OWNER AUTHORIZATION REQUIRED"), left + W / 2, top + 86, RED);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("THE MANIPULATOR WILL BE DESTROYED AFTER 10 SECONDS"), left + W / 2, top + 101, 0xFFEADBD8);
    }
}
