package fun.photon.module.impl.render;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventClick;
import fun.photon.events.impl.EventRender2D;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.MouseUtil;
import fun.photon.utils.render.Render2DUtil;

@ModuleInfo(
        name = "RenderTest",
        category = Category.RENDER
)
public class RenderTest extends Module {

    private static final class Button {
        final float x, y, w, h;
        final String label;
        boolean on;

        Button(float x, float y, float w, float h, String label) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.label = label;
        }

        boolean hovered() {
            return MouseUtil.isHovered(x, y, w, h);
        }
    }

    private final Button[] buttons = {
            new Button(40, 40, 220, 60, "Alpha"),
            new Button(40, 120, 220, 60, "Beta"),
            new Button(40, 200, 220, 60, "Gamma"),
    };

    private final fun.photon.setting.NumberSetting radius =
            register(new fun.photon.setting.NumberSetting("Radius", 14, 0, 30, 1));
    private final fun.photon.setting.BooleanSetting outline =
            register(new fun.photon.setting.BooleanSetting("Outline", true));
    private final fun.photon.setting.ModeSetting fill =
            register(new fun.photon.setting.ModeSetting("Fill", "Blue", "Blue", "Green"));

    private int clickCount;

    @EventTarget
    public void onClick(EventClick event) {

        if (!event.isPressed() || !event.isLeft()) return;
        if (fun.photon.utils.ScreenUtil.isOpen()) return;

        for (Button b : buttons) {
            if (event.isInside(b.x, b.y, b.w, b.h)) {
                b.on = !b.on;
                clickCount++;
                System.out.println("[Photon] click on " + b.label + " -> " + b.on
                        + " @ (" + (int) event.getX() + ", " + (int) event.getY() + ")");
                event.cancel();
                return;
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        float r = radius.getFloat();
        int onColor = fill.is("Green") ? 0xFF40E080 : 0xFF3050C0;
        for (Button b : buttons) {
            int base = b.on ? onColor : 0xFF505868;
            int color = b.hovered() ? ColorUtil.brighten(base, 40) : base;
            Render2DUtil.roundedRect(b.x, b.y, b.w, b.h, r, color);
            if (outline.get() && b.hovered()) {
                Render2DUtil.roundedOutline(b.x, b.y, b.w, b.h, r, 2, 0xFFFFFFFF);
            }
            FontUtil.medium(18f).drawString(
                    b.label + (b.on ? " [on]" : " [off]"),
                    b.x + 16, b.y + b.h / 2f - 9, 0xFFFFFFFF);
        }

        FontUtil.medium(16f).drawString(
                "mouse: " + (int) MouseUtil.getX() + ", " + (int) MouseUtil.getY()
                        + "   clicks: " + clickCount
                        + "   bind: " + fun.photon.utils.KeyUtil.getName(getKey()),
                40, 290, 0xFFFFFFFF);

        ImageUtil.drawPng("logo.png", 320, 40, 120, 120, 0xFFFFFFFF);
        ImageUtil.drawSvg("KiCon/settings.svg", 320, 180, 48, 48, 0xFF40E080);
        ImageUtil.drawSvg("KiCon/search.svg", 380, 180, 48, 48, 0xFF50A0FF);
        ImageUtil.drawSvg("KiCon/user.svg", 440, 180, 48, 48, 0xFFFF3050);
    }
}
