package com.cerulixteam.gallifrey.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDate;
import java.time.Month;

/*
 * Priority 100: with several mods replacing the title logo the same way (RegenCraft does too),
 * the one with the LOWEST priority number runs first, draws its logo and cancels the rest.
 * 10001 made Gallifrey run last, so RegenCraft's logo always won.
 */
@Mixin(value = LogoDrawer.class, priority = 100)
public class LogoDrawerMixin {

    @Unique
    private static final Identifier GALL_STANDARD =
            new Identifier("gallifrey", "textures/gui/title/gall_standard.png");

    @Unique
    private static final Identifier GALL_XMAS =
            new Identifier("gallifrey", "textures/gui/title/gall_xmas.png");

    @Unique
    private static final Identifier GALL_DWDAY =
            new Identifier("gallifrey", "textures/gui/title/gall_dwday.png");

    @Unique
    private static final Identifier GALL_REGENDAY =
            new Identifier("gallifrey", "textures/gui/title/gall_regenday.png");

    @Unique
    private static final Identifier GALL_PRIDE =
            new Identifier("gallifrey", "textures/gui/title/gall_pride.png");

    @Unique
    private static final Identifier GALL_BLUESDAY =
            new Identifier("gallifrey", "textures/gui/title/gall_bluesday.png");

    @Unique
    private static final Identifier GALL_HALLOWEEN =
            new Identifier("gallifrey", "textures/gui/title/gall_halloween.png");

    @Unique
    private static final Identifier GALL_LIGHTSDAY =
            new Identifier("gallifrey", "textures/gui/title/gall_lightsday.png");

    @Unique
    private static final Identifier GALL_DEOSDAY =
            new Identifier("gallifrey", "textures/gui/title/gall_deosday.png");

    @Inject(
            method = "draw(Lnet/minecraft/client/gui/DrawContext;IF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gallifrey$replaceLogo(DrawContext context, int screenWidth, float alpha, CallbackInfo ci) {

        Identifier logo = getLogo();

        int logoWidth = 380;
        int logoHeight = 45;

        int drawX = (screenWidth - logoWidth) / 2;
        int drawY = 15;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        context.drawTexture(
                logo,
                drawX,
                drawY,
                0,
                0,
                logoWidth,
                logoHeight,
                logoWidth,
                logoHeight
        );

        ci.cancel();
    }


    @Unique
    private Identifier getLogo() {
        LocalDate date = LocalDate.now();

        if (date.getMonth() == Month.DECEMBER) {
            return GALL_XMAS;
        }

        if (date.getMonth() == Month.OCTOBER && date.getDayOfMonth() == 31) {
            return GALL_HALLOWEEN;
        }

        if (date.getMonth() == Month.OCTOBER && date.getDayOfMonth() == 15) {
            return GALL_BLUESDAY;
        }

        if (date.getMonth() == Month.JUNE && date.getDayOfMonth() == 10) {
            return GALL_DEOSDAY;
        }

        if (date.getMonth() == Month.OCTOBER && date.getDayOfMonth() == 16) {
            return GALL_LIGHTSDAY;
        }

        if (date.getMonth() == Month.NOVEMBER && date.getDayOfMonth() == 23) {
            return GALL_DWDAY;
        }

        if (date.getMonth() == Month.JULY && date.getDayOfMonth() == 4) {
            return GALL_REGENDAY;
        }

        if (date.getMonth() == Month.JUNE) {
            return GALL_PRIDE;
        }

        return GALL_STANDARD;
    }
}