package com.cerulixteam.gallifrey.screens;

import com.cerulixteam.gallifrey.networking.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Main Vortex Manipulator control console. */
public class VortexManipulatorScreen extends Screen {
    private static final int W = 500, H = 280;
    private static final int PANEL = 0xF00B0A08, PANEL_LIGHT = 0xFF17130C, PANEL_DARK = 0xFF070807;
    private static final int GOLD = 0xFFFFC857, GOLD_DIM = 0xFF9A6A1F, GOLD_DARK = 0xFF4B3211;
    private static final int SILVER = 0xFFD7DCE0, SILVER_DIM = 0xFF7D858C;
    private static final int BROWN = 0xFF6A4524;
    private static final int TEXT = 0xFFF5F1E8, DIM = 0xFFAAA397, GREEN = 0xFF54E88A, RED = 0xFFFF5364;

    private int left, top;
    private int tab;
    private int locationPage;
    private boolean owner;
    private int selfDestructTicks;
    private boolean surfaceMode;
    private boolean blockInboundTeleports;
    private ButtonWidget selfDestructStatus;

    private TextFieldWidget dimension, x, y, z, targetPlayer, locationName, accessPlayer;
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<AccessUser> USERS = new ArrayList<>();

    public VortexManipulatorScreen() { super(Text.literal("Vortex Manipulator")); }

    @Override protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        rebuild();
        requestState();
    }

    private void rebuild() {
        clearChildren();
        pendingLabels.clear();
        buildNavigation();
        buildTabs();
    }

    private void buildTabs() {
        addDrawableChild(btn(left + 12, top + 39, 150, 22, "NAVIGATION", b -> {}));
        addDrawableChild(btn(left + 170, top + 39, 150, 22, "SAVED LOCATIONS",
                b -> client.setScreen(new VortexSavedLocationsScreen())));
        addDrawableChild(btn(left + 328, top + 39, 160, 22, "ISOMORPHIC CONTROLS",
                b -> client.setScreen(new VortexIsomorphicScreen())));
    }

    private void buildNavigation() {
        label("DIMENSION", left + 18, top + 74);
        dimension = field(left + 18, top + 86, 382, "overworld or namespace:path");
        dimension.setText("overworld");
        addDrawableChild(btn(left + 406, top + 86, 76, 24, "DIM LIST", b -> client.setScreen(new VortexDimensionsScreen(this))));

        label("PLAYER TARGET — OPTIONAL", left + 18, top + 116);
        targetPlayer = field(left + 18, top + 128, 464, "Online player; overrides coordinates");

        label("DESTINATION COORDINATES", left + 18, top + 158);
        x = field(left + 18, top + 170, 146, "X");
        y = field(left + 177, top + 170, 146, "Y");
        z = field(left + 336, top + 170, 146, "Z");

        addDrawableChild(btn(left + 18, top + 202, 146, 26, surfaceMode ? "SURFACE: ON" : "SURFACE: OFF",
                b -> { surfaceMode = !surfaceMode; b.setMessage(Text.literal(surfaceMode ? "SURFACE: ON" : "SURFACE: OFF")); }));
        addDrawableChild(btn(left + 177, top + 202, 146, 26, "ENGAGE VORTEX", b -> teleport()));
        addDrawableChild(btn(left + 336, top + 202, 146, 26, "SELF-DESTRUCT", b -> openSelfDestructConfirmation()));

        selfDestructStatus = null;
        if (selfDestructTicks > 0) {
            int seconds = (selfDestructTicks + 19) / 20;
            selfDestructStatus = btn(left + 18, top + 236, 464, 26,
                    "SELF-DESTRUCT ARMED — " + seconds + "s   [CANCEL]",
                    b -> sendCommand("CANCEL_SELF_DESTRUCT"));
            addDrawableChild(selfDestructStatus);
            drawHintText = "";
        } else {
            drawHintText = "READY • use DIM LIST for built-in destinations, or enter namespace:dimension_id for other mods.";
        }
    }

    private String drawHintText = "";

    private void buildLocations() {
        label("LOCATION NAME", left + 18, top + 74);
        locationName = field(left + 18, top + 86, 264, "Location name");
        addDrawableChild(btn(left + 292, top + 86, 92, 24, "SAVE HERE", b -> saveLocation()));
        addDrawableChild(btn(left + 392, top + 86, 90, 24, "DELETE", b -> deleteLocation()));
        drawHintText = LOCATIONS.isEmpty() ? "No saved locations yet." : "Select a destination to travel. Names are case-insensitive.";

        int start = locationPage * 5;
        for (int i = 0; i < 5; i++) {
            int idx = start + i;
            if (idx >= LOCATIONS.size()) break;
            Location loc = LOCATIONS.get(idx);
            int yy = top + 120 + i * 27;
            String text = loc.name() + "  •  " + shortDimension(loc.dimension()) +
                    "  (" + fmt(loc.x()) + ", " + fmt(loc.y()) + ", " + fmt(loc.z()) + ")";
            addDrawableChild(btn(left + 18, yy, 374, 24, fit(text, 362), b -> goLocation(loc.name())));
            addDrawableChild(btn(left + 400, yy, 82, 24, "DELETE", b -> { locationName.setText(loc.name()); deleteLocation(); }));
        }
        addDrawableChild(btn(left + 18, top + 252, 100, 22, "◀ PREVIOUS", b -> {
            if (locationPage > 0) { locationPage--; rebuild(); }
        }));
        addDrawableChild(btn(left + 382, top + 252, 100, 22, "NEXT ▶", b -> {
            if ((locationPage + 1) * 5 < LOCATIONS.size()) { locationPage++; rebuild(); }
        }));
    }

    private void buildAccess() {
        label("PLAYER TO AUTHORIZE", left + 18, top + 74);
        accessPlayer = field(left + 18, top + 86, 352, "Player name");
        ButtonWidget add = btn(left + 382, top + 86, 100, 24, "ADD PLAYER", b -> addPlayer());
        add.active = owner;
        addDrawableChild(add);
        drawHintText = owner ? "You are the VM owner. Only online players can be added by name." :
                "ISOMORPHIC LOCK: only the VM owner can change authorized users.";

        for (int i = 0; i < Math.min(5, USERS.size()); i++) {
            AccessUser user = USERS.get(i);
            int yy = top + 120 + i * 27;
            String name = user.name().equals(user.uuid().toString()) ? user.uuid().toString() : user.name();
            addDrawableChild(btn(left + 18, yy, 374, 24, fit(name, 362), b -> accessPlayer.setText(user.name())));
            ButtonWidget remove = btn(left + 400, yy, 82, 24, "REMOVE", b -> removePlayer(user.name()));
            remove.active = owner;
            addDrawableChild(remove);
        }
        String protectionText = blockInboundTeleports
                ? "INBOUND PLAYER TELEPORTS: BLOCKED"
                : "INBOUND PLAYER TELEPORTS: ALLOWED";
        ButtonWidget protection = btn(left + 18, top + 252, 464, 22, protectionText, b -> toggleInboundTeleports());
        protection.active = owner;
        addDrawableChild(protection);
    }

    private TextFieldWidget field(int x, int y, int w, String placeholder) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 24, Text.literal(placeholder));
        f.setMaxLength(128);
        f.setPlaceholder(Text.literal(placeholder));
        f.setEditableColor(TEXT);
        f.setUneditableColor(DIM);
        addDrawableChild(f);
        return f;
    }

    private ButtonWidget btn(int x, int y, int w, int h, String text, ButtonWidget.PressAction action) {
        return new ThemedButtonWidget(x, y, w, h, Text.literal(text), action);
    }

    private final List<String> pendingLabels = new ArrayList<>();

    private void label(String text, int x, int y) {
        pendingLabels.add(x + "\n" + y + "\n" + text);
    }

    private void switchTab(int newTab) {
        tab = newTab;
        rebuild();
    }

    private void requestState() {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString("REQUEST_STATE");
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }

    public static void applyServerState(PacketByteBuf buf) {
        boolean newOwner = buf.readBoolean();
        int sd = buf.readVarInt();
        List<Location> newLocations = new ArrayList<>();
        List<AccessUser> newUsers = new ArrayList<>();
        boolean newBlockInboundTeleports = buf.readBoolean();
        int lc = buf.readVarInt();
        for (int i = 0; i < lc; i++) {
            newLocations.add(new Location(buf.readString(32), buf.readString(128), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        int uc = buf.readVarInt();
        for (int i = 0; i < uc; i++) {
            newUsers.add(new AccessUser(buf.readUuid(), buf.readString(64)));
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof VortexManipulatorScreen screen) {
            boolean dataChanged = screen.owner != newOwner || screen.blockInboundTeleports != newBlockInboundTeleports || !LOCATIONS.equals(newLocations) || !USERS.equals(newUsers);
            int oldSd = screen.selfDestructTicks;
            LOCATIONS.clear();
            LOCATIONS.addAll(newLocations);
            USERS.clear();
            USERS.addAll(newUsers);
            screen.owner = newOwner;
            screen.selfDestructTicks = sd;
            screen.blockInboundTeleports = newBlockInboundTeleports;

            if (dataChanged || ((oldSd == 0) != (sd == 0))) {
                screen.rebuild();
            } else if (screen.selfDestructStatus != null && sd > 0) {
                screen.selfDestructStatus.setMessage(Text.literal("SELF-DESTRUCT ARMED — " + ((sd + 19) / 20) + "s   [CANCEL]"));
            }
        }
    }

    public void setDimensionValue(String value) {
        if (dimension != null) dimension.setText(value);
    }

    private void teleport() {
        if (client == null || client.player == null) return;
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
                client.player.sendMessage(Text.literal("Invalid destination. Check dimension and coordinates."), true);
                return;
            }
            p.writeIdentifier(id);
            p.writeDouble(tx); p.writeDouble(ty); p.writeDouble(tz);
            p.writeBoolean(surfaceMode);
        }
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
        client.setScreen(null);
    }

    private void saveLocation() {
        if (locationName == null) return;
        String name = locationName.getText().trim();
        if (!name.isEmpty()) sendString("SAVE", name);
    }

    private void deleteLocation() {
        if (locationName == null) return;
        String name = locationName.getText().trim();
        if (!name.isEmpty()) sendString("DELETE", name);
    }

    private void goLocation(String name) {
        sendString("GO", name);
        client.setScreen(null);
    }

    private void addPlayer() {
        String name = accessPlayer == null ? "" : accessPlayer.getText().trim();
        if (!name.isEmpty()) sendString("ADD_PLAYER", name);
    }

    private void removePlayer(String name) { sendString("REMOVE_PLAYER", name); }

    private void toggleInboundTeleports() {
        if (!owner) return;
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString("SET_INBOUND_TELEPORTS");
        p.writeBoolean(!blockInboundTeleports);
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }


    private void openSelfDestructConfirmation() {
        client.setScreen(new VortexSelfDestructConfirmScreen(this));
    }

    private void sendCommand(String action) {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString(action);
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }

    private void sendString(String action, String value) {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString(action);
        p.writeString(value, 128);
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
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
        if (squashed.equals("lostreality")) return new Identifier("gallifrey", "lost_reality");
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

    private String shortDimension(String d) {
        int i = d.indexOf(':');
        return i >= 0 ? d.substring(i + 1) : d;
    }

    private String fit(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String ellipsis = "…";
        int end = value.length();
        while (end > 0 && textRenderer.getWidth(value.substring(0, end) + ellipsis) > maxWidth) end--;
        return end <= 0 ? ellipsis : value.substring(0, end) + ellipsis;
    }

    private String fmt(double d) {
        if (d == Math.rint(d)) return Long.toString((long) d);
        return String.format(java.util.Locale.ROOT, "%.1f", d);
    }

    private float vortexGuiScale() {
        double guiScale = client.getWindow().getScaleFactor();
        float desired = (float) (3.5D / guiScale);
        float fit = Math.min((width - 20.0F) / W, (height - 20.0F) / H);
        return Math.min(desired, fit);
    }

    private int scaledMouseX(double mouseX) {
        float s = vortexGuiScale();
        return Math.round((float) (width * 0.5D + (mouseX - width * 0.5D) / s));
    }

    private int scaledMouseY(double mouseY) {
        float s = vortexGuiScale();
        return Math.round((float) (height * 0.5D + (mouseY - height * 0.5D) / s));
    }

    @Override public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x99000000);
        float s = vortexGuiScale();
        c.getMatrices().push();
        c.getMatrices().translate(width * 0.5F, height * 0.5F, 0.0F);
        c.getMatrices().scale(s, s, 1.0F);
        c.getMatrices().translate(-width * 0.5F, -height * 0.5F, 0.0F);
        drawPanel(c);
        for (String raw : pendingLabels) {
            String[] parts = raw.split("\\n", 3);
            c.drawText(textRenderer, parts[2], Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), DIM, false);
        }
        super.render(c, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        if (drawHintText != null && !drawHintText.isEmpty()) {
            c.drawText(textRenderer, Text.literal(fit(drawHintText, W - 36)), left + 18, top + (tab == 0 ? 254 : 116), DIM, false);
        }
        c.getMatrices().pop();
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float s = vortexGuiScale();
        return super.mouseDragged(scaledMouseX(mouseX), scaledMouseY(mouseY), button, deltaX / s, deltaY / s);
    }

    @Override public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(scaledMouseX(mouseX), scaledMouseY(mouseY));
    }

    private void drawPanel(DrawContext c) {
        c.fill(0, 0, width, height, 0x99000000);
        c.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0x553A2612);
        c.fill(left, top, left + W, top + H, PANEL);
        c.drawBorder(left, top, W, H, GOLD);
        c.drawBorder(left + 4, top + 4, W - 8, H - 8, GOLD_DARK);
        c.fill(left + 10, top + 10, left + W - 10, top + 32, PANEL_LIGHT);
        c.fill(left + 10, top + 31, left + W - 10, top + 32, GOLD_DIM);
        c.drawText(textRenderer, "VORTEX MANIPULATOR", left + 18, top + 17, GOLD, false);

    }

    private class ThemedButtonWidget extends ButtonWidget {
        ThemedButtonWidget(int x, int y, int w, int h, Text message, PressAction action) {
            super(x, y, w, h, message, action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override public void renderButton(DrawContext c, int mx, int my, float delta) {
            int bg = isHovered() ? 0xFF332512 : PANEL_DARK;
            c.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            c.drawBorder(getX(), getY(), getWidth(), getY() + getHeight() - getY(), active ? GOLD_DIM : SILVER_DIM);
            Text visible = Text.literal(fit(getMessage().getString(), Math.max(12, getWidth() - 12)));
            c.drawCenteredTextWithShadow(textRenderer, visible, getX() + getWidth() / 2,
                    getY() + getHeight() / 2 - 4, active ? TEXT : DIM);
        }
    }

    private record Location(String name, String dimension, double x, double y, double z) {}
    private record AccessUser(UUID uuid, String name) {}
}
