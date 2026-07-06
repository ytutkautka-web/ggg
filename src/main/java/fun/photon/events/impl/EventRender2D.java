package fun.photon.events.impl;

import net.minecraft.client.gui.GuiGraphics;

public class EventRender2D {

    private final GuiGraphics graphics;
    private final float partialTicks;

    public EventRender2D(GuiGraphics graphics, float partialTicks) {
        this.graphics = graphics;
        this.partialTicks = partialTicks;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}