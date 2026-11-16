package com.cerulixteam.gallifrey.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;

public class VortexSavedLocationsScreen extends VortexManipulatorSubScreen {
    private TextFieldWidget locationName;
    private int page;

    public VortexSavedLocationsScreen() { super("Vortex Manipulator - Saved Locations"); }

    @Override protected void build() {
        locationName = field(left + 24, top + 92, 300, "Location name");
        addDrawableChild(btn(left + 334, top + 92, 85, 24, "SAVE HERE", b -> saveLocation()));
        addDrawableChild(btn(left + 429, top + 92, 87, 24, "DELETE", b -> deleteLocation()));

        int start = page * 6;
        for (int i = 0; i < 6; i++) {
            int idx = start + i;
            if (idx >= LOCATIONS.size()) break;
            Location loc = LOCATIONS.get(idx);
            int yy = top + 132 + i * 30;
            addDrawableChild(btn(left + 24, yy, 390, 24,
                    loc.name() + "  [" + shortDimension(loc.dimension()) + "]",
                    b -> goLocation(loc.name())));
            ButtonWidget del = btn(left + 424, yy, 92, 24, "DELETE",
                    b -> sendString("DELETE", loc.name()));
            addDrawableChild(del);
        }
        addDrawableChild(btn(left + 150, top + 328, 100, 22, "◀ PAGE",
                b -> { if (page > 0) { page--; clearAndRebuild(); }}));
        addDrawableChild(btn(left + 290, top + 328, 100, 22, "PAGE ▶",
                b -> { if ((page + 1) * 6 < LOCATIONS.size()) { page++; clearAndRebuild(); }}));
        buildBackButton();
    }

    private void saveLocation() {
        String name = locationName.getText().trim();
        if (!name.isEmpty()) sendString("SAVE", name);
    }

    private void deleteLocation() {
        String name = locationName.getText().trim();
        if (!name.isEmpty()) sendString("DELETE", name);
    }

    private void goLocation(String name) {
        sendString("GO", name);
        client.setScreen(null);
    }

    @Override protected void renderVortexChrome(DrawContext c, int mx, int my, float delta) {
        buildHeader(c, "SAVED LOCATIONS");
        c.drawText(textRenderer, LOCATIONS.isEmpty() ? "No locations stored." :
                "Select a location to travel, or delete it.", left + 24, top + 116, DIM, false);
    }
}
