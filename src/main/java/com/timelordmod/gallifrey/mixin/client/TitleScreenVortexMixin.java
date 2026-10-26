package com.timelordmod.gallifrey.mixin.client;

import com.timelordmod.gallifrey.client.GallifreyClientConfig;
import com.timelordmod.gallifrey.client.MenuBackground;
import com.timelordmod.gallifrey.client.VortexBackgroundRenderer;
import com.timelordmod.gallifrey.client.VideoVortexRenderer;
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
 * Provides Gallifrey's selectable title-screen backgrounds.
 *
 * The panorama is allowed to render normally so other mods, including
 * Adventures in Time, can modify its rendering.
 *
 * Gallifrey draws its selected vortex background immediately afterward.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenVortexMixin extends Screen {

    protected TitleScreenVortexMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void gallifrey$drawMenuBackground(
            DrawContext context,
            int mouseX,
            int mouseY,
            float tickDelta,
            CallbackInfo ci
    ) {
        MenuBackground background = GallifreyClientConfig.getMenuBackground();

        if (background == MenuBackground.VORTEX) {
            VortexBackgroundRenderer.render(
                    context,
                    this.width,
                    this.height,
                    1.0F
            );
        } else if (background.frameFolder() != null) {
            VideoVortexRenderer.render(
                    context,
                    this.width,
                    this.height,
                    1.0F,
                    background.frameFolder()
            );
        }
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void gallifrey$addBackgroundButton(CallbackInfo ci) {
        int buttonWidth = 150;

        this.addDrawableChild(
                ButtonWidget.builder(
                                gallifrey$label(),
                                button -> {
                                    GallifreyClientConfig.cycleMenuBackground();
                                    button.setMessage(gallifrey$label());
                                }
                        )
                        .dimensions(4, this.height - 34, buttonWidth, 20)
                        .tooltip(
                                Tooltip.of(
                                        Text.translatable(
                                                "gui.gallifrey.menu_background.tooltip"
                                        )
                                )
                        )
                        .build()
        );
    }

    private static Text gallifrey$label() {
        return Text.translatable(
                GallifreyClientConfig.getMenuBackground().translationKey()
        );
    }
}