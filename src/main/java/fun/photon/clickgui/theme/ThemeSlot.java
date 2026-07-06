package fun.photon.clickgui.theme;

import fun.photon.clickgui.Palette;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public enum ThemeSlot {

    ACCENT      ("Accent",       () -> Palette.ACCENT,      v -> Palette.ACCENT = v),
    PANEL_BG    ("Panel",        () -> Palette.PANEL_BG,    v -> Palette.PANEL_BG = v),
    HEADER_BG   ("Header",       () -> Palette.HEADER_BG,   v -> Palette.HEADER_BG = v),
    CARD_FILL   ("Card",         () -> Palette.CARD_FILL,   v -> Palette.CARD_FILL = v),
    CARD_OUTLINE("Card outline", () -> Palette.CARD_OUTLINE,v -> Palette.CARD_OUTLINE = v),
    HOVER       ("Hover",        () -> Palette.HOVER,       v -> Palette.HOVER = v),
    TEXT_DARK   ("Text on",      () -> Palette.TEXT_DARK,   v -> Palette.TEXT_DARK = v),
    TEXT_WHITE  ("Text off",     () -> Palette.TEXT_WHITE,  v -> Palette.TEXT_WHITE = v),
    TOGGLE_OFF  ("Toggle off",   () -> Palette.TOGGLE_OFF,  v -> Palette.TOGGLE_OFF = v),
    TOGGLE_ON   ("Toggle on",    () -> Palette.TOGGLE_ON,   v -> Palette.TOGGLE_ON = v),
    KNOB_ON     ("Knob",         () -> Palette.KNOB_ON,     v -> Palette.KNOB_ON = v),
    DIM         ("Dim",          () -> Palette.DIM,         v -> Palette.DIM = v);

    public final String label;
    private final IntSupplier getter;
    private final IntConsumer setter;

    ThemeSlot(String label, IntSupplier getter, IntConsumer setter) {
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    public int read()        { return getter.getAsInt(); }
    public void write(int c)  { setter.accept(c); }
}
