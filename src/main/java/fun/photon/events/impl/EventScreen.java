package fun.photon.events.impl;

import net.minecraft.client.gui.screens.Screen;

public class EventScreen {

    private final Screen screen;
    private final Screen previous;

    public EventScreen(Screen screen, Screen previous) {
        this.screen = screen;
        this.previous = previous;
    }

    public Screen getScreen() {
        return screen;
    }

    public Screen getPrevious() {
        return previous;
    }

    public boolean isOpening() {
        return screen != null;
    }

    public boolean isClosing() {
        return screen == null;
    }
}
