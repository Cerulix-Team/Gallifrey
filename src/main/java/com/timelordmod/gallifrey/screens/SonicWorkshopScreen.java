package com.timelordmod.gallifrey.screens;

import com.timelordmod.gallifrey.item.GallifreyModItems;
import com.timelordmod.gallifrey.item.custom.SonicScrewdriver;
import com.timelordmod.gallifrey.networking.SonicCasingClientNetworking;
import com.timelordmod.gallifrey.sonic.SonicCasing;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.timelordmod.gallifrey.block.entity.SonicWorkshopBlockEntity;

public class SonicWorkshopScreen extends Screen {

    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 270;

    /*
     * ============================================================
     * DOCTOR WHO / TARDIS COLOUR PALETTE
     * ============================================================
     */

    // Main TARDIS / panel colours
    private static final int PANEL       = 0xF007141C;
    private static final int PANEL_LIGHT = 0xFF102D38;
    private static final int PANEL_DARK  = 0xFF091D27;

    // TARDIS blues
    private static final int BLUE      = 0xFF35C5E8;
    private static final int BLUE_DIM  = 0xFF168AAD;
    private static final int BLUE_DARK = 0xFF0A4F66;

    // Text
    private static final int TEXT     = 0xFFE8F1ED;
    private static final int TEXT_DIM = 0xFF7599A3;

    // Time Lord / brass accent
    private static final int GOLD        = 0xFFD6A84F;
    private static final int GOLD_BRIGHT = 0xFFF2D27A;
    private static final int GOLD_DARK   = 0xFF735725;

    private int left;
    private int top;

    private SonicCasing selectedCasing;
    private final BlockPos pos;
    /** True once the client has seen the installed Sonic (see tick()). */
    private boolean sawSonic;
    private final List<CasingButton> casingButtons = new ArrayList<>();
    private int casingScrollRows = 0;
    private static final int CASING_VIEW_TOP = 104;
    private static final int CASING_VIEW_BOTTOM = 250;
    private static final int CASING_ROW_HEIGHT = 37;
    private static final int VISIBLE_CASING_ROWS = 4;

    public SonicWorkshopScreen(BlockPos pos) {
        super(Text.literal("Sonic Workshop"));
        this.pos = pos;
        this.selectedCasing = SonicCasing.THIRD_DOCTOR;
    }

    /** The workshop at pos, if it still exists and still holds a Sonic; otherwise null. */
    private SonicWorkshopBlockEntity workshop() {
        if (client != null && client.world != null
                && client.world.getBlockEntity(pos) instanceof SonicWorkshopBlockEntity workshop
                && workshop.hasSonic()) {
            return workshop;
        }
        return null;
    }

    @Override
    protected void init() {

        left = (width - GUI_WIDTH) / 2;
        top = (height - GUI_HEIGHT) / 2;

        // `client` is only set once the screen is shown, so read the installed casing here.
        SonicWorkshopBlockEntity workshop = workshop();
        if (workshop != null) {
            sawSonic = true;
            selectedCasing = SonicScrewdriver.getCasing(workshop.getSonic());
        }

        createCasingButtons();
    }

    private void createCasingButtons() {

        casingButtons.clear();
        SonicCasing[] casings = SonicCasing.values();

        /*
         * ========================================================
         * 4-COLUMN BUTTON GRID
         * ========================================================
         */

        int columns = 4;

        int buttonWidth = 90;
        int buttonHeight = 30;

        int spacingX = 6;
        int spacingY = 7;

        int gridWidth =
                columns * buttonWidth
                        + (columns - 1) * spacingX;

        int panelWidth =
                GUI_WIDTH - 36;

        int startX =
                left + 18
                        + (panelWidth - gridWidth) / 2;

        int startY =
                top + 108;

        for (int i = 0; i < casings.length; i++) {

            SonicCasing casing =
                    casings[i];

            int column =
                    i % columns;

            int row =
                    i / columns;

            int buttonX =
                    startX
                            + column
                            * (buttonWidth + spacingX);

            int buttonY =
                    startY
                            + (row - casingScrollRows)
                            * (buttonHeight + spacingY);

            CasingButton button = new CasingButton(
                    buttonX, buttonY, buttonWidth, buttonHeight, casing);
            casingButtons.add(button);
            addDrawableChild(button);
        }
    }


    private void updateCasingButtonPositions() {
        int columns = 4;
        int buttonWidth = 90;
        int buttonHeight = 30;
        int spacingX = 6;
        int spacingY = 7;
        int gridWidth = columns * buttonWidth + (columns - 1) * spacingX;
        int panelWidth = GUI_WIDTH - 36;
        int startX = left + 18 + (panelWidth - gridWidth) / 2;
        int startY = top + 108;
        for (int i = 0; i < casingButtons.size(); i++) {
            CasingButton button = casingButtons.get(i);
            int row = i / columns;
            int column = i % columns;
            button.setX(startX + column * (buttonWidth + spacingX));
            button.setY(startY + (row - casingScrollRows) * (buttonHeight + spacingY));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        int viewTop = top + CASING_VIEW_TOP;
        int viewBottom = top + CASING_VIEW_BOTTOM;
        if (mouseX >= left + 12 && mouseX <= left + GUI_WIDTH - 12
                && mouseY >= viewTop && mouseY <= viewBottom) {
            int maxRows = Math.max(0, (SonicCasing.values().length - 1) / 4 - VISIBLE_CASING_ROWS + 1);
            int next = Math.max(0, Math.min(maxRows, casingScrollRows + (verticalAmount < 0 ? 1 : -1)));
            if (next != casingScrollRows) {
                casingScrollRows = next;
                updateCasingButtonPositions();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

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

                    button ->
                            onCasingSelected(casing),

                    DEFAULT_NARRATION_SUPPLIER
            );

            this.casing = casing;

            this.previewStack =
                    new ItemStack(GallifreyModItems.SONIC_SCREWDRIVER);

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

            boolean selected =
                    selectedCasing == casing;

            int fill;
            int border;
            int textColor;

            /*
             * ====================================================
             * SELECTED
             *
             * Gold = active / currently equipped casing
             * ====================================================
             */
            if (selected) {

                fill = GOLD_DARK;
                border = GOLD_BRIGHT;
                textColor = GOLD_BRIGHT;

                /*
                 * ====================================================
                 * HOVER
                 *
                 * Bright TARDIS blue
                 * ====================================================
                 */
            } else if (hovered) {

                fill = PANEL_LIGHT;
                border = BLUE;
                textColor = TEXT;

                /*
                 * ====================================================
                 * NORMAL
                 * ====================================================
                 */
            } else {

                fill = PANEL_DARK;
                border = BLUE_DIM;
                textColor = BLUE;
            }

            // Button background
            context.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    fill
            );

            // Button border
            context.drawBorder(
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    border
            );

            // Icon preview
            context.drawItem(
                    previewStack,
                    getX() + getWidth() / 2 - 8,
                    getY() + 2
            );

            // Button text
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    getMessage(),
                    getX() + getWidth() / 2,
                    getY() + 21,
                    textColor
            );
        }
    }

    private void onCasingSelected(
            SonicCasing casing
    ) {

        if (client == null ||
                client.player == null) {

            return;
        }

        selectedCasing = casing;

        SonicCasingClientNetworking.sendCasingChange(
                pos,
                casing
        );

        /*
         * Gold/cyan message to match the new UI.
         */
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

        context.enableScissor(left + 12, top + CASING_VIEW_TOP, left + GUI_WIDTH - 12, top + CASING_VIEW_BOTTOM);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        drawLabels(context);
        context.drawText(textRenderer, "Scroll to browse all 24 casings", left + 20, top + GUI_HEIGHT - 15, TEXT_DIM, false);
    }

    private void drawBackground(
            DrawContext context
    ) {

        /*
         * ========================================================
         * WORLD BACKGROUND
         * ========================================================
         */

        context.fill(
                0,
                0,
                width,
                height,
                0x99000000
        );

        /*
         * ========================================================
         * OUTER TARDIS BLUE GLOW
         * ========================================================
         */

        context.fill(
                left - 3,
                top - 3,
                left + GUI_WIDTH + 3,
                top + GUI_HEIGHT + 3,
                0x30168AAD
        );

        /*
         * ========================================================
         * MAIN PANEL
         * ========================================================
         */

        context.fill(
                left,
                top,
                left + GUI_WIDTH,
                top + GUI_HEIGHT,
                PANEL
        );

        /*
         * ========================================================
         * MAIN BORDER
         * ========================================================
         */

        context.drawBorder(
                left,
                top,
                GUI_WIDTH,
                GUI_HEIGHT,
                BLUE
        );

        /*
         * ========================================================
         * INNER BORDER
         * ========================================================
         */

        context.drawBorder(
                left + 4,
                top + 4,
                GUI_WIDTH - 8,
                GUI_HEIGHT - 8,
                BLUE_DARK
        );

        /*
         * ========================================================
         * HEADER
         * ========================================================
         */

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

        /*
         * ========================================================
         * SONIC INFORMATION
         * ========================================================
         */

        drawSectionBox(
                context,
                left + 18,
                top + 57,
                GUI_WIDTH - 36,
                43
        );

        /*
         * ========================================================
         * CASING GRID
         * ========================================================
         */

        drawSectionBox(
                context,
                left + 18,
                top + 102,
                GUI_WIDTH - 36,
                151
        );

        /*
         * ========================================================
         * BOTTOM ACCENT
         * ========================================================
         */

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

        // Top blue accent
        context.fill(
                x,
                y,
                x + width,
                y + 1,
                BLUE_DARK
        );

        // Bottom subtle blue accent
        context.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                0xFF102D38
        );
    }

    private void drawLabels(
            DrawContext context
    ) {

        /*
         * ========================================================
         * HEADER
         * ========================================================
         */

        context.drawText(
                textRenderer,
                "SONIC WORKSHOP",
                left + 20,
                top + 17,
                GOLD_BRIGHT,
                false
        );

        // Installed Sonic icon at the left of the info box; the labels below sit to its right.
        SonicWorkshopBlockEntity workshop = workshop();
        if (workshop != null) {
            context.drawItem(workshop.getSonic(), left + 26, top + 67);
        }

        context.drawText(
                textRenderer,
                "SONIC CASING CONFIGURATION SYSTEM",
                left + 20,
                top + 31,
                TEXT_DIM,
                false
        );

        /*
         * ========================================================
         * SONIC INFORMATION
         * ========================================================
         */

        context.drawText(
                textRenderer,
                "SONIC SCREWDRIVER",
                left + 48,
                top + 64,
                BLUE,
                false
        );

        /*
         * ========================================================
         * SELECT CASING
         * ========================================================
         */

        context.drawText(
                textRenderer,
                "SELECT CASING",
                left + 48,
                top + 79,
                TEXT_DIM,
                false
        );

        /*
         * ========================================================
         * CURRENT CASING
         *
         * Right aligned so longer casing names don't collide
         * with SELECT CASING.
         * ========================================================
         */

        String currentLabel =
                "CURRENT:";

        String currentValue =
                selectedCasing.getDisplayName();

        int rightEdge =
                left + GUI_WIDTH - 27;

        int valueWidth =
                textRenderer.getWidth(currentValue);

        int labelWidth =
                textRenderer.getWidth(currentLabel);

        int gap = 6;

        int valueX =
                rightEdge - valueWidth;

        int labelX =
                valueX - gap - labelWidth;

        // CURRENT:
        context.drawText(
                textRenderer,
                currentLabel,
                labelX,
                top + 78,
                TEXT_DIM,
                false
        );

        // Third Doctor / Fourth Doctor / etc.
        context.drawText(
                textRenderer,
                currentValue,
                valueX,
                top + 78,
                GOLD_BRIGHT,
                false
        );
    }

    @Override
    public void tick() {
        super.tick();
        // Close if the workshop block is gone, or its Sonic was taken out while open.
        // Don't close just because the client hasn't received the Sonic yet: the
        // server only opens this screen when a Sonic IS installed, and the block
        // data can arrive a tick or two after the open packet.
        boolean blockGone = client == null || client.world == null
                || !(client.world.getBlockEntity(pos) instanceof SonicWorkshopBlockEntity);
        SonicWorkshopBlockEntity workshop = workshop();
        if (workshop != null && !sawSonic) {
            sawSonic = true;
            selectedCasing = SonicScrewdriver.getCasing(workshop.getSonic());
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

