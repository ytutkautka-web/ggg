package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.BooleanSetting;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.glfw.Cursors;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;

public class BooleanValueComponent extends ValueComponent {

    private final BooleanSetting bool;
    private final Animation anim = new Animation(Easing.EASE_OUT_CUBIC, 150);

    public BooleanValueComponent(BooleanSetting setting, float width) {
        super(setting, width - 10);
        this.bool = setting;
    }

    private static final float PAD = 8;
    private static final float ROW = 28;
    private static final float TW = 30;
    private static final float TH = 16;

    @Override
    public void render(double mouseX, double mouseY) {
        anim.run(bool.get() ? 1 : 0);

        PhotonFont f = font(14);
        f.drawInRowY(bool.getName(), x + PAD, y, ROW, fg);

        float tx = x + PAD + width - TW;
        float ty = y + (ROW - TH) / 2f;

        if (hover(mouseX, mouseY, tx, ty, TW, TH)) Cursors.set(Cursors.hand());

        double t = anim.getValue();
        int onCol = Palette.ACCENT;
        int track = ColorUtil.interpolate(onCol & 0x00FFFFFF, onCol, t);
        int knobColor = ColorUtil.interpolate(Palette.KNOB_OFF, Palette.KNOB_ON, t);

        Render2DUtil.rounded(tx, ty, TW, TH, TH / 2f, TH / 2f, TH / 2f, TH / 2f, 1f, track, Palette.TOGGLE_BORDER, 1.5f);

        float kr = TH / 2f - 3f;
        float kcx = (float) (tx + 3 + kr + t * (TW - 2 * (kr + 3)));
        Render2DUtil.circle(kcx, ty + TH / 2f, kr, knobColor);

        height = ROW;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hover(mouseX, mouseY, x + PAD, y, width, height)) {
            bool.toggle();
        }
    }
}
