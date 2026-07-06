package fun.photon.clickgui.component;

import fun.photon.Photon;
import fun.photon.clickgui.Palette;
import fun.photon.config.ConfigManager;
import fun.photon.utils.KeyUtil;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.ScissorUtil;
import fun.photon.utils.render.glfw.Cursors;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ConfigPanel {

    private static final String ICON_DEL = "KiCon/close.svg";
    private static final String ICON_FOLDER = "KiCon/folder.svg";
    private static final String ICON_CFG = "KiCon/doc.svg";

    private static final float BTN_W = 110f, BTN_H = 30f;
    private static final float PANEL_W = 230f, PANEL_H = 320f;
    private static final float PAD = 12f, ROUND = 14f;
    private static final float ROW = 28f, FOOTER = 44f;

    private float btnX, btnY;
    private boolean open, prevOpen;
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 240);
    private final ThemeParticles particles = new ThemeParticles();

    private float scroll, animatedScroll, contentHeight, listTop, listBottom;
    private String input = "";

    private static final String ICON_STAR = "KiCon/star.svg";
    private static final int GOLD = ColorUtil.rgba(255, 200, 60, 255);

    private String dragName;
    private float dragStartX, dragOffset;
    private final java.util.Map<String, Animation> alive = new java.util.HashMap<>();
    private final java.util.Set<String> deleting = new java.util.HashSet<>();
    private final java.util.Map<String, Float> deleteDir = new java.util.HashMap<>();

    private float screenSlideX;

    public void setAnchor(float screenW, float screenH) {
        this.btnX = screenW - BTN_W - 12f;
        this.btnY = screenH - BTN_H - 12f;
    }

    public void setScreenSlide(float s) {
        this.screenSlideX = s * (PANEL_W + 80f);
    }

    private float panelX() {
        return btnX + BTN_W - PANEL_W;
    }

    private float panelY() {
        return btnY - PANEL_H - 8f;
    }

    public boolean isInside(double mx, double my) {
        if (inside(mx, my, btnX, btnY, BTN_W, BTN_H)) return true;
        return open && inside(mx, my, panelX(), panelY(), PANEL_W, PANEL_H);
    }

    public void render(double mouseX, double mouseY) {
        btnX += screenSlideX;
        if (open != prevOpen) {
            openAnim.reset();
            particles.burst(btnX + BTN_W / 2f, btnY + BTN_H / 2f, open ? 34 : 26, open ? 1f : -1f);
            prevOpen = open;
        }
        openAnim.run(open ? 1f : 0f);
        float t = (float) openAnim.getValue();

        boolean btnHov = inside(mouseX, mouseY, btnX, btnY, BTN_W, BTN_H);
        int btnFill = open ? Palette.ACCENT : ColorUtil.rgba(20, 20, 26, 220);
        if (btnHov) btnFill = ColorUtil.brighten(btnFill, 18);
        Render2DUtil.roundedRect(btnX, btnY, BTN_W, BTN_H, 8f, btnFill);
        Render2DUtil.roundedOutline(btnX, btnY, BTN_W, BTN_H, 8f, 1f,
                open ? ColorUtil.rgba(255, 255, 255, 120) : ColorUtil.rgba(255, 255, 255, 50));
        int btnText = ColorUtil.rgba(245, 245, 250, 255);
        FontUtil.medium(14).drawInRowY("Configs", btnX + 12, btnY, BTN_H, btnText);
        FontUtil.mitr(18).drawRightInRow(open ? "-" : "+", btnX + BTN_W - 12, btnY, BTN_H, btnText);
        if (btnHov) Cursors.set(Cursors.hand());

        if (t > 0.001f) renderPanel(mouseX, mouseY, t);
        particles.render();
    }

    private void renderPanel(double mouseX, double mouseY, float t) {
        float px = panelX();
        float py = panelY() + (1f - t) * 10f;
        float fa = t;
        px += (1f - t) * 24f;
        boolean interactive = t > 0.85f;

        Render2DUtil.roundedRect(px, py, PANEL_W, PANEL_H, ROUND,
                ColorUtil.multAlpha(ColorUtil.rgba(14, 15, 20, 240), fa));
        Render2DUtil.roundedOutline(px, py, PANEL_W, PANEL_H, ROUND, 1f,
                ColorUtil.multAlpha(Palette.ACCENT, fa * 0.6f));

        FontUtil.mitr(19).drawInRowY("Configs", px + PAD, py + 2, 36,
                ColorUtil.multAlpha(ColorUtil.rgba(245, 245, 250, 255), fa));

        listTop = py + 40;
        listBottom = py + PANEL_H - FOOTER;
        float visible = listBottom - listTop;

        Render2DUtil.roundedRect(px + PAD - 2, listTop - 4, PANEL_W - PAD * 2 + 4, visible + 8, 8f,
                ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 150), fa));

        animatedScroll += (scroll - animatedScroll) * 0.2f;
        if (dragName != null) dragOffset = (float) (mouseX - dragStartX);

        ConfigManager cfg = Photon.getInstance().getConfigManager();
        List<String> configs = cfg.listConfigs();
        float rowW = PANEL_W - PAD * 2;
        float delThresh = rowW * 0.4f;

        ScissorUtil.scissor(px + PAD - 2, listTop, rowW + 4, visible);
        PhotonFont rf = FontUtil.medium(15);
        float yy = listTop + animatedScroll;
        for (String name : configs) {
            Animation al = alive.computeIfAbsent(name, k -> {
                Animation a = new Animation(Easing.EASE_OUT_CUBIC, 260);
                a.setValue(1);
                return a;
            });
            boolean del = deleting.contains(name);
            al.run(del ? 0d : 1d);
            float a = clamp((float) al.getValue(), 0f, 1f);
            if (del && a < 0.03f) {
                cfg.delete(name);
                alive.remove(name);
                deleting.remove(name);
                deleteDir.remove(name);
                continue;
            }

            boolean starred = cfg.isConfigStarred(name);
            float off = name.equals(dragName) ? dragOffset : 0f;
            if (del) off = deleteDir.getOrDefault(name, 1f) * (1f - a) * (rowW * 0.7f);
            float rowAlpha = a * fa;
            float rh = ROW * a;
            float rowH = ROW - 4;

            float rxp = px + PAD + off;

            boolean swiping = name.equals(dragName) && Math.abs(off) > 4f;
            int rowBg = (swiping && Math.abs(off) > delThresh && !starred)
                    ? ColorUtil.rgba(150, 50, 50, 120)
                    : ColorUtil.rgba(255, 255, 255, 18);
            boolean hov = interactive && dragName == null
                    && inside(mouseX, mouseY, px + PAD, yy, rowW, rowH);
            if (hov) rowBg = ColorUtil.rgba(255, 255, 255, 34);
            Render2DUtil.roundedRect(rxp, yy, rowW, rowH, 7f, ColorUtil.multAlpha(rowBg, rowAlpha));

            float stX = rxp + 6, stY = yy + rowH / 2f - 8f;
            ImageUtil.drawSvg(ICON_STAR, stX, stY, 16, 16,
                    ColorUtil.multAlpha(starred ? GOLD : ColorUtil.rgba(95, 100, 115, 255), rowAlpha));

            float icY = yy + rowH / 2f - 7f;
            ImageUtil.drawSvg(ICON_CFG, rxp + 28, icY, 14, 14,
                    ColorUtil.multAlpha(ColorUtil.rgba(170, 180, 205, 255), rowAlpha));

            rf.drawInRowY(name, rxp + 48, yy, rowH,
                    ColorUtil.multAlpha(starred ? GOLD : ColorUtil.rgba(238, 240, 248, 255), rowAlpha));

            if (hov) Cursors.set(Cursors.hand());
            yy += rh;
        }
        contentHeight = yy - (listTop + animatedScroll);
        ScissorUtil.disable();

        float maxScroll = Math.max(0f, contentHeight - visible);
        scroll = clamp(scroll, -maxScroll, 0f);

        if (configs.isEmpty()) {
            FontUtil.medium(13).drawInRowY("нет конфигов", px + PAD + 8, listTop, ROW,
                    ColorUtil.multAlpha(ColorUtil.rgba(140, 145, 160, 255), fa));
        }

        float fy = py + PANEL_H - FOOTER + 8f;
        float inner = PANEL_W - PAD * 2;
        float folderW = 28f, saveW = 64f, gap = 6f;
        float folderX = px + PAD;
        float fieldX = folderX + folderW + gap;
        float fieldW = inner - folderW - saveW - gap * 2;
        float bx = fieldX + fieldW + gap;

        boolean folderHov = interactive && inside(mouseX, mouseY, folderX, fy, folderW, 28f);
        Render2DUtil.roundedRect(folderX, fy, folderW, 28f, 7f, ColorUtil.multAlpha(
                folderHov ? ColorUtil.rgba(50, 56, 74, 235) : ColorUtil.rgba(28, 32, 42, 230), fa));
        ImageUtil.drawSvg(ICON_FOLDER, folderX + 7, fy + 7, 14, 14,
                ColorUtil.multAlpha(ColorUtil.rgba(220, 200, 130, 255), fa));
        if (folderHov) Cursors.set(Cursors.hand());

        Render2DUtil.roundedRect(fieldX, fy, fieldW, 28f, 7f,
                ColorUtil.multAlpha(ColorUtil.rgba(24, 28, 38, 235), fa));
        Render2DUtil.roundedOutline(fieldX, fy, fieldW, 28f, 7f, 1f,
                ColorUtil.multAlpha(ColorUtil.rgba(90, 120, 210, 140), fa));
        String shown = input.isEmpty() ? "имя…" : input + "_";
        int tc = input.isEmpty() ? ColorUtil.rgba(110, 120, 140, 255) : ColorUtil.rgba(235, 238, 250, 255);
        FontUtil.medium(14).drawInRowY(shown, fieldX + 8, fy, 28f, ColorUtil.multAlpha(tc, fa));
        if (!input.isEmpty()) {
            float clrX = fieldX + fieldW - 20, clrY = fy + 6;
            boolean clrHov = interactive && inside(mouseX, mouseY, clrX, clrY, 16, 16);
            ImageUtil.drawSvg(ICON_DEL, clrX + 4, clrY + 4, 8, 8, ColorUtil.multAlpha(
                    clrHov ? ColorUtil.rgba(255, 160, 160, 255) : ColorUtil.rgba(140, 145, 160, 255), fa));
            if (clrHov) Cursors.set(Cursors.hand());
        }

        boolean addHov = interactive && inside(mouseX, mouseY, bx, fy, saveW, 28f);
        Render2DUtil.roundedRect(bx, fy, saveW, 28f, 7f, ColorUtil.multAlpha(
                addHov ? ColorUtil.rgba(60, 90, 200, 235) : ColorUtil.rgba(40, 60, 140, 220), fa));
        FontUtil.medium(14).drawCenteredInRow("Save", bx + saveW / 2f, fy, 28f,
                ColorUtil.multAlpha(ColorUtil.rgba(235, 238, 250, 255), fa));
        if (addHov) Cursors.set(Cursors.hand());
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inside(mouseX, mouseY, btnX, btnY, BTN_W, BTN_H)) {
            open = !open;
            return;
        }
        if (!open || openAnim.getValue() < 0.6f || button != 0) return;

        float px = panelX(), py = panelY();
        ConfigManager cfg = Photon.getInstance().getConfigManager();
        float rowW = PANEL_W - PAD * 2;

        List<String> configs = cfg.listConfigs();
        float yy = listTop + animatedScroll;
        for (String name : configs) {
            if (deleting.contains(name)) { yy += ROW; continue; }
            float rowH = ROW - 4;
            if (yy + rowH > listTop && yy < listBottom) {
                float stX = px + PAD + 6, stY = yy + rowH / 2f - 8f;
                if (inside(mouseX, mouseY, stX, stY, 16, 16)) {
                    cfg.toggleConfigStar(name);
                    return;
                }
                if (inside(mouseX, mouseY, px + PAD, yy, rowW, rowH)) {
                    dragName = name;
                    dragStartX = (float) mouseX;
                    dragOffset = 0f;
                    return;
                }
            }
            yy += ROW;
        }

        float fy = py + PANEL_H - FOOTER + 8f;
        float inner = PANEL_W - PAD * 2;
        float folderW = 28f, saveW = 64f, gap = 6f;
        float folderX = px + PAD;
        float fieldX = folderX + folderW + gap;
        float fieldW = inner - folderW - saveW - gap * 2;
        float bx = fieldX + fieldW + gap;

        if (inside(mouseX, mouseY, folderX, fy, folderW, 28f)) {
            openFolder();
            return;
        }
        if (!input.isEmpty() && inside(mouseX, mouseY, fieldX + fieldW - 20, fy + 6, 16, 16)) {
            input = "";
            return;
        }
        if (inside(mouseX, mouseY, bx, fy, saveW, 28f)) {
            saveCurrent();
        }
    }

    private void openFolder() {
        try {
            net.minecraft.util.Util.getPlatform().openPath(Photon.MODULE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void saveCurrent() {
        String name = input.trim();
        if (name.length() < 1 || name.length() > 24) return;
        Photon.getInstance().getConfigManager().save(name);
        input = "";
    }

    public void keyPressed(int key) {
        if (!open) return;
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            saveCurrent();
            return;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
            return;
        }
        if (key == GLFW.GLFW_KEY_MINUS) {
            if (input.length() < 24) input += "_";
            return;
        }
        String n = KeyUtil.getName(key);
        if (n != null && n.length() == 1) {
            char c = n.charAt(0);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                if (input.length() < 24) input += Character.toLowerCase(c);
            }
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void paste(String s) {
        if (!open || s == null) return;
        for (int i = 0; i < s.length() && input.length() < 24; i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
                input += Character.toLowerCase(c);
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragName == null) return;
        String name = dragName;
        float off = (float) (mouseX - dragStartX);
        float rowW = PANEL_W - PAD * 2;
        ConfigManager cfg = Photon.getInstance().getConfigManager();

        if (!cfg.isConfigStarred(name) && Math.abs(off) > rowW * 0.4f) {

            deleting.add(name);
            deleteDir.put(name, off >= 0 ? 1f : -1f);
            particles.burst((float) mouseX, (float) mouseY, 24, 1f);
        } else if (Math.abs(off) < 6f) {

            cfg.load(name);
        }
        dragName = null;
        dragOffset = 0f;
    }

    public void mouseScrolled(double delta) {
        if (!open) return;
        float maxScroll = Math.max(0f, contentHeight - (listBottom - listTop));
        scroll = clamp(scroll + (float) (delta * 26), -maxScroll, 0f);
    }

    public void onClose() {
        scroll = 0;
        animatedScroll = 0;
        input = "";
        dragName = null;
        dragOffset = 0f;
        deleting.clear();
        deleteDir.clear();
        alive.clear();
        particles.clear();
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
