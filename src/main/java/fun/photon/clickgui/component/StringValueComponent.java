package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.StringSetting;
import fun.photon.utils.KeyUtil;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;
import org.lwjgl.glfw.GLFW;

public class StringValueComponent extends ValueComponent {

    private final StringSetting string;
    private boolean focused;

    public StringValueComponent(StringSetting setting, float width) {
        super(setting, width - 10);
        this.string = setting;
    }

    private static final float PAD = 8;
    private static final float NAME_ROW = 22;
    private static final float FIELD_H = 20;

    @Override
    public void render(double mouseX, double mouseY) {
        PhotonFont f = font(14);
        f.drawInRowY(string.getName(), x + PAD, y, NAME_ROW, fg);

        float fx = x + PAD;
        float fy = y + NAME_ROW;
        float fw = width;

        int box = ColorUtil.rgba(245, 245, 248, 235);
        Render2DUtil.rounded(fx, fy, fw, FIELD_H, 5f, 5f, 5f, 5f, 1f, box,
                focused ? Palette.TEXT_DARK : Palette.TOGGLE_BORDER, 1f);

        String text = string.get();
        String shown = focused ? text + "_" : text;
        font(13).drawInRowY(shown, fx + 6, fy, FIELD_H, Palette.TEXT_DARK);

        if (hover(mouseX, mouseY, fx, fy, fw, FIELD_H)) Cursors.set(Cursors.hand());

        height = NAME_ROW + FIELD_H + PAD;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        float fx = x + PAD;
        float fy = y + NAME_ROW;
        focused = button == 0 && hover(mouseX, mouseY, fx, fy, width, FIELD_H);
    }

    @Override
    public void keyPressed(int key) {
        if (!focused) return;
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return;
        }
        String text = string.get();
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!text.isEmpty()) string.setValue(text.substring(0, text.length() - 1));
            return;
        }
        if (key == GLFW.GLFW_KEY_SPACE) {
            string.setValue(text + " ");
            return;
        }
        String name = KeyUtil.getName(key);
        if (name.length() == 1) {
            string.setValue(text + name.toLowerCase());
        }
    }

    @Override
    public void onClose() {
        focused = false;
    }
}
