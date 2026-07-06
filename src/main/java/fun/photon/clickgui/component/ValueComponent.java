package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.Setting;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.PhotonFont;

public abstract class ValueComponent {

    protected final Setting<?> setting;
    protected float x, y, width, height;

    protected int fg = Palette.TEXT_DARK;

    protected ValueComponent(Setting<?> setting, float width) {
        this.setting = setting;
        this.width = width;
    }

    public void setForeground(int color) {
        this.fg = color;
    }

    public Setting<?> getSetting() {
        return setting;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getHeight() {
        return height;
    }

    public abstract void render(double mouseX, double mouseY);

    public void mouseClicked(double mouseX, double mouseY, int button) {}

    public void mouseReleased(double mouseX, double mouseY, int button) {}

    public void keyPressed(int key) {}

    public void onClose() {}

    protected static boolean hover(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    protected static PhotonFont font(float size) {
        return FontUtil.medium(size);
    }
}
