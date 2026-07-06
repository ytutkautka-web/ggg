package fun.photon.clickgui.theme;

public class Theme {

    private String name;
    private final boolean editable;
    private final int[] colors = new int[ThemeSlot.values().length];

    public Theme(String name, boolean editable) {
        this.name = name;
        this.editable = editable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public int get(ThemeSlot slot) {
        return colors[slot.ordinal()];
    }

    public void set(ThemeSlot slot, int color) {
        colors[slot.ordinal()] = color;
    }

    public Theme capture() {
        for (ThemeSlot s : ThemeSlot.values()) colors[s.ordinal()] = s.read();
        return this;
    }

    public Theme copyFrom(Theme other) {
        System.arraycopy(other.colors, 0, this.colors, 0, colors.length);
        return this;
    }

    public void apply() {
        for (ThemeSlot s : ThemeSlot.values()) s.write(colors[s.ordinal()]);
    }
}
