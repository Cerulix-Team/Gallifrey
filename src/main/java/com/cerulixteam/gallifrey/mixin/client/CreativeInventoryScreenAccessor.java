package com.cerulixteam.gallifrey.mixin.client;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeInventoryScreen.class)
public interface CreativeInventoryScreenAccessor {

    /** The creative tab currently open (a private static field in vanilla). */
    @Accessor("selectedTab")
    static ItemGroup gallifrey$getSelectedTab() {
        throw new AssertionError();
    }

    /** Moves the scroll bar thumb back to the top when we swap the item list. */
    @Accessor("scrollPosition")
    void gallifrey$setScrollPosition(float scrollPosition);
}
