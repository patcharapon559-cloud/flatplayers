package com.example.flatplayers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** In-game settings screen (opened with the keybind or from the Mods menu). */
public class FlatConfigScreen extends Screen {
    private static final int MAX_SLIDER = 128;
    private final Screen parent;

    public FlatConfigScreen(Screen parent) {
        super(Component.literal("Flat Players Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 220;
        int x = this.width / 2 - w / 2;
        int y = this.height / 4;

        addRenderableWidget(Button.builder(enabledText(), b -> {
            FlatConfig.ENABLED.set(!FlatConfig.ENABLED.get());
            b.setMessage(enabledText());
        }).bounds(x, y, w, 20).build());

        addRenderableWidget(new DistanceSlider(x, y + 28, w, 20));

        addRenderableWidget(Button.builder(nametagText(), b -> {
            FlatConfig.SHOW_NAMETAG.set(!FlatConfig.SHOW_NAMETAG.get());
            b.setMessage(nametagText());
        }).bounds(x, y + 56, w, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(x, y + 96, w, 20).build());
    }

    private static Component enabledText() {
        return Component.literal("Flat players: " + (FlatConfig.ENABLED.get() ? "ON" : "OFF"));
    }

    private static Component nametagText() {
        return Component.literal("Name tags: " + (FlatConfig.SHOW_NAMETAG.get() ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 24, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        // write changes to config/flatplayers-client.toml
        FlatConfig.ENABLED.save();
        FlatConfig.FLAT_DISTANCE.save();
        FlatConfig.SHOW_NAMETAG.save();
    }

    private static class DistanceSlider extends AbstractSliderButton {
        DistanceSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(),
                    Math.min(1.0D, FlatConfig.FLAT_DISTANCE.get() / MAX_SLIDER));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int d = (int) Math.round(this.value * MAX_SLIDER);
            setMessage(Component.literal(d == 0
                    ? "Flat distance: all other players"
                    : "Flat distance: beyond " + d + " blocks"));
        }

        @Override
        protected void applyValue() {
            FlatConfig.FLAT_DISTANCE.set((double) Math.round(this.value * MAX_SLIDER));
        }
    }
}
