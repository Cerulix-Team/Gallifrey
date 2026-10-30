
package com.cerulixteam.gallifrey.mixin.client;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {

    @Accessor("handler")
    ScreenHandler gallifrey$getHandler();

    @Accessor("x")
    int gallifrey$getX();

    @Accessor("y")
    int gallifrey$getY();

    @Accessor("backgroundHeight")
    int gallifrey$getBackgroundHeight();
}

