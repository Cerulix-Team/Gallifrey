package com.timelordmod.gallifrey.screens;

import com.timelordmod.gallifrey.block.entity.SonicWorkshopBlockEntity;
import com.timelordmod.gallifrey.item.GallifreyModItems;
import com.timelordmod.gallifrey.item.custom.SonicScrewdriver;
import com.timelordmod.gallifrey.networking.SonicCasingClientNetworking;
import com.timelordmod.gallifrey.sonic.SonicCasing;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SonicWorkshopScreen extends Screen {

    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 270;

    // TARDIS colour palette
    private static final int PANEL       = 0xF007141C;
    private static final int PANEL_LIGHT = 0xFF102D38;
    private static final int PANEL_DARK  = 0xFF091D27;

    private static final int BLUE      = 0xFF35C5E8;
    private static final int BLUE_DIM  = 0xFF168AAD;
    private static final int BLUE_DARK = 0xFF0A4F66;

    private static final int TEXT     = 0xFFE8F1ED;
    private static final int TEXT_DIM = 0xFF7599A3;

    private static final int GOLD_BRIGHT = 0xFFF2D27A;
    private static final int GOLD_DARK   = 0xFF735725;

    // Casing selection layout
    private static final int COLUMNS = 4;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 30;
    private static final int SPACING_X = 6;
    private static final int SPACING_Y = 7;

    // Four columns and four rows per page.
    private static final int ROWS_PER_PAGE = 4;
    private static final int CASINGS_PER_PAGE =
            COLUMNS * ROWS_PER_PAGE;

    private static final int GRID_TOP = 108;
    private static final int GRID_BOTTOM = 238;

    private int left;
    private int top;

    private int casingPage = 0;

    private SonicCasing selectedCasing;
    private final BlockPos pos;

    private boolean sawSonic;

    public SonicWorkshopScreen(BlockPos pos) {
        super(Text.literal("Sonic Workshop"));

        this.pos = pos;
        this.selectedCasing = SonicCasing.THIRD_DOCTOR;
    }

    /**
     * Returns the workshop if it still exists and contains a Sonic.
     */
    private SonicWorkshopBlockEntity workshop() {
        if (client != null && client.world != null
                && client.world.getBlockEntity(pos)
                instanceof SonicWorkshopBlockEntity workshop
                && workshop.hasSonic()) {
            return workshop;
        }

        return null;
    }

    @Override
    protected void init() {
        left = (width - GUI_WIDTH) / 2;
        top = (height - GUI_HEIGHT) / 2;

        SonicWorkshopBlockEntity workshop = workshop();

        if (workshop != null) {
            sawSonic = true;
            selectedCasing =
                    SonicScrewdriver.getCasing(workshop.getSonic());
        }

        int totalPages = getTotalPages();

        casingPage = Math.max(
                0,
                Math.min(casingPage, totalPages - 1)
        );

        createCasingButtons();
        createNavigationButtons();
    }

    /**
     * Calculates the number of pages required.
     */
    private int getTotalPages() {
        int count = SonicCasing.values().length;

        return Math.max(
                1,
                (count + CASINGS_PER_PAGE - 1)
                        / CASINGS_PER_PAGE
        );
    }

    /**
     * Creates only the buttons belonging to the current page.
     */
    private void createCasingButtons() {
        SonicCasing[] casings = SonicCasing.values();

        int gridWidth =
                COLUMNS * BUTTON_WIDTH
                        + (COLUMNS - 1) * SPACING_X;

        int panelWidth = GUI_WIDTH - 36;

        int startX =
                left + 18 + (panelWidth - gridWidth) / 2;

        int startY = top + GRID_TOP;

        int startIndex =
                casingPage * CASINGS_PER_PAGE;

        int endIndex = Math.min(
                startIndex + CASINGS_PER_PAGE,
                casings.length
        );

        for (int i = startIndex; i < endIndex; i++) {
            SonicCasing casing = casings[i];

            int localIndex = i - startIndex;

            int column = localIndex % COLUMNS;
            int row = localIndex / COLUMNS;

            int buttonX =
                    startX + column
                            * (BUTTON_WIDTH + SPACING_X);

            int buttonY =
                    startY + row
                            * (BUTTON_HEIGHT + SPACING_Y);

            addDrawableChild(
                    new CasingButton(
                            buttonX,
                            buttonY,
                            BUTTON_WIDTH,
                            BUTTON_HEIGHT,
                            casing
                    )
            );
        }
    }

    /**
     * Adds Previous and Next buttons for navigating casing pages.
     */
    private void createNavigationButtons() {
        int totalPages = getTotalPages();

        int buttonY = top + 242;

        addDrawableChild(
                ButtonWidget.builder(
                                Text.literal("< PREVIOUS"),
                                button -> changePage(-1)
                        )
                        .dimensions(left + 20, buttonY, 100, 20)
                        .build()
        );

        addDrawableChild(
                ButtonWidget.builder(
                                Text.literal("NEXT >"),
                                button -> changePage(1)
                        )
                        .dimensions(
                                left + GUI_WIDTH - 120,
                                buttonY,
                                100,
                                20
                        )
                        .build()
        );
    }

    /**
     * Changes the current page and rebuilds the selection buttons.
     */
    private void changePage(int direction) {
        int newPage = casingPage + direction;

        if (newPage < 0 || newPage >= getTotalPages()) {
            return;
        }

        casingPage = newPage;
        clearAndInit();
    }

    /**
     * Mouse-wheel navigation over the casing panel.
     *
     * Negative wheel movement goes forward.
     * Positive wheel movement goes backward.
     */
    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double amount
    ) {
        int panelLeft = left + 18;
        int panelRight = left + GUI_WIDTH - 18;
        int panelTop = top + 102;
        int panelBottom = top + GRID_BOTTOM;

        if (mouseX < panelLeft
                || mouseX >= panelRight
                || mouseY < panelTop
                || mouseY >= panelBottom) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    amount
            );
        }

        if (amount < 0) {
            changePage(1);
        } else if (amount > 0) {
            changePage(-1);
        }

        return true;
    }

    /**
     * Individual Sonic casing selection button.
     */
    private class CasingButton extends ButtonWidget {

        private final SonicCasing casing;
        private final ItemStack previewStack;

        public CasingButton(
                int x,
                int y,
                int width,
                int height,
                SonicCasing casing
        ) {
            super(
                    x,
                    y,
                    width,
                    height,
                    Text.literal(casing.getDisplayName()),
                    button -> onCasingSelected(casing),
                    DEFAULT_NARRATION_SUPPLIER
            );

            this.casing = casing;

            this.previewStack =
                    new ItemStack(
                            GallifreyModItems.SONIC_SCREWDRIVER
                    );

            SonicScrewdriver.setCasing(
                    previewStack,
                    casing
            );
        }

        @Override
        public void renderButton(
                DrawContext context,
                int mouseX,
                int mouseY,
                float delta
        ) {
            boolean hovered =
                    mouseX >= getX()
                            && mouseX < getX() + getWidth()
                            && mouseY >= getY()
                            && mouseY < getY() + getHeight();

            boolean selected = selectedCasing == casing;

            int fill;
            int border;
            int textColor;

            if (selected) {
                fill = GOLD_DARK;
                border = GOLD_BRIGHT;
                textColor = GOLD_BRIGHT;
            } else if (hovered) {
                fill = PANEL_LIGHT;
                border = BLUE;
                textColor = TEXT;
            } else {
                fill = PANEL_DARK;
                border = BLUE_DIM;
                textColor = BLUE;
            }

            context.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    fill
            );

            context.drawBorder(
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    border
            );

            context.drawItem(
                    previewStack,
                    getX() + getWidth() / 2 - 8,
                    getY() + 2
            );

            context.drawCenteredTextWithShadow(
                    textRenderer,
                    getMessage(),
                    getX() + getWidth() / 2,
                    getY() + 21,
                    textColor
            );
        }
    }

    /**
     * Applies the selected casing through the existing networking system.
     */
    private void onCasingSelected(SonicCasing casing) {
        if (client == null || client.player == null) {
            return;
        }

        selectedCasing = casing;

        SonicCasingClientNetworking.sendCasingChange(
                pos,
                casing
        );

        client.player.sendMessage(
                Text.literal(
                        "§6CASING: §f"
                                + casing.getDisplayName()
                ),
                true
        );
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        drawBackground(context);

        super.render(
                context,
                mouseX,
                mouseY,
                delta
        );

        drawLabels(context);
        drawPageIndicator(context);
    }

    /**
     * Draws the TARDIS-style workshop background.
     */
    private void drawBackground(DrawContext context) {
        context.fill(
                0, 0, width, height,
                0x99000000
        );

        context.fill(
                left - 3,
                top - 3,
                left + GUI_WIDTH + 3,
                top + GUI_HEIGHT + 3,
                0x30168AAD
        );

        context.fill(
                left,
                top,
                left + GUI_WIDTH,
                top + GUI_HEIGHT,
                PANEL
        );

        context.drawBorder(
                left,
                top,
                GUI_WIDTH,
                GUI_HEIGHT,
                BLUE
        );

        context.drawBorder(
                left + 4,
                top + 4,
                GUI_WIDTH - 8,
                GUI_HEIGHT - 8,
                BLUE_DARK
        );

        context.fill(
                left + 10,
                top + 10,
                left + GUI_WIDTH - 10,
                top + 47,
                PANEL_LIGHT
        );

        context.fill(
                left + 10,
                top + 46,
                left + GUI_WIDTH - 10,
                top + 47,
                BLUE_DIM
        );

        drawSectionBox(
                context,
                left + 18,
                top + 57,
                GUI_WIDTH - 36,
                43
        );

        drawSectionBox(
                context,
                left + 18,
                top + 102,
                GUI_WIDTH - 36,
                151
        );

        context.fill(
                left + 18,
                top + 255,
                left + GUI_WIDTH - 18,
                top + 256,
                BLUE_DARK
        );
    }

    private void drawSectionBox(
            DrawContext context,
            int x,
            int y,
            int width,
            int height
    ) {
        context.fill(
                x,
                y,
                x + width,
                y + height,
                PANEL_DARK
        );

        context.fill(
                x,
                y,
                x + width,
                y + 1,
                BLUE_DARK
        );

        context.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                PANEL_LIGHT
        );
    }

    /**
     * Draws the header and installed Sonic information.
     */
    private void drawLabels(DrawContext context) {
        context.drawText(
                textRenderer,
                "SONIC WORKSHOP",
                left + 20,
                top + 17,
                GOLD_BRIGHT,
                false
        );

        context.drawText(
                textRenderer,
                "SONIC CASING CONFIGURATION SYSTEM",
                left + 20,
                top + 31,
                TEXT_DIM,
                false
        );

        SonicWorkshopBlockEntity workshop = workshop();

        if (workshop != null) {
            context.drawItem(
                    workshop.getSonic(),
                    left + 26,
                    top + 67
            );
        }

        context.drawText(
                textRenderer,
                "SONIC SCREWDRIVER",
                left + 48,
                top + 64,
                BLUE,
                false
        );

        context.drawText(
                textRenderer,
                "SELECT CASING",
                left + 48,
                top + 79,
                TEXT_DIM,
                false
        );

        String currentLabel = "CURRENT:";
        String currentValue =
                selectedCasing.getDisplayName();

        int rightEdge = left + GUI_WIDTH - 27;

        int valueX =
                rightEdge - textRenderer.getWidth(currentValue);

        int labelX =
                valueX - 6 - textRenderer.getWidth(currentLabel);

        context.drawText(
                textRenderer,
                currentLabel,
                labelX,
                top + 78,
                TEXT_DIM,
                false
        );

        context.drawText(
                textRenderer,
                currentValue,
                valueX,
                top + 78,
                GOLD_BRIGHT,
                false
        );
    }

    /**
     * Shows which section of the casing list is currently displayed.
     */
    private void drawPageIndicator(DrawContext context) {
        String pageText =
                "PAGE " + (casingPage + 1)
                        + " / " + getTotalPages();

        context.drawCenteredTextWithShadow(
                textRenderer,
                pageText,
                left + GUI_WIDTH / 2,
                top + 248,
                GOLD_BRIGHT
        );
    }

    @Override
    public void tick() {
        super.tick();

        boolean blockGone =
                client == null
                        || client.world == null
                        || !(client.world.getBlockEntity(pos)
                        instanceof SonicWorkshopBlockEntity);

        SonicWorkshopBlockEntity workshop = workshop();

        if (workshop != null && !sawSonic) {
            sawSonic = true;

            selectedCasing =
                    SonicScrewdriver.getCasing(
                            workshop.getSonic()
                    );
        }

        if (blockGone || (sawSonic && workshop == null)) {
            close();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

