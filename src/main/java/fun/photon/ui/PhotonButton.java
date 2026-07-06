package fun.photon.ui;

import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.MouseUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;

public class PhotonButton {

    private static final int IDLE = ColorUtil.rgba(22, 24, 38, 170);
    private static final int HOVER = ColorUtil.rgba(60, 90, 200, 210);
    private static final int OUTLINE = ColorUtil.rgba(120, 150, 255, 120);
    private static final int TEXT = ColorUtil.rgba(235, 238, 250, 255);

    private final String label;
    private final Runnable action;
    private final Animation hover = new Animation(Easing.EASE_OUT_CUBIC, 180);

    private float x, y, w, h;

    public PhotonButton(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public void setBounds(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getW() { return w; }
    public float getH() { return h; }

    private boolean inside(double mx, double my) {
        return MouseUtil.isInside(mx, my, x, y, w, h);
    }

    public void render(double mx, double my) {
        hover.run(inside(mx, my) ? 1d : 0d);
        float k = (float) hover.getValue();

        int fill = ColorUtil.interpolate(IDLE, HOVER, k);
        Render2DUtil.roundedRect(x, y, w, h, 6f, fill);
        Render2DUtil.roundedOutline(x, y, w, h, 6f, 1f, ColorUtil.multAlpha(OUTLINE, 0.4f + 0.6f * k));

        PhotonFont font = FontUtil.medium(16f);
        if (font != null) {
            font.drawCenteredInRow(label, x + w / 2f, y, h, TEXT);
        }
    }

    public boolean click(double mx, double my, int button) {
        if (button == 0 && inside(mx, my)) {
            action.run();
            return true;
        }
        return false;
    }
}
