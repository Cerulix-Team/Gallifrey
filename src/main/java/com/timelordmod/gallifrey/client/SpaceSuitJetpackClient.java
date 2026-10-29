package com.timelordmod.gallifrey.client;

import com.timelordmod.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** The jetpack key (R by default, in the Gallifrey section of Controls). The server checks the suit. */
public final class SpaceSuitJetpackClient {
    private static KeyBinding activateJetpack;
    private SpaceSuitJetpackClient() {}

    public static void register() {
        activateJetpack = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gallifrey.space_suit_jetpack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.gallifrey"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (activateJetpack.wasPressed()) {
                if (client.player == null || client.world == null) continue;
                ClientPlayNetworking.send(ModPackets.SPACE_SUIT_JETPACK, PacketByteBufs.empty());
            }
        });
    }
}
