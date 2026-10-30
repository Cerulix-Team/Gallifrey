package com.cerulixteam.gallifrey.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;

public class VortexIsomorphicScreen extends VortexManipulatorSubScreen {
    private TextFieldWidget accessPlayer;

    public VortexIsomorphicScreen() { super("Vortex Manipulator - Isomorphic Controls"); }

    @Override protected void build() {
        accessPlayer = field(left + 24, top + 92, 350, "Online player name");
        ButtonWidget add = btn(left + 386, top + 92, 130, 24, "ADD PLAYER", b -> addPlayer());
        add.active = owner;
        addDrawableChild(add);

        for (int i = 0; i < USERS.size() && i < 5; i++) {
            AccessUser user = USERS.get(i);
            int yy = top + 132 + i * 30;
            addDrawableChild(btn(left + 24, yy, 390, 24, user.name(), b -> accessPlayer.setText(user.name())));
            ButtonWidget rem = btn(left + 424, yy, 92, 24, "REMOVE", b -> removePlayer(user.name()));
            rem.active = owner;
            addDrawableChild(rem);
        }
        ButtonWidget protection = btn(left + 24, top + 286, 492, 22,
                blockInboundTeleports ? "INBOUND TELEPORTS: BLOCKED" : "INBOUND TELEPORTS: ALLOWED",
                b -> toggleInboundTeleports());
        protection.active = owner;
        addDrawableChild(protection);
        buildBackButton();
    }

    private void addPlayer() {
        String name = accessPlayer.getText().trim();
        if (!name.isEmpty()) sendString("ADD_PLAYER", name);
    }

    private void removePlayer(String name) {
        sendString("REMOVE_PLAYER", name);
    }

    private void toggleInboundTeleports() {
        if (!owner) return;
        net.minecraft.network.PacketByteBuf p = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        p.writeString("SET_INBOUND_TELEPORTS");
        p.writeBoolean(!blockInboundTeleports);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(com.cerulixteam.gallifrey.networking.ModPackets.VM_PACKET, p);
    }

    @Override protected void renderVortexChrome(DrawContext c, int mx, int my, float delta) {
        buildHeader(c, "ISOMORPHIC CONTROLS");
        c.drawText(textRenderer, "Only the VM owner can change authorized users.", left + 24, top + 116, DIM, false);
    }
}
