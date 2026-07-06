package fun.photon.clickgui.component;

import com.mojang.blaze3d.platform.Window;
import fun.photon.clickgui.Palette;
import fun.photon.setting.BindSetting;
import fun.photon.utils.KeyUtil;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.glfw.Cursors;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class BindPopup {

    public static BindPopup INSTANCE;

    private static final float W = 168f, H = 92f, PAD = 12f, ROUND = 10f;
    private static final String ICON_EYE = "KiCon/eye.svg";
    private static final String ICON_EYE_OFF = "KiCon/eye_off.svg";
    private static final String ICON_TRASH = "KiCon/trash.svg";

    private BindSetting bind;
    private fun.photon.module.Module module;
    private String title = "";
    private boolean open, listening;
    private float anchorX = -1, anchorY = -1;
    private final Animation anim = new Animation(Easing.EASE_OUT_CUBIC, 200);

    public void openFor(BindSetting setting, fun.photon.module.Module module, float ax, float ay) {
        this.bind = setting;
        this.module = module;
        this.title = module == null ? setting.getName() : module.getName();
        this.anchorX = ax;
        this.anchorY = ay;
        this.open = true;
        this.listening = false;
    }

    public void toggleFor(BindSetting setting, fun.photon.module.Module module, float ax, float ay) {
        if (open && this.module == module) {
            beginClose();
        } else {
            openFor(setting, module, ax, ay);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void beginClose() {
        this.open = false;
        this.listening = false;
    }

    public boolean isListening() {
        return open && listening;
    }

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    private float px() {
        if (anchorX < 0) return window().getWidth() / 2f - W / 2f;
        return Math.max(4f, Math.min(anchorX, window().getWidth() - W - 4f));
    }

    private float py() {
        if (anchorY < 0) return window().getHeight() / 2f - H / 2f;
        return Math.max(4f, Math.min(anchorY, window().getHeight() - H - 4f));
    }

    public boolean isInside(double mx, double my) {
        return open && inside(mx, my, px(), py(), W, H);
    }

    public void render(double mx, double my) {
        anim.run(open ? 1f : 0f);
        float t = (float) anim.getValue();
        if (t < 0.001f) return;

        float x = px() + (1f - t) * 8f, y = py();
        Render2DUtil.roundedRect(x, y, W, H, ROUND, ColorUtil.multAlpha(ColorUtil.rgba(20, 22, 28, 246), t));
        Render2DUtil.roundedOutline(x, y, W, H, ROUND, 1f, ColorUtil.multAlpha(ColorUtil.rgba(70, 74, 88, 255), t));

        float hY = y + PAD - 2, icoS = 16f;
        FontUtil.medium(15).drawInRowY("Бинд", x + PAD, hY - 4, 20,
                ColorUtil.multAlpha(ColorUtil.rgba(238, 240, 248, 255), t));

        float trashX = x + W - PAD - icoS;
        float eyeX = trashX - icoS - 8;
        boolean eyeHov = inside(mx, my, eyeX - 3, hY - 3, icoS + 6, icoS + 6);
        boolean trashHov = inside(mx, my, trashX - 3, hY - 3, icoS + 6, icoS + 6);
        boolean hidden = module != null && module.isHidden();
        ImageUtil.drawSvg(hidden ? ICON_EYE_OFF : ICON_EYE, eyeX, hY, icoS, icoS, ColorUtil.multAlpha(
                eyeHov ? ColorUtil.rgba(255, 255, 255, 255) : ColorUtil.rgba(170, 174, 186, 255), t));
        ImageUtil.drawSvg(ICON_TRASH, trashX, hY, icoS, icoS, ColorUtil.multAlpha(
                trashHov ? ColorUtil.rgba(255, 120, 120, 255) : ColorUtil.rgba(170, 174, 186, 255), t));
        if (eyeHov || trashHov) Cursors.set(Cursors.hand());

        String key = listening ? "нажмите клавишу/мышь…"
                : (bind.getCode() == KeyUtil.NONE ? "NONE" : bind.getKeyName().toUpperCase());
        float kY = y + 30, kH = 24f, kW = W - PAD * 2;
        boolean kHov = inside(mx, my, x + PAD, kY, kW, kH);
        Render2DUtil.roundedRect(x + PAD, kY, kW, kH, 6f, ColorUtil.multAlpha(
                listening ? Palette.ACCENT : (kHov ? ColorUtil.rgba(44, 48, 60, 235) : ColorUtil.rgba(34, 38, 48, 230)), t));
        FontUtil.medium(listening ? 12 : 14).drawCenteredInRow(key, x + W / 2f, kY, kH,
                ColorUtil.multAlpha(listening ? ColorUtil.rgba(255, 255, 255, 255) : ColorUtil.rgba(238, 240, 248, 255), t));
        if (kHov) Cursors.set(Cursors.hand());

        float mY = y + 60, mH = 22f, half = (W - PAD * 2 - 8) / 2f;
        boolean hold = bind.isHold();
        drawBtn(x + PAD, mY, half, mH, "Toggle", !hold, inside(mx, my, x + PAD, mY, half, mH), t);
        drawBtn(x + PAD + half + 8, mY, half, mH, "Hold", hold, inside(mx, my, x + PAD + half + 8, mY, half, mH), t);
    }

    private void drawBtn(float x, float y, float w, float h, String label, boolean active, boolean hov, float t) {
        int bg = active ? Palette.ACCENT : (hov ? ColorUtil.rgba(50, 56, 74, 235) : ColorUtil.rgba(30, 34, 46, 220));
        Render2DUtil.roundedRect(x, y, w, h, 7f, ColorUtil.multAlpha(bg, t));
        FontUtil.medium(14).drawCenteredInRow(label, x + w / 2f, y, h,
                ColorUtil.multAlpha(ColorUtil.rgba(238, 240, 248, 255), t));
        if (hov) Cursors.set(Cursors.hand());
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!open) return false;

        if (listening) {
            bind.setCode(KeyUtil.mouseCode(button));
            listening = false;
            save();
            return true;
        }

        if (!isInside(mx, my)) {

            open = false;
            return true;
        }
        if (button != 0) return true;

        float x = px(), y = py();
        float icoS = 12f, hY = y + PAD;
        float trashX = x + W - PAD - icoS, eyeX = trashX - icoS - 8;
        float kY = y + 30, kH = 24f, kW = W - PAD * 2;
        float mY = y + 60, mH = 22f, half = (W - PAD * 2 - 8) / 2f;

        if (inside(mx, my, eyeX - 3, hY - 3, icoS + 6, icoS + 6)) {
            if (module != null) module.setHidden(!module.isHidden());
            return true;
        }
        if (inside(mx, my, trashX - 3, hY - 3, icoS + 6, icoS + 6)) { bind.setCode(KeyUtil.NONE); save(); return true; }
        if (inside(mx, my, x + PAD, kY, kW, kH)) { listening = true; return true; }
        if (inside(mx, my, x + PAD, mY, half, mH)) { bind.setHold(false); save(); return true; }
        if (inside(mx, my, x + PAD + half + 8, mY, half, mH)) { bind.setHold(true); save(); return true; }
        return true;
    }

    public boolean keyPressed(int key) {
        if (!open) return false;
        if (listening) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { listening = false; return true; }
            bind.setCode(key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE ? KeyUtil.NONE : key);
            listening = false;
            save();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { open = false; return true; }
        return true;
    }

    private void save() {
        fun.photon.Photon.getInstance().getConfigManager().notifyChanged();
    }

    public void onClose() {
        open = false;
        listening = false;
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
