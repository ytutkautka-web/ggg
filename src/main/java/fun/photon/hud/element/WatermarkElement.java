package fun.photon.hud.element;

import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class WatermarkElement extends HudElement {

    public boolean showUser = true;
    public boolean showFps = true;
    public boolean showBps = true;
    public boolean showPing = true;
    public boolean showTps = true;
    public boolean showIp = false;
    public boolean useLogo = false;

    public WatermarkElement() {
        super("watermark", "Водяной знак", 6, 6);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.watermark.get();
    }

    @Override
    public boolean hasSettings() { return true; }

    @Override
    public List<Opt> options() {
        List<Opt> l = new ArrayList<>();
        l.add(new Opt("Ник", () -> showUser, () -> showUser = !showUser));
        l.add(new Opt("FPS", () -> showFps, () -> showFps = !showFps));
        l.add(new Opt("BPS", () -> showBps, () -> showBps = !showBps));
        l.add(new Opt("Пинг", () -> showPing, () -> showPing = !showPing));
        l.add(new Opt("TPS", () -> showTps, () -> showTps = !showTps));
        l.add(new Opt("IP сервера", () -> showIp, () -> showIp = !showIp));
        l.add(new Opt("Лого вместо названия", () -> useLogo, () -> useLogo = !useLogo));
        return l;
    }

    public String flags() {
        return (showUser ? "1" : "0") + (showFps ? "1" : "0") + (showBps ? "1" : "0")
                + (showPing ? "1" : "0") + (showTps ? "1" : "0") + (showIp ? "1" : "0")
                + (useLogo ? "1" : "0");
    }

    public void setFlags(String s) {
        if (s == null || s.length() < 7) return;
        showUser = s.charAt(0) == '1';
        showFps = s.charAt(1) == '1';
        showBps = s.charAt(2) == '1';
        showPing = s.charAt(3) == '1';
        showTps = s.charAt(4) == '1';
        showIp = s.charAt(5) == '1';
        useLogo = s.charAt(6) == '1';
    }

    private String fps() { return String.valueOf(mc.getFps()); }

    private String bps() {
        if (mc.player == null) return "0.0";
        double dx = mc.player.getX() - mc.player.xOld;
        double dz = mc.player.getZ() - mc.player.zOld;
        return String.format(java.util.Locale.US, "%.1f", Math.sqrt(dx * dx + dz * dz) * 20.0);
    }

    private String ping() {
        if (mc.getConnection() == null || mc.player == null) return "0";
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? "0" : String.valueOf(info.getLatency());
    }

    private String user() {
        return mc.player == null ? "Player" : mc.player.getName().getString();
    }

    private String ip() {
        net.minecraft.client.multiplayer.ServerData sd = mc.getCurrentServer();
        return sd == null ? "Локально" : sd.ip;
    }

    private String tps() {
        if (mc.level == null) return "20.0";
        return String.format(java.util.Locale.US, "%.1f", fun.photon.utils.TpsTracker.get());
    }

    private static final class Stat {
        final String id, icon, value;
        final boolean on;
        Stat(String id, String icon, String value, boolean on) {
            this.id = id; this.icon = icon; this.value = value; this.on = on;
        }
    }

    private final java.util.Map<String, Animation> anims = new java.util.HashMap<>();

    private float anim(String id, boolean on) {
        Animation a = anims.computeIfAbsent(id, k -> new Animation(Easing.EASE_OUT_CUBIC, 220));
        a.run(on ? 1f : 0f);
        return (float) a.getValue();
    }

    private List<Stat> stats() {
        List<Stat> l = new ArrayList<>();
        l.add(new Stat("user", "KiCon/user.svg", user(), showUser));
        l.add(new Stat("fps", "KiCon/monitor.svg", fps(), showFps));
        l.add(new Stat("bps", "KiCon/movement.svg", bps(), showBps));
        l.add(new Stat("ping", "KiCon/signal_4_4.svg", ping(), showPing));
        l.add(new Stat("tps", "KiCon/pulse.svg", tps(), showTps));
        l.add(new Stat("ip", "KiCon/internet.svg", ip(), showIp));
        return l;
    }

    private float iconSize() { return font(12).getHeight() * 0.92f; }

    private float logoH() { return height(); }

    private float logoW() { return logoH(); }

    private void boldIcon(String icon, float ix, float iy, float ic) {
        float o = s(0.5f);
        ImageUtil.drawSvg(icon, ix - o, iy, ic, ic, accent());
        ImageUtil.drawSvg(icon, ix + o, iy, ic, ic, accent());
        ImageUtil.drawSvg(icon, ix, iy - o, ic, ic, accent());
        ImageUtil.drawSvg(icon, ix, iy + o, ic, ic, accent());
        ImageUtil.drawSvg(icon, ix, iy, ic, ic, accent());
    }

    private float statW(Stat st) {
        return iconSize() + s(4) + font(12).getWidth(st.value) + s(10);
    }

    private float statsWidth() {
        float w = 0;
        for (Stat st : stats()) w += statW(st) * anim(st.id, st.on);
        return w;
    }

    private final Animation logoAnim = new Animation(Easing.EASE_OUT_CUBIC, 260);

    private float logoFade() {
        logoAnim.run(useLogo ? 1f : 0f);
        return (float) logoAnim.getValue();
    }

    private float titleWidth() {
        float la = logoFade();
        return font(13).getWidth("Proton") * (1f - la) + logoW() * la;
    }

    @Override
    public float height() {
        return font(13).getHeight() + s(8);
    }

    @Override
    public float width() {
        float sw = statsWidth();
        float divider = sw > 0.5f ? s(8) + 1 + s(8) : 0;
        return s(8) + titleWidth() + divider + sw + s(4);
    }

    @Override
    public void render() {
        PhotonFont t = font(13);
        PhotonFont f = font(12);
        float h = height();
        float w = width();

        panel(x, y, w, h);
        Render2DUtil.rounded(x, y, s(2), h, 0f, 0f, s(4), s(4), 1f, accent(), 0, 0f);

        float base0 = RenderTransform.alpha;
        float la = logoFade();
        float cx = x + s(8);
        float tw = titleWidth();
        if (la < 0.99f) {
            RenderTransform.alpha = base0 * (1f - la);
            t.drawInRowY("Proton", cx + (tw - t.getWidth("Proton")) / 2f, y, h, accent());
        }
        if (la > 0.01f) {
            RenderTransform.alpha = base0 * la;
            float lh = logoH();
            float lw = logoW();
            ImageUtil.drawPng("logo.png", cx + (tw - lw) / 2f, y + (h - lh) / 2f, lw, lh, -1);
        }
        RenderTransform.alpha = base0;
        cx += tw + s(8);

        float sw = statsWidth();
        if (sw > 0.5f) {
            Render2DUtil.rounded(cx, y + s(5), 1, h - s(10), 0f, 0f, 0f, 0f, 1f, ColorUtil.rgba(255, 255, 255, 45), 0, 0f);
            cx += s(8);
        }

        float base = RenderTransform.alpha;
        float ic = iconSize();
        for (Stat st : stats()) {
            float v = anim(st.id, st.on);
            if (v <= 0.01f) continue;
            RenderTransform.alpha = base * v;
            float sz = ic * v;
            float iy = y + (h - sz) / 2f;
            boldIcon(st.icon, cx + (ic - sz) / 2f, iy, sz);
            cx += (ic + s(4)) * v;
            f.drawInRowY(st.value, cx, y, h, Palette.TEXT_LIGHT);
            cx += (f.getWidth(st.value) + s(10)) * v;
        }
        RenderTransform.alpha = base;
    }
}
