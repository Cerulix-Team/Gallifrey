package com.cerulixteam.gallifrey.screens;

import com.cerulixteam.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VortexNavigationScreen extends VortexManipulatorSubScreen {
    @Override protected int accent() { return 0xFFFFC857; }
    @Override protected int accentDim() { return 0xFF9A6A1F; }
    @Override protected int accentDark() { return 0xFF4B3211; }
    @Override protected int panel() { return 0xF00B0A08; }
    @Override protected int panelLight() { return 0xFF17130C; }
    @Override protected int panelDark() { return 0xFF070807; }
    @Override protected int dimColor() { return 0xFFAAA397; }
    private TextFieldWidget dimension, x, y, z, targetPlayer;
    private boolean surfaceMode;

    public VortexNavigationScreen() { super("Vortex Manipulator - Navigation"); }

    @Override protected void build() {
        dimension = field(left + 24, top + 92, 492, "Dimension");
        dimension.setText("overworld");
        targetPlayer = field(left + 24, top + 137, 492, "Player target (optional)");
        x = field(left + 24, top + 184, 150, "X");
        y = field(left + 195, top + 184, 150, "Y");
        z = field(left + 366, top + 184, 150, "Z");

        addDrawableChild(btn(left + 24, top + 224, 150, 24, "SURFACE: OFF",
                b -> { surfaceMode = !surfaceMode; b.setMessage(Text.literal(surfaceMode ? "SURFACE: ON" : "SURFACE: OFF")); }));
        addDrawableChild(btn(left + 195, top + 224, 150, 24, "ENGAGE VORTEX", b -> teleport()));
        addDrawableChild(btn(left + 366, top + 224, 150, 24, "SELF-DESTRUCT",
                b -> client.setScreen(new VortexSelfDestructScreen())));
        buildBackButton();
    }

    private void teleport() {
        String playerName = targetPlayer.getText().trim();
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString("TELEPORT");
        p.writeBoolean(!playerName.isEmpty());
        if (!playerName.isEmpty()) {
            p.writeString(playerName, 64);
        } else {
            Identifier id = parseDimension(dimension.getText());
            Double tx = parse(x.getText()), ty = parse(y.getText()), tz = parse(z.getText());
            if (id == null || tx == null || ty == null || tz == null) {
                client.player.sendMessage(Text.literal("INVALID DESTINATION"), true);
                return;
            }
            p.writeIdentifier(id);
            p.writeDouble(tx); p.writeDouble(ty); p.writeDouble(tz);
            p.writeBoolean(surfaceMode);
        }
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
        client.setScreen(null);
    }

    private Double parse(String s) {
        try { return s == null || s.trim().isEmpty() ? null : Double.parseDouble(s.trim()); }
        catch (Exception e) { return null; }
    }

    private Identifier parseDimension(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String n = input.trim().toLowerCase();
        // Names with spaces can't be dimension IDs, so match them by hand.
        // Ignores spaces, apostrophes, underscores and hyphens: "Pete's World", "petes world", "petes_world" all work.
        String squashed = n.replace("'", "").replace("\u2019", "").replace(" ", "").replace("_", "").replace("-", "");
        if (squashed.equals("petesworld")) return new Identifier("gallifrey", "petes_world");
        return switch (n) {
            case "overworld" -> new Identifier("minecraft", "overworld");
            case "nether" -> new Identifier("minecraft", "the_nether");
            case "end" -> new Identifier("minecraft", "the_end");
            case "gallifrey" -> new Identifier("gallifrey", "gallifrey");
            case "skaro" -> new Identifier("gallifrey", "skaro");
            case "mars" -> new Identifier("gallifrey", "mars");
            case "mondas" -> new Identifier("gallifrey", "mondas");
            case "prehistoric" -> new Identifier("gallifrey", "prehistoric");
            case "classic" -> new Identifier("gallifrey", "classic");
            default -> Identifier.tryParse(n);
        };
    }

    @Override protected void renderVortexChrome(DrawContext c, int mx, int my, float delta) {
        buildHeader(c, "");
        c.drawText(textRenderer, "DIMENSION", left + 24, top + 82, dimColor(), false);
        c.drawText(textRenderer, "PLAYER TARGET — OPTIONAL", left + 24, top + 127, dimColor(), false);
    }
}
