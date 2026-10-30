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
import java.util.UUID;

public abstract class VortexManipulatorSubScreen extends Screen {
    protected static final int W = 540, H = 360;
    protected static final int PANEL = 0xF0091118, PANEL_LIGHT = 0xFF101D25, PANEL_DARK = 0xFF080D12;
    protected static final int CYAN = 0xFF26E6FF, CYAN_DIM = 0xFF08758C, CYAN_DARK = 0xFF063D4A;
    protected static final int TEXT = 0xFFE7FBFF, DIM = 0xFF75AAB5, GREEN = 0xFF38FF88, RED = 0xFFFF4F6B;

    protected int accent() { return CYAN; }
    protected int accentDim() { return CYAN_DIM; }
    protected int accentDark() { return CYAN_DARK; }
    protected int panel() { return PANEL; }
    protected int panelLight() { return PANEL_LIGHT; }
    protected int panelDark() { return PANEL_DARK; }
    protected int textColor() { return TEXT; }
    protected int dimColor() { return DIM; }

    protected int left, top;
    protected boolean owner;
    protected int selfDestructTicks;
    protected boolean blockInboundTeleports;
    protected static final List<Location> LOCATIONS = new ArrayList<>();
    protected static final List<AccessUser> USERS = new ArrayList<>();

    protected VortexManipulatorSubScreen(String title) {
        super(Text.literal(title));
    }

    @Override protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        build();
        requestState();
    }

    protected abstract void build();

    protected void buildHeader(DrawContext c, String title) {
        c.fill(0, 0, width, height, 0x99000000);
        c.fill(left - 3, top - 3, left + W + 3, top + H + 3, (accentDark() & 0x00FFFFFF) | 0x55000000);
        c.fill(left, top, left + W, top + H, panel());
        c.drawBorder(left, top, W, H, accent());
        c.drawBorder(left + 4, top + 4, W - 8, H - 8, accentDark());
        c.fill(left + 10, top + 10, left + W - 10, top + 34, panelLight());
        c.fill(left + 10, top + 33, left + W - 10, top + 34, accentDim());
        c.drawText(textRenderer, "VORTEX MANIPULATOR", left + 20, top + 17, accent(), false);
        if (title != null && !title.isBlank()) {
            c.drawText(textRenderer, title, left + 24, top + 62, accent(), false);
        }
    }

    protected void buildBackButton() {
        addDrawableChild(btn(left + 24, top + 328, 110, 22, "◀ BACK", b -> MinecraftClient.getInstance().setScreen(new VortexManipulatorScreen())));
    }

    protected ButtonWidget btn(int x, int y, int w, int h, String text, ButtonWidget.PressAction action) {
        return new ThemedButtonWidget(x, y, w, h, Text.literal(text), action);
    }

    protected TextFieldWidget field(int x, int y, int w, String placeholder) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 24, Text.literal(placeholder));
        f.setMaxLength(128);
        f.setPlaceholder(Text.literal(placeholder));
        f.setEditableColor(textColor());
        f.setUneditableColor(dimColor());
        addDrawableChild(f);
        return f;
    }

    protected void requestState() {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString("REQUEST_STATE");
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }

    protected void sendString(String action, String value) {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString(action);
        p.writeString(value, 128);
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }

    protected void selfDestructCommand(String action) {
        PacketByteBuf p = PacketByteBufs.create();
        p.writeString(action);
        ClientPlayNetworking.send(ModPackets.VM_PACKET, p);
    }

    protected void drawBase(DrawContext c) {
        buildHeader(c, getTitle().getString().replace("Vortex Manipulator", "").trim());
    }

    /**
     * Render the Vortex GUI at a consistent physical size regardless of Minecraft's GUI scale.
     * The design is authored around GUI scale 3.5 and is reduced only when the screen is too
     * small to contain it (notably GUI scale 4 on a small display).
     */
    protected float vortexGuiScale() {
        double guiScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        float desired = (float) (3.5D / guiScale);
        float fit = Math.min((width - 20.0F) / W, (height - 20.0F) / H);
        return Math.min(desired, fit);
    }

    protected int scaledMouseX(double mouseX) {
        float s = vortexGuiScale();
        return Math.round((float) (width * 0.5D + (mouseX - width * 0.5D) / s));
    }

    protected int scaledMouseY(double mouseY) {
        float s = vortexGuiScale();
        return Math.round((float) (height * 0.5D + (mouseY - height * 0.5D) / s));
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x99000000);
        float s = vortexGuiScale();
        context.getMatrices().push();
        context.getMatrices().translate(width * 0.5F, height * 0.5F, 0.0F);
        context.getMatrices().scale(s, s, 1.0F);
        context.getMatrices().translate(-width * 0.5F, -height * 0.5F, 0.0F);
        renderVortexChrome(context, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        super.render(context, scaledMouseX(mouseX), scaledMouseY(mouseY), delta);
        context.getMatrices().pop();
    }

    /** Screen-specific decoration/text drawn behind the widgets. */
    protected void renderVortexChrome(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBase(context);
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

    protected String shortDimension(String d) {
        int i = d.indexOf(':');
        return i >= 0 ? d.substring(i + 1) : d;
    }

    public static void applyServerState(PacketByteBuf buf) {
        boolean newOwner = buf.readBoolean();
        int sd = buf.readVarInt();
        boolean newBlockInboundTeleports = buf.readBoolean();
        LOCATIONS.clear();
        USERS.clear();
        int lc = buf.readVarInt();
        for (int i = 0; i < lc; i++) {
            LOCATIONS.add(new Location(buf.readString(32), buf.readString(128),
                    buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        int uc = buf.readVarInt();
        for (int i = 0; i < uc; i++) {
            USERS.add(new AccessUser(buf.readUuid(), buf.readString(64)));
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof VortexManipulatorSubScreen screen) {
            screen.owner = newOwner;
            screen.selfDestructTicks = sd;
            screen.blockInboundTeleports = newBlockInboundTeleports;
            screen.clearAndRebuild();
        }
    }

    protected void clearAndRebuild() {
        clearChildren();
        build();
    }

    protected class ThemedButtonWidget extends ButtonWidget {
        ThemedButtonWidget(int x, int y, int w, int h, Text message, PressAction action) {
            super(x, y, w, h, message, action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override public void renderButton(DrawContext c, int mx, int my, float delta) {
            int bg = isHovered() ? 0xFF123744 : PANEL_DARK;
            c.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            c.drawBorder(getX(), getY(), getWidth(), getHeight(), active ? CYAN_DIM : 0xFF30434A);
            c.drawCenteredTextWithShadow(textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + getHeight() / 2 - 4,
                    active ? TEXT : DIM);
        }
    }

    protected record Location(String name, String dimension, double x, double y, double z) {}
    protected record AccessUser(UUID uuid, String name) {}
}
