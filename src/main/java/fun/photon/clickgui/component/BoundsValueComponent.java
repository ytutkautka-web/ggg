package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.BoundsSetting;
import fun.photon.utils.math.Mathf;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;

public class BoundsValueComponent extends ValueComponent {

    private final BoundsSetting bounds;
    private int grabbed = -1;

    public BoundsValueComponent(BoundsSetting setting, float width) {
        super(setting, width - 10);
        this.bounds = setting;
    }

    private static final float PAD = 8;
    private static final float TOP_ROW = 20;
    private static final float TRACK_H = 4;

    private static String trim(double v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.valueOf(Math.round(v * 100.0) / 100.0);
    }

    private float frac(double v) {
        return (float) ((v - bounds.getMin()) / (bounds.getMax() - bounds.getMin()));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        PhotonFont f = font(14);
        f.drawInRowY(bounds.getName(), x + PAD, y, TOP_ROW, fg);
        f.drawRightInRow(trim(bounds.getLow()) + " - " + trim(bounds.getHigh()), x + PAD + width, y, TOP_ROW, fg);

        float trackY = y + TOP_ROW + 4;
        float tx = x + PAD;
        float tw = width;

        if (grabbed != -1) {
            double p = Math.max(0, Math.min(1, (mouseX - tx) / tw));
            double raw = bounds.getMin() + p * (bounds.getMax() - bounds.getMin());
            raw = Mathf.step(raw, bounds.getStep());
            if (grabbed == 0) bounds.setLow(raw);
            else bounds.setHigh(raw);
            Cursors.set(Cursors.resizeH());
        } else if (hover(mouseX, mouseY, tx, trackY - 5, tw, TRACK_H + 10)) {
            Cursors.set(Cursors.resizeH());
        }

        float lowF = frac(bounds.getLow());
        float highF = frac(bounds.getHigh());
        float cy = trackY + TRACK_H / 2f;

        Render2DUtil.rounded(tx, trackY, tw, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, Palette.TRACK, 0, 0f);
        Render2DUtil.rounded(tx + lowF * tw, trackY, (highF - lowF) * tw, TRACK_H, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, TRACK_H / 2f, 1f, fg, 0, 0f);

        float kw = 6, kh = 13;
        Render2DUtil.rounded(tx + lowF * tw - kw / 2f, cy - kh / 2f, kw, kh, 3f, 3f, 3f, 3f, 1f, fg, 0, 0f);
        Render2DUtil.rounded(tx + highF * tw - kw / 2f, cy - kh / 2f, kw, kh, 3f, 3f, 3f, 3f, 1f, fg, 0, 0f);

        PhotonFont mf = font(11);
        float labelY = trackY + TRACK_H + 4;
        mf.drawString(trim(bounds.getMin()), tx, labelY, Palette.TEXT_MUTED);
        mf.drawRight(trim(bounds.getMax()), tx + tw, labelY, Palette.TEXT_MUTED);

        height = TOP_ROW + 4 + TRACK_H + 4 + mf.getHeight() + PAD;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        float trackY = y + TOP_ROW + 4;
        float tx = x + PAD;
        float tw = width;
        if (!hover(mouseX, mouseY, tx, trackY - 5, tw, TRACK_H + 10)) return;
        float lowX = tx + frac(bounds.getLow()) * tw;
        float highX = tx + frac(bounds.getHigh()) * tw;
        grabbed = Math.abs(mouseX - lowX) <= Math.abs(mouseX - highX) ? 0 : 1;
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
