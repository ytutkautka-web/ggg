package fun.photon.clickgui.component;

import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import net.minecraft.client.Minecraft;

public final class Tooltip {

    private static String pending;
    private static String shown;
    private static final Animation alpha = new Animation(Easing.EASE_OUT_CUBIC, 160);

    private Tooltip() {}

    public static void queue(String t, double mouseX, double mouseY) {
        if (t == null || t.isEmpty()) return;
        pending = t;
    }

    public static void renderQueued() {
        if (pending != null) {
            shown = pending;
        }
        alpha.run(pending != null ? 1f : 0f);
        float a = (float) alpha.getValue();
        pending = null;

        if (shown == null || a <= 0.001f) {
            if (a <= 0.001f) shown = null;
            return;
        }

        PhotonFont f = FontUtil.medium(15);
        float pad = 10f;
        float w = f.getWidth(shown) + pad * 2;
        float h = 22f;
        float x = Minecraft.getInstance().getWindow().getWidth() / 2f - w / 2f;
        float y = 30f + (1f - a) * 6f;

        Render2DUtil.roundedRect(x, y, w, h, 6f, ColorUtil.multAlpha(ColorUtil.rgba(18, 20, 26, 244), a));
        Render2DUtil.roundedOutline(x, y, w, h, 6f, 1f, ColorUtil.multAlpha(ColorUtil.rgba(70, 74, 88, 255), a));
        f.drawCenteredInRow(shown, x + w / 2f, y, h, ColorUtil.multAlpha(ColorUtil.rgba(238, 240, 248, 255), a));
    }
}
