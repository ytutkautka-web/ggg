package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.ColorSetting;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;

public class ColorValueComponent extends ValueComponent {

    private final ColorSetting color;
    private boolean expanded;
    private int grabbed = -1;

    public ColorValueComponent(ColorSetting setting, float width) {
        super(setting, width - 10);
        this.color = setting;
    }

    private static final float PAD = 8;
    private static final float NAME_ROW = 28;
    private static final float SW = 28;
    private static final float SH = 16;
    private static final float TRACK_H = 4;
    private static final float SLIDER_ROW = 18;

    private static final String[] CH = {"R", "G", "B", "A"};

    private int channel(int i) {
        switch (i) {
            case 0:  return ColorUtil.red(color.get());
            case 1:  return ColorUtil.green(color.get());
            case 2:  return ColorUtil.blue(color.get());
            default: return ColorUtil.alpha(color.get());
        }
    }

    private void setChannel(int i, int v) {
        v = Math.max(0, Math.min(255, v));
        int c = color.get();
        int r = ColorUtil.red(c), g = ColorUtil.green(c), b = ColorUtil.blue(c), a = ColorUtil.alpha(c);
        switch (i) {
            case 0: r = v; break;
            case 1: g = v; break;
            case 2: b = v; break;
            default: a = v; break;
        }
        color.set(ColorUtil.rgba(r, g, b, a));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        PhotonFont f = font(14);
        f.drawInRowY(color.getName(), x + PAD, y, NAME_ROW, fg);

        float sx = x + PAD + width - SW;
        float sy = y + (NAME_ROW - SH) / 2f;
        Render2DUtil.rounded(sx, sy, SW, SH, 5f, 5f, 5f, 5f, 1f, color.get(), 0, 0f);
        Render2DUtil.rounded(sx, sy, SW, SH, 5f, 5f, 5f, 5f, 1f, 0, Palette.TOGGLE_BORDER, 1f);
        if (hover(mouseX, mouseY, sx, sy, SW, SH)) Cursors.set(Cursors.hand());

        float total = NAME_ROW;

        if (expanded) {
            float trackX = x + PAD + 14;
            float trackW = width - 14;
            for (int i = 0; i < 4; i++) {
                float rowY = y + NAME_ROW + i * SLIDER_ROW;
                float trackY = rowY + (SLIDER_ROW - TRACK_H) / 2f;

                if (grabbed == i) {
                    double p = Math.max(0, Math.min(1, (mouseX - trackX) / trackW));
                    setChannel(i, (int) Math.round(p * 255));
                    Cursors.set(Cursors.resizeH());
                } else if (hover(mouseX, mouseY, trackX, trackY - 5, trackW, TRACK_H + 10)) {
                    Cursors.set(Cursors.resizeH());
                }

                font(11).drawInRowY(CH[i], x + PAD, rowY, SLIDER_ROW, fg);

                float frac = channel(i) / 255f;
                Render2DUtil.rounded(trackX, trackY, trackW, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, Palette.TRACK, 0, 0f);
                Render2DUtil.rounded(trackX, trackY, frac * trackW, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, fg, 0, 0f);
                float kw = 6, kh = 13;
                Render2DUtil.rounded(trackX + frac * trackW - kw / 2f, trackY + TRACK_H / 2f - kh / 2f, kw, kh, 3f, 3f, 3f, 3f, 1f, fg, 0, 0f);
            }
            total = NAME_ROW + 4 * SLIDER_ROW + PAD;
        }

        height = total;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        float sx = x + PAD + width - SW;
        float sy = y + (NAME_ROW - SH) / 2f;
        if (button == 0 && hover(mouseX, mouseY, sx, sy, SW, SH)) {
            expanded = !expanded;
            return;
        }
        if (expanded && button == 0) {
            float trackX = x + PAD + 14;
            float trackW = width - 14;
            for (int i = 0; i < 4; i++) {
                float rowY = y + NAME_ROW + i * SLIDER_ROW;
                float trackY = rowY + (SLIDER_ROW - TRACK_H) / 2f;
                if (hover(mouseX, mouseY, trackX, trackY - 5, trackW, TRACK_H + 10)) {
                    grabbed = i;
                }
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        grabbed = -1;
    }

    @Override
    public void onClose() {
        grabbed = -1;
    }
}
