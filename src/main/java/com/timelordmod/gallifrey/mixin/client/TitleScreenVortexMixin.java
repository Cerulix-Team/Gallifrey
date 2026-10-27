package com.timelordmod.gallifrey.mixin.client;

import com.timelordmod.gallifrey.client.GallifreyClientConfig;
import com.timelordmod.gallifrey.client.TitleScreenBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Title screen background choice: the normal panorama, the code-drawn time vortex, or one of the
 * looping vortex animations (see MenuBackground).
 *
 * - Doesn't touch the panorama line in render(): Adventures in Time @Redirects that line, and
 *   two mods hooking the same line is what left the game stuck on the red loading screen.
 *   Instead, render() is marked start to end (HEAD / RETURN, which always exist) and
 *   RotatingCubeMapRendererMixin swaps the panorama for the vortex while it's marked.
 * - Adds a small button in the bottom-left corner that cycles through the backgrounds.
 *   The choice is saved in config/gallifrey-client.properties.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenVortexMixin extends Screen {

    protected TitleScreenVortexMixin(Text title) {
        super(title);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"))
    private void gallifrey$beginBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleScreenBackground.begin(context, this.width, this.height);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("RETURN"))
    private void gallifrey$endBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleScreenBackground.end();
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void gallifrey$addBackgroundButton(CallbackInfo ci) {
        int buttonWidth = 150;
        this.addDrawableChild(ButtonWidget.builder(gallifrey$label(), button -> {
                    GallifreyClientConfig.cycleMenuBackground();
                    button.setMessage(gallifrey$label());
                })
                // Bottom-left corner, just above the "Minecraft 1.20.1" version text.
                .dimensions(4, this.height - 34, buttonWidth, 20)
                .tooltip(Tooltip.of(Text.translatable("gui.gallifrey.menu_background.tooltip")))
                .build());
    }

    private static Text gallifrey$label() {
        return GallifreyClientConfig.getMenuBackgroundLabel();
    }
}
