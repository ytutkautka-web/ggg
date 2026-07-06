package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.math.Interpolator;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;

public class NumberValueComponent extends ValueComponent {

    private final NumberSetting number;
    private boolean grabbed;
    private float current;

    public NumberValueComponent(NumberSetting setting, float width) {
        super(setting, width - 10);
        this.number = setting;
        this.current = frac();
    }

    private float frac() {
        return (float) ((number.get() - number.getMin()) / (number.getMax() - number.getMin()));
    }

    private static String trim(double v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.valueOf(Math.round(v * 100.0) / 100.0);
    }

    private static final float PAD = 8;
    private static final float TOP_ROW = 20;
    private static final float TRACK_H = 4;

    @Override
    public void render(double mouseX, double mouseY) {
        PhotonFont f = font(14);

        f.drawInRowY(number.getName(), x + PAD, y, TOP_ROW, fg);
        f.drawRightInRow(trim(number.get()), x + PAD + width, y, TOP_ROW, fg);

        float trackY = y + TOP_ROW + 4;
        float tx = x + PAD;
        float tw = width;

        if (grabbed) {
            double p = Math.max(0, Math.min(1, (mouseX - tx) / tw));
            double raw = number.getMin() + p * (number.getMax() - number.getMin());
            number.set(fun.photon.utils.math.Mathf.step(raw, number.getStep()));
            Cursors.set(Cursors.resizeH());
        } else if (hover(mouseX, mouseY, tx, trackY - 5, tw, TRACK_H + 10)) {
            Cursors.set(Cursors.resizeH());
        }

        current = Interpolator.lerp(current, frac(), 0.5f);
        float fillW = current * tw;
        float cy = trackY + TRACK_H / 2f;

        Render2DUtil.rounded(tx, trackY, tw, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, Palette.TRACK, 0, 0f);
        Render2DUtil.rounded(tx, trackY, fillW, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, fg, 0, 0f);

        float kw = 6, kh = 13;
        Render2DUtil.rounded(tx + fillW - kw / 2f, cy - kh / 2f, kw, kh, 3f, 3f, 3f, 3f, 1f, fg, 0, 0f);

        PhotonFont mf = font(11);
        float labelY = trackY + TRACK_H + 4;
        mf.drawString(trim(number.getMin()), tx, labelY, Palette.TEXT_MUTED);
        mf.drawRight(trim(number.getMax()), tx + tw, labelY, Palette.TEXT_MUTED);

        height = TOP_ROW + 4 + TRACK_H + 4 + mf.getHeight() + PAD;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        float trackY = y + TOP_ROW + 4;
        if (button == 0 && hover(mouseX, mouseY, x + PAD, trackY - 5, width, TRACK_H + 10)) {
            grabbed = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        grabbed = false;
    }

    @Override
    public void onClose() {
        grabbed = false;
    }
}
