package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.setting.MultiBooleanSetting;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiBooleanValueComponent extends ValueComponent {

    private final MultiBooleanSetting multi;
    private final Map<String, Animation> anims = new HashMap<>();

    public MultiBooleanValueComponent(MultiBooleanSetting setting, float width) {
        super(setting, width - 10);
        this.multi = setting;
    }

    private Animation anim(String opt) {
        return anims.computeIfAbsent(opt, k -> new Animation(Easing.EASE_OUT_CUBIC, 150));
    }

    private static final float PAD = 8;
    private static final float TITLE_ROW = 20;
    private static final float ROW = 24;
    private static final float TW = 30;
    private static final float TH = 16;

    private List<String> options() {
        return new ArrayList<>(multi.getStates().keySet());
    }

    @Override
    public void render(double mouseX, double mouseY) {
        font(14).drawInRowY(multi.getName(), x + PAD, y, TITLE_ROW, fg);

        PhotonFont f = font(13);
        float rowY = y + TITLE_ROW;
        for (String opt : options()) {
            f.drawInRowY(opt, x + PAD + 8, rowY, ROW, fg);

            float tx = x + PAD + width - TW;
            float ty = rowY + (ROW - TH) / 2f;
            if (hover(mouseX, mouseY, tx, ty, TW, TH)) Cursors.set(Cursors.hand());

            Animation a = anim(opt);
            a.run(multi.isOn(opt) ? 1 : 0);
            double t = a.getValue();
            int track = ColorUtil.interpolate(Palette.TOGGLE_OFF, Palette.TOGGLE_ON, t);
            int knob = ColorUtil.interpolate(Palette.KNOB_OFF, Palette.KNOB_ON, t);
            Render2DUtil.rounded(tx, ty, TW, TH, TH / 2f, TH / 2f, TH / 2f, TH / 2f, 1f, track, Palette.TOGGLE_BORDER, 1.5f);
            float kr = TH / 2f - 3f;
            float kcx = (float) (tx + 3 + kr + t * (TW - 2 * (kr + 3)));
            Render2DUtil.circle(kcx, ty + TH / 2f, kr, knob);

            rowY += ROW;
        }

        height = TITLE_ROW + options().size() * ROW + PAD;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        float rowY = y + TITLE_ROW;
        for (String opt : options()) {
            if (hover(mouseX, mouseY, x + PAD, rowY, width, ROW)) {
                multi.toggle(opt);
            }
            rowY += ROW;
        }
    }
}
