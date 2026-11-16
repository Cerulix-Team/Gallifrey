package com.cerulixteam.gallifrey.sonic;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class SonicModes {

    private static final String MODE_KEY = "SonicMode";

    private SonicModes() {}

    public static SonicMode getMode(ItemStack stack) {
        int mode = stack.getOrCreateNbt().getInt(MODE_KEY);
        SonicMode[] modes = SonicMode.values();
        if (mode < 0 || mode >= modes.length) {
            return SonicMode.INTERACTION;
        }
        return modes[mode];
    }

    public static void setMode(ItemStack stack, SonicMode mode) {
        stack.getOrCreateNbt().putInt(MODE_KEY, mode.ordinal());
    }

    public static void nextMode(ItemStack stack, net.minecraft.entity.player.PlayerEntity player) {
        SonicMode[] modes = SonicMode.values();
        SonicMode current = getMode(stack);
        SonicMode next = modes[(current.ordinal() + 1) % modes.length];
        setMode(stack, next);

        player.sendMessage(
                Text.literal("§bSONIC MODE: §f" + next.getDisplayName()),
                true
        );
    }
}
