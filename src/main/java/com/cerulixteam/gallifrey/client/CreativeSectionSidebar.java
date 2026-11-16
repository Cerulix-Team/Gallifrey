package com.cerulixteam.gallifrey.client;

import com.cerulixteam.gallifrey.GallifreyMod;
import com.cerulixteam.gallifrey.item.GallifreyCreativeTab;
import com.cerulixteam.gallifrey.item.section.CreativeSection;
import com.cerulixteam.gallifrey.item.section.GallifreyTabSections;
import com.cerulixteam.gallifrey.mixin.client.CreativeInventoryScreenAccessor;
import com.cerulixteam.gallifrey.mixin.client.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public final class CreativeSectionSidebar {

    private static final Identifier TEXTURE = GallifreyMod.id("textures/gui/creative_sections.png");

    private static final int PANEL_WIDTH = 30;
    private static final int PANEL_EDGE = 6;       // top/bottom border height
    private static final int ROW = 18;             // one section slot
    private static final int SLOT_OFFSET_X = 6;    // slot position inside the panel

    private static final int U_PANEL = 0, V_PANEL_TOP = 0, V_PANEL_ROW = 6, V_PANEL_BOTTOM = 114;
    private static final int U_SLOT = 32, U_SLOT_SELECTED = 52, V_SLOT = 0;
    private static int selected = -1;
    /** Other mods' buttons hidden while the Gallifrey tab is open, so they can be shown again. */
    private static final java.util.Set<ClickableWidget> HIDDEN =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    private CreativeSectionSidebar() {}
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof CreativeInventoryScreen creative) {
                attach(creative);
            }
        });
    }

    private static void attach(CreativeInventoryScreen screen) {
        List<CreativeSection> sections = GallifreyTabSections.ALL;
        if (sections.isEmpty()) return;

        HandledScreenAccessor gui = (HandledScreenAccessor) screen;
        int panelHeight = PANEL_EDGE * 2 + ROW * sections.size();
        int panelX = gui.gallifrey$getX() - PANEL_WIDTH;
        int panelY = gui.gallifrey$getY() + Math.max(0, (gui.gallifrey$getBackgroundHeight() - panelHeight) / 2);

        SidebarWidget sidebar = new SidebarWidget(screen, panelX, panelY, panelHeight, sections);
        sidebar.visible = isGallifreyTab();
        Screens.getButtons(screen).add(sidebar);
        boolean[] wasOpen = { false };

        ScreenEvents.beforeRender(screen).register((s, context, mouseX, mouseY, delta) -> {
            boolean open = isGallifreyTab();
            sidebar.visible = open;
            hideCoveredButtons(screen, sidebar, open);

            if (open && selected >= 0) {
                boolean justOpened = !wasOpen[0];
                boolean listReplaced = screen.getScreenHandler().itemList.size() != sections.get(selected).getStacks().size();
                if (justOpened || listReplaced) {
                    showItems(screen, sections);
                }
            }
            wasOpen[0] = open;
        });

        ScreenEvents.remove(screen).register(s -> {
            selected = -1;
            HIDDEN.clear();
        });
    }

    /**
     * While the Gallifrey tab is open, hides buttons other mods put where the sidebar sits
     * (Axiom's two side buttons, for example) so they can't be seen or clicked through it.
     * They're shown again as soon as another tab is picked. Checked every frame, so it also
     * catches buttons a mod adds or moves later.
     */
    private static void hideCoveredButtons(CreativeInventoryScreen screen, SidebarWidget sidebar, boolean open) {
        if (!open) {
            for (ClickableWidget w : HIDDEN) w.visible = true;
            HIDDEN.clear();
            return;
        }
        for (ClickableWidget other : Screens.getButtons(screen)) {
            if (other == sidebar || !other.visible) continue;
            boolean overlapsX = other.getX() < sidebar.getX() + sidebar.getWidth() && other.getX() + other.getWidth() > sidebar.getX();
            boolean overlapsY = other.getY() < sidebar.getY() + sidebar.getHeight() && other.getY() + other.getHeight() > sidebar.getY();
            if (overlapsX && overlapsY) {
                other.visible = false;
                HIDDEN.add(other);
            }
        }
    }

    private static boolean isGallifreyTab() {
        ItemGroup tab = CreativeInventoryScreenAccessor.gallifrey$getSelectedTab();
        return tab == GallifreyCreativeTab.TAB;
    }

    private static void showItems(CreativeInventoryScreen screen, List<CreativeSection> sections) {
        CreativeInventoryScreen.CreativeScreenHandler handler = screen.getScreenHandler();
        handler.itemList.clear();

        if (selected >= 0 && selected < sections.size()) {
            handler.itemList.addAll(sections.get(selected).getStacks());
        } else {
            handler.itemList.addAll(GallifreyCreativeTab.TAB.getDisplayStacks());
        }

        ((CreativeInventoryScreenAccessor) screen).gallifrey$setScrollPosition(0.0F);
        handler.scrollItems(0.0F);
    }
    private static final class SidebarWidget extends ClickableWidget {
        private final CreativeInventoryScreen screen;
        private final List<CreativeSection> sections;

        SidebarWidget(CreativeInventoryScreen screen, int x, int y, int height, List<CreativeSection> sections) {
            super(x, y, PANEL_WIDTH, height, Text.translatable("itemGroup.gallifrey.gallifrey"));
            this.screen = screen;
            this.sections = sections;
        }

        private int slotX() {
            return this.getX() + SLOT_OFFSET_X;
        }

        private int slotY(int index) {
            return this.getY() + PANEL_EDGE + index * ROW;
        }
        private int sectionAt(double mouseX, double mouseY) {
            int sx = slotX();
            if (mouseX < sx || mouseX >= sx + ROW) return -1;
            for (int i = 0; i < this.sections.size(); i++) {
                int sy = slotY(i);
                if (mouseY >= sy && mouseY < sy + ROW) return i;
            }
            return -1;
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            int x = this.getX();
            int y = this.getY();
            context.drawTexture(TEXTURE, x, y, U_PANEL, V_PANEL_TOP, PANEL_WIDTH, PANEL_EDGE);
            for (int i = 0; i < this.sections.size(); i++) {
                context.drawTexture(TEXTURE, x, slotY(i), U_PANEL, V_PANEL_ROW, PANEL_WIDTH, ROW);
            }
            context.drawTexture(TEXTURE, x, slotY(this.sections.size()), U_PANEL, V_PANEL_BOTTOM, PANEL_WIDTH, PANEL_EDGE);

            int hovered = this.isHovered() ? sectionAt(mouseX, mouseY) : -1;

            for (int i = 0; i < this.sections.size(); i++) {
                int sx = slotX();
                int sy = slotY(i);
                boolean isSelected = i == selected;

                context.drawTexture(TEXTURE, sx, sy, isSelected ? U_SLOT_SELECTED : U_SLOT, V_SLOT, ROW, ROW);
                context.drawItem(this.sections.get(i).getIcon(), sx + 1, sy + 1);

                if (i == hovered) {
                    context.getMatrices().push();
                    context.getMatrices().translate(0.0F, 0.0F, 200.0F);
                    context.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0x80FFFFFF);
                    context.getMatrices().pop();
                    this.screen.setTooltip(List.of(this.sections.get(i).getTitle().asOrderedText()));
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.visible || !this.active) return false;
            if (mouseX < this.getX() || mouseX >= this.getX() + this.width
                    || mouseY < this.getY() || mouseY >= this.getY() + this.height) {
                return false;
            }

            int index = sectionAt(mouseX, mouseY);
            if (button == 0 && index >= 0) {
                selected = (selected == index) ? -1 : index;
                showItems(this.screen, this.sections);
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            }
            return true;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
