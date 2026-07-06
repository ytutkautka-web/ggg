package fun.photon.clickgui.theme;

import fun.photon.utils.render.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public final class ThemeManager {

    private static ThemeManager instance;

    public static ThemeManager get() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    private final List<Theme> themes = new ArrayList<>();
    private Theme active;

    private ThemeManager() {
        Theme def = new Theme("Default", false).capture();
        themes.add(def);

        themes.add(preset("Ocean", def,
                ColorUtil.rgba(40, 165, 220, 255),
                ColorUtil.rgba(6, 14, 26, 180),
                ColorUtil.rgba(232, 244, 250, 255)));

        themes.add(preset("Purple", def,
                ColorUtil.rgba(155, 80, 235, 255),
                ColorUtil.rgba(14, 8, 22, 180),
                ColorUtil.rgba(244, 238, 250, 255)));

        themes.add(midnight(def));

        themes.add(preset("Crimson", def,
                ColorUtil.rgba(230, 60, 72, 255),
                ColorUtil.rgba(22, 8, 10, 185),
                ColorUtil.rgba(250, 238, 240, 255)));

        themes.add(preset("Emerald", def,
                ColorUtil.rgba(40, 200, 140, 255),
                ColorUtil.rgba(6, 20, 16, 180),
                ColorUtil.rgba(236, 250, 244, 255)));

        themes.add(preset("Sunset", def,
                ColorUtil.rgba(245, 150, 50, 255),
                ColorUtil.rgba(24, 14, 6, 185),
                ColorUtil.rgba(250, 244, 234, 255)));

        themes.add(preset("Rose", def,
                ColorUtil.rgba(240, 110, 170, 255),
                ColorUtil.rgba(22, 10, 18, 185),
                ColorUtil.rgba(250, 238, 246, 255)));

        themes.add(new Theme("Custom", true).copyFrom(def));

        active = def;
        active.apply();
    }

    private static Theme preset(String name, Theme base, int accent, int panel, int header) {
        Theme t = new Theme(name, false).copyFrom(base);
        t.set(ThemeSlot.ACCENT, accent);
        t.set(ThemeSlot.PANEL_BG, panel);
        t.set(ThemeSlot.HEADER_BG, header);
        return t;
    }

    private static Theme midnight(Theme base) {
        Theme t = new Theme("Midnight", false).copyFrom(base);
        t.set(ThemeSlot.ACCENT, ColorUtil.rgba(96, 120, 255, 255));
        t.set(ThemeSlot.PANEL_BG, ColorUtil.rgba(0, 0, 0, 200));
        t.set(ThemeSlot.HEADER_BG, ColorUtil.rgba(24, 26, 34, 255));
        t.set(ThemeSlot.CARD_FILL, ColorUtil.rgba(40, 44, 58, 255));
        t.set(ThemeSlot.CARD_OUTLINE, ColorUtil.rgba(90, 100, 140, 120));
        t.set(ThemeSlot.TEXT_DARK, ColorUtil.rgba(235, 238, 250, 255));
        t.set(ThemeSlot.TEXT_WHITE, ColorUtil.rgba(170, 178, 205, 255));
        t.set(ThemeSlot.TOGGLE_ON, ColorUtil.rgba(96, 120, 255, 255));
        t.set(ThemeSlot.KNOB_ON, ColorUtil.rgba(245, 247, 255, 255));
        t.set(ThemeSlot.HOVER, ColorUtil.rgba(255, 255, 255, 28));
        return t;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public Theme getActive() {
        return active;
    }

    public Theme getCustom() {
        for (Theme t : themes) if (t.isEditable()) return t;
        return active;
    }

    public List<Theme> getEditable() {
        List<Theme> out = new ArrayList<>();
        for (Theme t : themes) if (t.isEditable()) out.add(t);
        return out;
    }

    public Theme createCustom() {
        return createCustom(null);
    }

    public Theme createCustom(String name) {
        String base = (name == null || name.trim().isEmpty()) ? "Custom" : name.trim();
        String n = base;
        int i = 2;
        while (findByName(n) != null) n = base + " " + (i++);
        Theme t = new Theme(n, true).copyFrom(active);
        themes.add(t);
        return t;
    }

    public Theme getOrCreateCustom(String name) {
        Theme t = findByName(name);
        if (t != null) return t.isEditable() ? t : null;
        Theme nt = new Theme(name, true);
        themes.add(nt);
        return nt;
    }

    public boolean remove(Theme t) {
        if (t == null || !t.isEditable()) return false;
        if (!themes.remove(t)) return false;
        if (active == t) select(themes.get(0));
        return true;
    }

    public Theme findByName(String name) {
        for (Theme t : themes) if (t.getName().equalsIgnoreCase(name)) return t;
        return null;
    }

    public void selectByName(String name) {
        Theme t = findByName(name);
        if (t != null) select(t);
    }

    public void select(Theme theme) {
        this.active = theme;
        theme.apply();
    }

    public void reapply() {
        active.apply();
    }

    public int accent() {
        return active.get(ThemeSlot.ACCENT);
    }
}
