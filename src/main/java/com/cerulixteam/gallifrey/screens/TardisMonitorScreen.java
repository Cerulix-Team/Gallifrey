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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TARDIS console UI, deliberately laid out like the Vortex Manipulator:
 * compact navigation tabs, dense isomorphic controls, clear status blocks,
 * and no giant Minecraft-style inventory window.
 */
public class TardisMonitorScreen extends Screen {
    private static final int W = 620, H = 390;
    private static final int PANEL = 0xF0091117, PANEL_LIGHT = 0xFF101D25, PANEL_DARK = 0xFF060C10;
    private static final int BLUE = 0xFF63DDF2, BLUE_DIM = 0xFF276B7A;
    private static final int AMBER = 0xFFFFC857, AMBER_DIM = 0xFF9A6A1F;
    private static final int TEXT = 0xFFE8F8FA, DIM = 0xFF86AAB4, GREEN = 0xFF5FE39A, RED = 0xFFFF6675;
    private static final int SILVER = 0xFFD2D9DC;

    private int left, top;
    private int tab = 0;
    private int fuel, maxFuel;
    private String exteriorStyle = "policebox";
    private String exteriorStyleName = "Police Box";
    private List<String> exteriorStyles = new ArrayList<>();
    private String interior = "tardis_platform";
    private String selectedInterior = "tardis_platform";
    private List<String> interiors = new ArrayList<>();
    private boolean flightPending, locked, antigravity = true, powered = true;
    private int selfDestructTicks;
    private String exteriorDimension = "minecraft:overworld";
    private String interiorDimension = "gallifrey:tardis";
    private double exteriorX, exteriorY, exteriorZ;

    private TextFieldWidget dimension, x, y, z;
    private ButtonWidget flightButton;
    private int interiorPage;

    public TardisMonitorScreen() { super(Text.literal("TARDIS Console")); }

    private float guiScale() {
        double windowScale = client == null ? 1.0D : client.getWindow().getScaleFactor();
        float desired = (float) (3.25D / Math.max(1.0D, windowScale));
        float fit = Math.min((width - 20.0F) / W, (height - 20.0F) / H);
        return Math.min(desired, fit);
    }

    private int scaledMouseX(double mouseX) {
        float s = guiScale();
        return Math.round((float) (width * 0.5D + (mouseX - width * 0.5D) / s));
    }

    private int scaledMouseY(double mouseY) {
        float s = guiScale();
        return Math.round((float) (height * 0.5D + (mouseY - height * 0.5D) / s));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        rebuild();
        requestState();
    }

    private void rebuild() {
        clearChildren();
        if (tab == 0) buildNavigation();
        else if (tab == 1) buildExterior();
        else if (tab == 2) buildInterior();
        else buildSystems();
    }

    private void buildNavigation() {
        dimension = field(left + 22, top + 108, 390, "minecraft:overworld");
        addDrawableChild(button(left + 420, top + 108, 178, 24, "DIMENSION DIRECTORY", b -> client.setScreen(new TardisDimensionsScreen(this))));
        x = field(left + 22, top + 157, 118, "X");
        y = field(left + 150, top + 157, 118, "Y");
        z = field(left + 278, top + 157, 118, "Z");
        flightButton = button(left + 410, top + 157, 188, 24, "SET DESTINATION", b -> sendFlight());

        addDrawableChild(button(left + 22, top + 205, 186, 28, "CURRENT LOCATION", b -> loadCurrentLocation()));
        addDrawableChild(button(left + 216, top + 205, 186, 28, "VORTEX: RETURN", b -> sendFlight()));
        addDrawableChild(button(left + 410, top + 205, 188, 28, "CANCEL / CLOSE", b -> close()));
    }

    private void buildExterior() {
        int count = exteriorStyles.isEmpty() ? 9 : exteriorStyles.size();
        for (int i = 0; i < count; i++) {
            int row = i / 3, col = i % 3;
            int bx = left + 18 + col * 196;
            int by = top + 102 + row * 42;
            String style = exteriorStyles.isEmpty() ? fallbackStyle(i) : exteriorStyles.get(i);
            String name = pretty(style);
            String marker = style.equals(exteriorStyle) ? "● " : "○ ";
            addDrawableChild(button(bx, by, 186, 34, marker + name,
                    b -> selectExterior(style)));
        }
        addDrawableChild(button(left + 18, top + 236, 576, 30,
                "ACTIVE SHELL: " + exteriorStyleName.toUpperCase(Locale.ROOT), b -> {}));
    }

    private void buildInterior() {
        int start = interiorPage * 6;
        for (int i = 0; i < 6; i++) {
            int index = start + i;
            if (index >= interiors.size()) break;
            String name = interiors.get(index);
            int row = i / 2, col = i % 2;
            int bx = left + 22 + col * 288;
            int by = top + 100 + row * 45;
            String marker = name.equals(selectedInterior) ? "● " : (name.equals(interior) ? "◆ " : "○ ");
            addDrawableChild(button(bx, by, 272, 34, marker + pretty(name), b -> { selectedInterior = name; rebuild(); }));
        }
        addDrawableChild(button(left + 22, top + 250, 120, 26, "◀ PREV", b -> { if (interiorPage > 0) { interiorPage--; rebuild(); } }));
        addDrawableChild(button(left + 146, top + 250, 328, 26, "SELECTED: " + pretty(selectedInterior), b -> {}));
        addDrawableChild(button(left + 478, top + 250, 104, 26, "NEXT ▶", b -> {
            if ((interiorPage + 1) * 6 < interiors.size()) { interiorPage++; rebuild(); }
        }));
        addDrawableChild(button(left + 22, top + 284, 560, 28, "INSTALL SELECTED INTERIOR  •  EJECTS ALL CREW", b -> applyInterior(selectedInterior)));
    }

    private void buildSystems() {
        addDrawableChild(button(left + 22, top + 108, 272, 30, "REFUEL ARTRON RESERVES", b -> sendAction("REFUEL")));
        addDrawableChild(button(left + 304, top + 108, 294, 30, locked ? "SECURITY: LOCKED" : "SECURITY: OPEN", b -> sendAction(locked ? "UNLOCK" : "LOCK")));
        addDrawableChild(button(left + 22, top + 154, 272, 30, "MUSIC: PLAY DR WHO VALE", b -> sendAction("PLAY_DRWHO_VALE")));
        addDrawableChild(button(left + 304, top + 154, 294, 30, "REQUEST SYSTEM STATUS", b -> requestState()));
        addDrawableChild(button(left + 22, top + 200, 272, 30, antigravity ? "ANTIGRAVITY: ON" : "ANTIGRAVITY: OFF", b -> sendAction("ANTIGRAV")));
        String destruct = selfDestructTicks > 0 ? "SELF-DESTRUCT: " + ((selfDestructTicks + 19) / 20) + "s" : "SELF-DESTRUCT: ARM (10s)";
        addDrawableChild(button(left + 304, top + 200, 294, 30, destruct, b -> sendAction(selfDestructTicks > 0 ? "CANCEL_SELF_DESTRUCT" : "SELF_DESTRUCT")));
        addDrawableChild(button(left + 22, top + 246, 576, 30, "RETURN TO NAVIGATION", b -> switchTab(0)));
    }

    private TextFieldWidget field(int px, int py, int w, String placeholder) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, px, py, w, 24, Text.literal(placeholder));
        f.setMaxLength(128);
        f.setPlaceholder(Text.literal(placeholder));
        f.setEditableColor(TEXT);
        addDrawableChild(f);
        return f;
    }

    private ButtonWidget button(int x, int y, int w, int h, String label, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(Text.literal(label), action).dimensions(x, y, w, h).build();
    }

    public void setDimensionValue(String id) {
        if (dimension != null) dimension.setText(id);
    }

    private void requestState() { sendAction("REQUEST_STATE"); }

    private void sendFlight() {
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString("FLIGHT");
            buf.writeString(dimension.getText().trim(), 128);
            buf.writeDouble(Double.parseDouble(x.getText().trim()));
            buf.writeDouble(Double.parseDouble(y.getText().trim()));
            buf.writeDouble(Double.parseDouble(z.getText().trim()));
            ClientPlayNetworking.send(ModPackets.TARDIS_MONITOR_ACTION, buf);
        } catch (Exception ignored) {
            if (client != null && client.player != null) client.player.sendMessage(Text.literal("Enter valid destination coordinates."), true);
        }
    }

    private void sendAction(String action) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action);
        ClientPlayNetworking.send(ModPackets.TARDIS_MONITOR_ACTION, buf);
    }

    private void applyInterior(String name) {
        if (name == null || name.isEmpty()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("INTERIOR");
        buf.writeString(name, 64);
        ClientPlayNetworking.send(ModPackets.TARDIS_MONITOR_ACTION, buf);
    }

    private void selectExterior(String style) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("EXTERIOR_STYLE");
        buf.writeString(style, 64);
        ClientPlayNetworking.send(ModPackets.TARDIS_MONITOR_ACTION, buf);
        exteriorStyle = style;
        exteriorStyleName = pretty(style);
        rebuild();
    }

    private void loadCurrentLocation() {
        setField(dimension, exteriorDimension);
        setField(x, fmt(exteriorX));
        setField(y, fmt(exteriorY));
        setField(z, fmt(exteriorZ));
    }

    private void setCurrentExteriorAsDestination() { loadCurrentLocation(); }

    private void setField(TextFieldWidget field, String value) {
        if (field != null) field.setText(value);
    }

    private void switchTab(int next) {
        tab = next;
        rebuild();
    }

    private String fallbackStyle(int i) {
        return switch (i) {
            case 1 -> "policebox_alt";
            case 2 -> "policebox_alt2";
            case 3 -> "policebox_badwolf";
            case 4 -> "policebox_coral";
            case 5 -> "policebox_dino";
            case 6 -> "policebox_purple";
            case 7 -> "policebox_tokomak";
            case 8 -> "gamblebox";
            default -> "policebox";
        };
    }

    private String pretty(String name) {
        if (name == null) return "Unknown";
        String[] words = name.replace('_', ' ').split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private String fmt(double d) {
        return d == Math.rint(d) ? Long.toString((long) d) : String.format(Locale.ROOT, "%.2f", d);
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        c.fill(0, 0, width, height, 0x99000000);
        float s = guiScale();
        c.getMatrices().push();
        c.getMatrices().translate(width * 0.5F, height * 0.5F, 0.0F);
        c.getMatrices().scale(s, s, 1.0F);
        c.getMatrices().translate(-width * 0.5F, -height * 0.5F, 0.0F);
        left = (width - W) / 2;
        top = (height - H) / 2;
        c.fill(left - 3, top - 3, left + W + 3, top + H + 3, 0x553A2612);
        c.fill(left, top, left + W, top + H, PANEL);
        c.drawBorder(left, top, W, H, SILVER);
        c.drawBorder(left + 4, top + 4, W - 8, H - 8, BLUE_DIM);
        c.fill(left + 10, top + 10, left + W - 10, top + 38, PANEL_LIGHT);
        c.fill(left + 10, top + 37, left + W - 10, top + 38, AMBER_DIM);

        c.drawText(textRenderer, "TARDIS CONTROL MATRIX", left + 20, top + 19, BLUE, false);
        c.drawText(textRenderer, flightPending ? "FLIGHT STATE: IN TRANSIT" : "FLIGHT STATE: READY", left + 405, top + 19, flightPending ? AMBER : GREEN, false);

        String[] tabs = {"NAVIGATION", "EXTERIOR", "INTERIOR", "SYSTEMS"};
        for (int i = 0; i < tabs.length; i++) {
            int bx = left + 18 + i * 149;
            c.fill(bx, top + 49, bx + 140, top + 76, i == tab ? 0xFF163542 : PANEL_DARK);
            c.drawBorder(bx, top + 49, 140, 27, i == tab ? BLUE : BLUE_DIM);
            c.drawCenteredTextWithShadow(textRenderer, Text.literal(tabs[i]), bx + 70, top + 58, i == tab ? TEXT : DIM);
        }

        // Tab buttons are drawn as actual widgets below; these small chrome buttons are
        // handled as mouse hit regions in mouseClicked so the layout stays clean.
        c.drawText(textRenderer, Text.literal("ARTRON"), left + 22, top + 344, DIM, false);
        int barW = 230;
        int filled = maxFuel <= 0 ? 0 : (int)(barW * Math.max(0, Math.min(1, fuel / (double) maxFuel)));
        c.fill(left + 84, top + 342, left + 84 + barW, top + 353, PANEL_DARK);
        c.fill(left + 84, top + 342, left + 84 + filled, top + 353, GREEN);
        c.drawText(textRenderer, Text.literal(fuel + " / " + maxFuel), left + 322, top + 344, TEXT, false);
        c.drawText(textRenderer, Text.literal("SHELL: " + exteriorStyleName), left + 405, top + 344, AMBER, false);
        c.drawText(textRenderer, Text.literal(powered ? "POWER: ONLINE" : "POWER: OFFLINE"), left + 405, top + 325, powered ? GREEN : RED, false);

        if (tab == 0) {
            c.drawText(textRenderer, Text.literal("DESTINATION"), left + 22, top + 94, DIM, false);
            c.drawText(textRenderer, Text.literal("X / Y / Z"), left + 22, top + 143, DIM, false);
            c.drawText(textRenderer, Text.literal("CURRENT EXTERIOR"), left + 22, top + 243, DIM, false);
            c.drawText(textRenderer, Text.literal(exteriorDimension + "  @  " + fmt(exteriorX) + ", " + fmt(exteriorY) + ", " + fmt(exteriorZ)), left + 22, top + 259, TEXT, false);
            c.drawText(textRenderer, Text.literal("POCKET DIMENSION"), left + 22, top + 276, DIM, false);
            c.drawText(textRenderer, Text.literal(interiorDimension), left + 22, top + 292, TEXT, false);
        } else if (tab == 1) {
            c.drawText(textRenderer, Text.literal("ISOMORPHIC SHELL CONTROL"), left + 22, top + 94, DIM, false);
            c.drawText(textRenderer, Text.literal("Select a supplied shell texture; the matching emission map is used automatically."), left + 22, top + 310, DIM, false);
        } else if (tab == 2) {
            c.drawText(textRenderer, Text.literal("INTERIOR ARCHITECTURE"), left + 22, top + 84, DIM, false);
            c.drawText(textRenderer, Text.literal("Installing an interior ejects everyone first, then rebuilds the room."), left + 22, top + 323, DIM, false);
        } else {
            c.drawText(textRenderer, Text.literal("SYSTEMS / ISOMORPHIC CONTROLS"), left + 22, top + 94, DIM, false);
            c.drawText(textRenderer, Text.literal(locked ? "Security is locked to the owner." : "Security is open."), left + 22, top + 250, locked ? AMBER : GREEN, false);
        }

        if (flightButton != null) flightButton.active = !flightPending && powered;
        super.render(c, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        c.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float sx = scaledMouseX(mouseX);
            float sy = scaledMouseY(mouseY);
            if (sy >= top + 49 && sy <= top + 76 && sx >= left + 18 && sx < left + 18 + 4 * 149) {
                int selected = (int) ((sx - (left + 18)) / 149);
                if (selected >= 0 && selected < 4) { switchTab(selected); return true; }
            }
        }
        return super.mouseClicked(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(scaledMouseX(mouseX), scaledMouseY(mouseY), button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float s = guiScale();
        return super.mouseDragged(scaledMouseX(mouseX), scaledMouseY(mouseY), button, deltaX / s, deltaY / s);
    }

    @Override public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(scaledMouseX(mouseX), scaledMouseY(mouseY));
    }

    @Override public boolean shouldPause() { return false; }

    public static void applyServerState(PacketByteBuf buf) {
        boolean valid = buf.readBoolean();
        if (!valid) return;
        int fuel = buf.readInt();
        int maxFuel = buf.readInt();
        String style = buf.readString(64);
        String styleName = buf.readString(64);
        int styleCount = buf.readVarInt();
        List<String> styles = new ArrayList<>();
        for (int i = 0; i < styleCount; i++) styles.add(buf.readString(64));
        String interior = buf.readString(64);
        int count = buf.readVarInt();
        List<String> interiors = new ArrayList<>();
        for (int i = 0; i < count; i++) interiors.add(buf.readString(64));
        String dim = buf.readString(128);
        String pocket = buf.readString(128);
        double x = buf.readDouble(), y = buf.readDouble(), z = buf.readDouble();
        boolean flight = buf.readBoolean();
        boolean locked = buf.readBoolean();
        boolean antigrav = buf.readBoolean();
        boolean powered = buf.readBoolean();
        int selfDestructTicks = buf.readInt();

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            TardisMonitorScreen screen = client.currentScreen instanceof TardisMonitorScreen existing
                    ? existing : new TardisMonitorScreen();
            if (client.currentScreen != screen) client.setScreen(screen);
            screen.fuel = fuel;
            screen.maxFuel = maxFuel;
            screen.exteriorStyle = style;
            screen.exteriorStyleName = styleName;
            screen.exteriorStyles = styles;
            screen.interior = interior;
            screen.interiors = interiors;
            if (!interiors.contains(screen.selectedInterior)) screen.selectedInterior = interior;
            screen.exteriorDimension = dim;
            screen.interiorDimension = pocket;
            screen.exteriorX = x;
            screen.exteriorY = y;
            screen.exteriorZ = z;
            screen.flightPending = flight;
            screen.locked = locked;
            screen.antigravity = antigrav;
            screen.powered = powered;
            screen.selfDestructTicks = selfDestructTicks;
            if (screen.dimension != null && !screen.dimension.isFocused()) screen.dimension.setText(dim);
            if (screen.x != null && !screen.x.isFocused()) screen.x.setText(screen.fmt(x));
            if (screen.y != null && !screen.y.isFocused()) screen.y.setText(screen.fmt(y));
            if (screen.z != null && !screen.z.isFocused()) screen.z.setText(screen.fmt(z));
            if (flight && screen.tab != 0) screen.tab = 0;
            screen.rebuild();
        });
    }
}
