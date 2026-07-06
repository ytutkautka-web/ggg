package fun.photon.module.impl.render;

import fun.photon.clickgui.Palette;
import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventClick;
import fun.photon.events.impl.EventRender2D;
import fun.photon.hud.HudElement;
import fun.photon.hud.element.ArmorElement;
import fun.photon.hud.element.ArraylistElement;
import fun.photon.utils.render.PhotonFont;
import fun.photon.hud.element.CooldownsElement;
import fun.photon.hud.element.CoordsElement;
import fun.photon.hud.element.KeyBindsElement;
import fun.photon.hud.element.PotionsElement;
import fun.photon.hud.element.StaffListElement;
import fun.photon.hud.element.TargetHudElement;
import fun.photon.hud.element.WatermarkElement;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.BooleanSetting;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.IMinecraft;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.MouseUtil;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "HUD",
        category = Category.RENDER,
        description = "Интерфейс на экране"
)
public class Hud extends Module implements IMinecraft {

    public static Hud INSTANCE;

    public final BooleanSetting watermark = register(new BooleanSetting("Водяной знак", true));
    public final BooleanSetting coords = register(new BooleanSetting("Координаты", true));
    public final BooleanSetting arraylist = register(new BooleanSetting("Список модулей", true));
    public final BooleanSetting potions = register(new BooleanSetting("Эффекты", true));
    public final BooleanSetting targethud = register(new BooleanSetting("Цель", true));
    public final BooleanSetting keybinds = register(new BooleanSetting("Бинды", false));
    public final BooleanSetting stafflist = register(new BooleanSetting("Стафф", false));
    public final BooleanSetting cooldowns = register(new BooleanSetting("Откаты", false));
    public final BooleanSetting armor = register(new BooleanSetting("Броня", true));
    public final BooleanSetting notifications = register(new BooleanSetting("Уведомления", true));
    public final NumberSetting scale = register(new NumberSetting("Масштаб", 100, 50, 200, 5));

    private String loadedPositions = "";
    private final java.util.Set<String> savedIds = new java.util.HashSet<>();

    private final List<HudElement> elements = new ArrayList<>();
    private boolean placed;

    private HudElement dragging;
    private float offX, offY;
    private boolean snapX, snapY;

    private final Animation editFade = new Animation(Easing.EASE_OUT_CUBIC, 200);

    private static final float SNAP = 6f;

    public boolean notifTopCenter = true;
    public boolean notifProgress = true;
    public boolean notifLonger = false;
    public boolean notifSettingsOpen = false;
    public final Animation notifSettingsAnim = new Animation(Easing.EASE_OUT_CUBIC, 200);

    private float notifDurScale() { return notifLonger ? 1.7f : 1f; }

    private boolean notifExpired(fun.photon.notify.Notification n) {
        return n.age() > n.duration * notifDurScale();
    }

    private float notifProgressFrac(fun.photon.notify.Notification n) {
        return 1f - n.age() / (n.duration * notifDurScale());
    }

    public Hud() {
        INSTANCE = this;
        elements.add(new WatermarkElement());
        elements.add(new CoordsElement());
        elements.add(new ArraylistElement());
        elements.add(new PotionsElement());
        elements.add(new TargetHudElement());
        elements.add(new KeyBindsElement());
        elements.add(new StaffListElement());
        elements.add(new CooldownsElement());
        elements.add(new ArmorElement());
    }

    public List<HudElement> getElements() {
        return elements;
    }

    private boolean editing() {
        return mc.screen instanceof ChatScreen;
    }

    private void placeDefaults() {
        if (placed) return;
        float sw = Render2DUtil.screenWidth();
        float sh = Render2DUtil.screenHeight();
        if (sw <= 0 || sh <= 0) return;
        placed = true;
        for (HudElement e : elements) {
            if (savedIds.contains(e.id)) {
                float px = e.getX(), py = e.getY();
                if (px > sw - 4) px = sw - e.width() - 4;
                if (py > sh - 4) py = sh - e.height() - 4;
                e.setPosition(Math.max(0, px), Math.max(0, py));
                continue;
            }
            if (e.id.equals("arraylist")) e.setPosition(sw - e.width() - 4, 4);
            else if (e.id.equals("coords")) e.setPosition(6, 30);
        }
    }

    public void writeExtra(com.google.gson.JsonObject obj) {
        obj.addProperty("positions", positionsString());
        obj.addProperty("staff", fun.photon.hud.element.StaffListElement.names);
        WatermarkElement wm = watermark();
        if (wm != null) obj.addProperty("watermark", wm.flags());
        obj.addProperty("notif", notifFlags());
    }

    public void readExtra(com.google.gson.JsonObject obj) {
        if (obj.has("positions")) {
            loadedPositions = obj.get("positions").getAsString();
            applySaved();
            placed = false;
        }
        if (obj.has("staff")) {
            fun.photon.hud.element.StaffListElement.names = obj.get("staff").getAsString();
        }
        if (obj.has("watermark")) {
            WatermarkElement wm = watermark();
            if (wm != null) wm.setFlags(obj.get("watermark").getAsString());
        }
        if (obj.has("notif")) {
            setNotifFlags(obj.get("notif").getAsString());
        }
    }

    private String notifFlags() {
        return (notifTopCenter ? "1" : "0") + (notifProgress ? "1" : "0") + (notifLonger ? "1" : "0");
    }

    private void setNotifFlags(String s) {
        if (s == null || s.length() < 3) return;
        notifTopCenter = s.charAt(0) == '1';
        notifProgress = s.charAt(1) == '1';
        notifLonger = s.charAt(2) == '1';
    }

    private WatermarkElement watermark() {
        for (HudElement e : elements) if (e instanceof WatermarkElement w) return w;
        return null;
    }

    private void applySaved() {
        savedIds.clear();
        java.util.Map<String, float[]> saved = parsePositions();
        for (HudElement e : elements) {
            float[] p = saved.get(e.id);
            if (p != null) {
                e.setPosition(p[0], p[1]);
                savedIds.add(e.id);
            }
        }
    }

    private String positionsString() {
        StringBuilder sb = new StringBuilder();
        for (HudElement e : elements) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.id).append(':').append((int) e.getX()).append(':').append((int) e.getY());
        }
        return sb.toString();
    }

    private java.util.Map<String, float[]> parsePositions() {
        java.util.Map<String, float[]> map = new java.util.HashMap<>();
        String raw = loadedPositions == null ? "" : loadedPositions.trim();
        if (raw.isEmpty()) return map;
        for (String part : raw.split(";")) {
            String[] kv = part.split(":");
            if (kv.length == 3) {
                try {
                    map.put(kv[0], new float[]{Float.parseFloat(kv[1]), Float.parseFloat(kv[2])});
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private void savePositions() {
        loadedPositions = positionsString();
        fun.photon.Photon.getInstance().getConfigManager().notifyChanged();
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        boolean chatOpen = editing();
        if (!chatOpen && dragging != null) {
            dragging = null;
            snapX = snapY = false;
            savePositions();
        }
        if (!chatOpen) for (HudElement e : elements) e.settingsOpen = false;

        editFade.run(chatOpen ? 1f : 0f);
        float ef = (float) editFade.getValue();
        boolean edit = ef > 0.01f;

        HudElement.EDIT = edit;
        HudElement.GRAPHICS = event.getGraphics();
        HudElement.SCALE = scale.getFloat() / 100f;
        placeDefaults();

        float sw = Render2DUtil.screenWidth();
        float sh = Render2DUtil.screenHeight();

        if (chatOpen) updateDrag(sw, sh);

        if (edit) {
            RenderTransform.set(1f, 0f, 0f, ef);
            grid(sw, sh);
            RenderTransform.reset();
        }

        for (HudElement e : elements) {
            e.vis.run(e.active() ? 1f : 0f);
            float a = (float) e.vis.getValue();
            if (a <= 0.01f) continue;

            float w = e.width(), h = e.height();
            float slide = (1f - a) * 12f * HudElement.SCALE;
            float scale = 0.88f + 0.12f * a;
            float pivX = e.getX() + w / 2f;
            float pivY = e.getY() + slide + h / 2f;

            float oy = e.getY();
            e.setPosition(e.getX(), oy + slide);
            RenderTransform.set(scale, pivX, pivY, a);
            e.render();
            RenderTransform.reset();
            e.setPosition(e.getX(), oy);
        }

        drawNotifications();

        if (edit) {
            RenderTransform.set(1f, 0f, 0f, ef);
            for (HudElement e : elements) if (e.enabled()) box(e);
            if (dragging != null && snapX) Render2DUtil.rect(sw / 2f - 0.5f, 0, 1f, sh, Palette.ACCENT);
            if (dragging != null && snapY) Render2DUtil.rect(0, sh / 2f - 0.5f, sw, 1f, Palette.ACCENT);
            for (HudElement e : elements) {
                if (!e.hasSettings()) continue;
                e.settingsAnim.run(e.enabled() && e.settingsOpen ? 1f : 0f);
                float sv = (float) e.settingsAnim.getValue();
                if (sv <= 0.01f) continue;
                float[] r = settingsRect(e);
                float pivX = r[0] + r[2] / 2f;
                float pivY = r[1];
                RenderTransform.set(0.9f + 0.1f * sv, pivX, pivY, ef * sv);
                drawSettings(e, r);
                RenderTransform.set(1f, 0f, 0f, ef);
            }
            RenderTransform.reset();
        }
    }

    private final fun.photon.notify.Notification testNotif1 =
            new fun.photon.notify.Notification("Sprint", "Включено", fun.photon.notify.Notification.Type.ENABLE, 999999);
    private final fun.photon.notify.Notification testNotif2 =
            new fun.photon.notify.Notification("Ошибка", "Соединение потеряно", fun.photon.notify.Notification.Type.ERROR, 999999);

    private void drawNotifications() {
        notifRects.clear();
        if (!notifications.get()) return;
        java.util.List<fun.photon.notify.Notification> all = fun.photon.notify.NotificationManager.get();
        all.removeIf(this::notifExpired);
        boolean edit = editing();
        if (all.isEmpty() && edit) {
            all = new java.util.ArrayList<>();
            all.add(testNotif1);
            all.add(testNotif2);
        }
        if (all.isEmpty()) {
            if (edit) drawNotifSettings(Render2DUtil.screenWidth(), Render2DUtil.screenHeight());
            return;
        }

        float sc = HudElement.SCALE;
        float sw = Render2DUtil.screenWidth();
        float sh = Render2DUtil.screenHeight();

        PhotonFont title = FontUtil.medium(11 * sc);
        PhotonFont sub = FontUtil.medium(9 * sc);

        float pad = 8f * sc;
        float gap = 6f * sc;
        float icoSize = 16f * sc;
        float icoGap = 8f * sc;
        float margin = 8f * sc;

        // центр по горизонтали, заметно ниже центра экрана (ниже прицела)
        float centerY = sh * 0.6f;
        float bottomMargin = 6f * sc;
        if (editing()) bottomMargin += 16f * sc;

        float cursor = notifTopCenter ? centerY : (sh - bottomMargin);

        for (fun.photon.notify.Notification n : all) {
            float tw = Math.max(title.getWidth(n.title), sub.getWidth(n.message)) + 4f * sc;
            float textH = title.getHeight() + sub.getHeight() + 3f * sc;
            float contentH = Math.max(icoSize, textH);
            float w = pad + icoSize + icoGap + tw + pad;
            float h = pad + contentH + pad;

            float anim = edit ? 1f : notifSlide(n);
            float x;
            float y;
            if (notifTopCenter) {
                x = sw / 2f - w / 2f - (1f - anim) * 30f * sc;
                y = cursor + (1f - anim) * 14f * sc;
            } else {
                x = sw - (w + margin) * anim;
                y = cursor - h;
            }

            notifRects.add(new float[]{x, y, w, h, System.identityHashCode(n)});

            int accent = notifColor(n);

            Render2DUtil.rounded(x, y, w, h, 6 * sc, 6 * sc, 6 * sc, 6 * sc, 1f,
                    ColorUtil.rgba(18, 19, 24, 235), 0, 0f);
            Render2DUtil.roundedOutline(x, y, w, h, 6 * sc, 1f * sc, ColorUtil.rgba(255, 255, 255, 25));

            float ix = x + pad;
            float iy = y + (h - icoSize) / 2f;
            Render2DUtil.rounded(ix, iy, icoSize, icoSize, 4 * sc, 4 * sc, 4 * sc, 4 * sc, 1f,
                    ColorUtil.multAlpha(accent, 0.22f), 0, 0f);
            float icoInner = icoSize - 5f * sc;
            ImageUtil.drawSvg(notifIcon(n), ix + (icoSize - icoInner) / 2f, iy + (icoSize - icoInner) / 2f,
                    icoInner, icoInner, accent);

            float tx = ix + icoSize + icoGap;
            float ty = y + (h - textH) / 2f;
            title.drawString(n.title, tx, ty, Palette.TEXT_LIGHT);
            sub.drawString(n.message, tx, ty + title.getHeight() + 3f * sc, accent);

            if (notifProgress) {
                float prog = notifProgressFrac(n);
                Render2DUtil.rounded(x, y + h - 2f * sc, w * Math.max(0f, prog), 2f * sc, 0, 0, 0, 0, 1f,
                        ColorUtil.multAlpha(accent, 0.85f), 0, 0f);
            } else {
                Render2DUtil.rounded(x, y + h - 2f * sc, w, 2f * sc, 0, 0, 0, 0, 1f,
                        ColorUtil.multAlpha(accent, 0.4f), 0, 0f);
            }

            if (notifTopCenter) cursor += h + gap;
            else cursor -= h + gap;
        }

        if (editing()) drawNotifSettings(sw, sh);
    }

    private final java.util.List<float[]> notifRects = new java.util.ArrayList<>();

    private void drawNotifSettings(float sw, float sh) {
        notifSettingsAnim.run(notifSettingsOpen ? 1f : 0f);
        float sv = (float) notifSettingsAnim.getValue();
        if (sv <= 0.01f) return;

        float[] r = notifSettingsRect(sw, sh);
        float px = r[0], py = r[1], pw = r[2], ph = r[3];
        float pivX = px + pw / 2f, pivY = py;
        RenderTransform.set(0.9f + 0.1f * sv, pivX, pivY, sv);
        Render2DUtil.rounded(px, py, pw, ph, 6, 6, 6, 6, 1f, ColorUtil.rgba(18, 19, 24, 235), 0, 0f);
        Render2DUtil.roundedOutline(px, py, pw, ph, 6, 1f, ColorUtil.rgba(255, 255, 255, 25));

        PhotonFont f = FontUtil.medium(12);
        PhotonFont h = FontUtil.medium(12);
        float headerH = f.getHeight() + 8f;
        f.drawInRowY("Уведомления", px + 8f, py, headerH, Palette.ACCENT);

        String[] names = {"Сверху по центру", "Полоса прогресса", "Подольше"};
        boolean[] states = {notifTopCenter, notifProgress, notifLonger};
        float rh = optRowH();
        float cy = py + headerH;
        for (int i = 0; i < names.length; i++) {
            boolean on = states[i];
            f.drawInRowY(names[i], px + 8f, cy, rh, on ? Palette.TEXT_LIGHT : Palette.TEXT_MUTED);
            float tw = 20f, th = 10f;
            float tx = px + pw - 8f - tw;
            float ty = cy + (rh - th) / 2f;
            Render2DUtil.rounded(tx, ty, tw, th, th / 2f, th / 2f, th / 2f, th / 2f, 1f,
                    on ? Palette.ACCENT : ColorUtil.rgba(60, 60, 66, 255), 0, 0f);
            float kr = th - 2f;
            float kx = on ? tx + tw - kr - 1f : tx + 1f;
            Render2DUtil.rounded(kx, ty + 1f, kr, kr, kr / 2f, kr / 2f, kr / 2f, kr / 2f, 1f, -1, 0, 0f);
            cy += rh;
        }
        RenderTransform.reset();
    }

    private float[] notifSettingsRect(float sw, float sh) {
        PhotonFont f = FontUtil.medium(12);
        String[] names = {"Уведомления", "Сверху по центру", "Полоса прогресса", "Подольше"};
        float maxName = 0;
        for (String s : names) maxName = Math.max(maxName, f.getWidth(s));
        float pw = 8f + maxName + 12f + 22f + 8f;
        float headerH = f.getHeight() + 8f;
        float ph = headerH + 3 * optRowH() + 5f;
        float px, py;
        if (notifTopCenter) {
            px = sw / 2f - pw / 2f;
            py = 8f + 40f;
        } else {
            px = sw - pw - 8f;
            py = sh - 8f - 40f - ph;
        }
        px = Math.max(0, Math.min(sw - pw, px));
        py = Math.max(0, Math.min(sh - ph, py));
        return new float[]{px, py, pw, ph};
    }

    private boolean clickNotifSettings(double mx, double my) {
        if (!notifSettingsOpen) return false;
        float[] r = notifSettingsRect(Render2DUtil.screenWidth(), Render2DUtil.screenHeight());
        if (!MouseUtil.isInside(mx, my, r[0], r[1], r[2], r[3])) return false;
        float headerH = FontUtil.medium(12).getHeight() + 8f;
        float rh = optRowH();
        float cy = r[1] + headerH;
        boolean[] targets = {true, true, true};
        for (int i = 0; i < targets.length; i++) {
            if (my >= cy && my < cy + rh) {
                switch (i) {
                    case 0: notifTopCenter = !notifTopCenter; break;
                    case 1: notifProgress = !notifProgress; break;
                    case 2: notifLonger = !notifLonger; break;
                }
                savePositions();
                return true;
            }
            cy += rh;
        }
        return true;
    }

    private String notifIcon(fun.photon.notify.Notification n) {
        switch (n.type) {
            case ENABLE: return "KiCon/check_ic.svg";
            case DISABLE: return "KiCon/close_ic.svg";
            case ERROR: return "KiCon/warning_ic.svg";
            default: return "KiCon/info_ic.svg";
        }
    }

    private float notifSlide(fun.photon.notify.Notification n) {
        long age = n.age(), fade = 250;
        if (age < fade) return age / (float) fade;
        if (age > n.duration - fade) return Math.max(0f, (n.duration - age) / (float) fade);
        return 1f;
    }

    private int notifColor(fun.photon.notify.Notification n) {
        switch (n.type) {
            case ENABLE: return ColorUtil.rgba(76, 209, 130, 255);
            case DISABLE: return ColorUtil.rgba(235, 87, 79, 255);
            case ERROR: return ColorUtil.rgba(255, 159, 28, 255);
            default: return Palette.ACCENT;
        }
    }

    private float optRowH() { return FontUtil.medium(12).getHeight() + 7f; }

    private float[] settingsRect(HudElement e) {
        java.util.List<HudElement.Opt> opts = e.options();
        PhotonFont f = FontUtil.medium(12);
        float maxName = f.getWidth("Настройки");
        for (HudElement.Opt o : opts) maxName = Math.max(maxName, f.getWidth(o.name));
        float pw = 8f + maxName + 12f + 22f + 8f;
        float headerH = f.getHeight() + 8f;
        float ph = headerH + opts.size() * optRowH() + 5f;
        float px = e.getX();
        float py = e.getY() + e.height() + 4f;
        float sw = Render2DUtil.screenWidth(), sh = Render2DUtil.screenHeight();
        px = Math.max(0, Math.min(sw - pw, px));
        py = Math.max(0, Math.min(sh - ph, py));
        return new float[]{px, py, pw, ph};
    }

    private void drawSettings(HudElement e, float[] r) {
        float px = r[0], py = r[1], pw = r[2], ph = r[3];
        PhotonFont f = FontUtil.medium(12);
        Render2DUtil.rounded(px, py, pw, ph, 4, 4, 4, 4, 1f, ColorUtil.rgba(18, 19, 24, 235), 0, 0f);
        Render2DUtil.roundedOutline(px, py, pw, ph, 4, 1f, ColorUtil.rgba(255, 255, 255, 25));
        float headerH = f.getHeight() + 8f;
        f.drawInRowY("Настройки", px + 8f, py, headerH, Palette.ACCENT);

        java.util.List<HudElement.Opt> opts = e.options();
        float rh = optRowH();
        float cy = py + headerH;
        for (HudElement.Opt o : opts) {
            boolean on = o.state.getAsBoolean();
            f.drawInRowY(o.name, px + 8f, cy, rh, on ? Palette.TEXT_LIGHT : Palette.TEXT_MUTED);
            float tw = 20f, th = 10f;
            float tx = px + pw - 8f - tw;
            float ty = cy + (rh - th) / 2f;
            Render2DUtil.rounded(tx, ty, tw, th, th / 2f, th / 2f, th / 2f, th / 2f, 1f,
                    on ? Palette.ACCENT : ColorUtil.rgba(60, 60, 66, 255), 0, 0f);
            float kr = th - 2f;
            float kx = on ? tx + tw - kr - 1f : tx + 1f;
            Render2DUtil.rounded(kx, ty + 1f, kr, kr, kr / 2f, kr / 2f, kr / 2f, kr / 2f, 1f, -1, 0, 0f);
            cy += rh;
        }
    }

    private boolean handleSettingsClick(HudElement e, double mx, double my) {
        if (!e.settingsOpen || !e.hasSettings()) return false;
        float[] r = settingsRect(e);
        if (!MouseUtil.isInside(mx, my, r[0], r[1], r[2], r[3])) return false;
        float headerH = FontUtil.medium(12).getHeight() + 8f;
        float rh = optRowH();
        float cy = r[1] + headerH;
        for (HudElement.Opt o : e.options()) {
            if (my >= cy && my < cy + rh) {
                o.toggle.run();
                savePositions();
                break;
            }
            cy += rh;
        }
        return true;
    }

    private void updateDrag(float sw, float sh) {
        if (dragging == null) return;
        double mouseX = MouseUtil.getX(), mouseY = MouseUtil.getY();
        float w = dragging.width();
        float h = dragging.height();
        float nx = (float) mouseX - offX;
        float ny = (float) mouseY - offY;

        snapX = Math.abs((nx + w / 2f) - sw / 2f) < SNAP;
        snapY = Math.abs((ny + h / 2f) - sh / 2f) < SNAP;
        if (snapX) nx = sw / 2f - w / 2f;
        if (snapY) ny = sh / 2f - h / 2f;

        nx = Math.max(0, Math.min(sw - w, nx));
        ny = Math.max(0, Math.min(sh - h, ny));
        dragging.setPosition(nx, ny);
    }

    private void grid(float sw, float sh) {
        int step = 40;
        int line = ColorUtil.rgba(255, 255, 255, 12);
        for (float gx = 0; gx <= sw; gx += step) Render2DUtil.rect(gx, 0, 1f, sh, line);
        for (float gy = 0; gy <= sh; gy += step) Render2DUtil.rect(0, gy, sw, 1f, line);
    }

    private void box(HudElement e) {
        float w = e.width();
        float h = e.height();
        if (w <= 0 || h <= 0) return;
        boolean hov = dragging == e || MouseUtil.isInside(MouseUtil.getX(), MouseUtil.getY(), e.getX(), e.getY(), w, h);
        int col = hov ? Palette.ACCENT : ColorUtil.rgba(255, 255, 255, 60);
        Render2DUtil.roundedOutline(e.getX() - 1, e.getY() - 1, w + 2, h + 2, 4, 1f, col);
        if (hov) FontUtil.medium(11).drawString(e.name, e.getX(), e.getY() - 13, Palette.ACCENT);
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onClick(EventClick event) {
        if (!editing()) return;

        if (event.isReleased()) {
            if (dragging != null) {
                dragging = null;
                snapX = snapY = false;
                savePositions();
                event.cancel();
            }
            return;
        }
        if (!event.isPressed()) return;

        double mouseX = MouseUtil.getX(), mouseY = MouseUtil.getY();

        if (clickNotifSettings(mouseX, mouseY)) {
            event.cancel();
            return;
        }

        if (event.isRight()) {
            for (float[] r : notifRects) {
                if (MouseUtil.isInside(mouseX, mouseY, r[0], r[1], r[2], r[3])) {
                    notifSettingsOpen = !notifSettingsOpen;
                    event.cancel();
                    return;
                }
            }
            if (notifSettingsOpen) notifSettingsOpen = false;
        }

        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement e = elements.get(i);
            if (!e.enabled() || !e.settingsOpen) continue;
            if (handleSettingsClick(e, mouseX, mouseY)) {
                event.cancel();
                return;
            }
        }

        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement e = elements.get(i);
            if (!e.enabled()) continue;
            if (MouseUtil.isInside(mouseX, mouseY, e.getX(), e.getY(), e.width(), e.height())) {
                if (event.isRight()) {
                    if (e.hasSettings()) e.settingsOpen = !e.settingsOpen;
                    else e.onRightClick();
                } else {
                    dragging = e;
                    offX = (float) mouseX - e.getX();
                    offY = (float) mouseY - e.getY();
                }
                event.cancel();
                return;
            }
        }

        for (HudElement e : elements) e.settingsOpen = false;
    }
}
