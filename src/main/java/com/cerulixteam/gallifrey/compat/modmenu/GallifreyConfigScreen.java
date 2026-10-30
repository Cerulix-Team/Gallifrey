package com.cerulixteam.gallifrey.compat.modmenu;

import com.cerulixteam.gallifrey.client.GallifreyClientConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Gallifrey's settings, opened from Mod Menu. Plain vanilla widgets, so it needs no config
 * library. Same setting as the title screen button; both save to
 * config/gallifrey-client.properties.
 */
public class GallifreyConfigScreen extends Screen {
    private final Screen parent;

    public GallifreyConfigScreen(Screen parent) {
        super(Text.translatable("gui.gallifrey.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        this.addDrawableChild(ButtonWidget.builder(menuBackgroundLabel(), button -> {
                    GallifreyClientConfig.cycleMenuBackground();
                    button.setMessage(menuBackgroundLabel());
                })
                .dimensions(x, this.height / 4 + 24, 200, 20)
                .tooltip(Tooltip.of(Text.translatable("gui.gallifrey.menu_background.tooltip")))
                .build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .dimensions(x, this.height - 28, 200, 20)
                .build());
    }

    private static Text menuBackgroundLabel() {
        return GallifreyClientConfig.getMenuBackgroundLabel();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }
}
