package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.clickgui.theme.Theme;
import fun.photon.clickgui.theme.ThemeManager;
import fun.photon.clickgui.theme.ThemeSlot;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.ScissorUtil;
import fun.photon.utils.render.glfw.Cursors;

import java.util.ArrayList;
import java.util.List;

public class ThemePanel {

    private static final float BTN_W = 110f, BTN_H = 30f;
    private static final float GAP = 14f;

    private static final float PANEL_W = 230f, PANEL_H = 410f;
    private static final float PAD = 12f, ROUND = 14f;
    private static final float THEME_ROW = 28f;

    private float btnX, btnY;
    private boolean open;
    private boolean prevOpen;
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 240);
    private final ThemeParticles particles = new ThemeParticles();

    private float scroll, animatedScroll, contentHeight;
    private float colorTop, colorBottom;

    private static final float EDITOR_W = 230f, EDITOR_H = 380f;
    private boolean editorOpen;
    private final Animation editorAnim = new Animation(Easing.EASE_OUT_CUBIC, 220);
    private String nameBuf = "";
    private boolean nameFocused;
    private double lastMx, lastMy;

    private final List<ColorRow> rows = new ArrayList<>();

    public ThemePanel() {
        for (ThemeSlot s : ThemeSlot.values()) rows.add(new ColorRow(s));
    }

    private boolean isOpen() {
        return open;
    }

    private void setOpen(boolean value) {
        this.open = value;
    }

    private float screenSlideX;

    public void setAnchor(float rightX, float topY) {
        float screenW = fun.photon.utils.render.Render2DUtil.screenWidth();
        this.btnX = screenW - BTN_W - 12f;
        this.btnY = 12f;
    }

    public void setScreenSlide(float s) {
        this.screenSlideX = s * (PANEL_W + EDITOR_W + 80f);
    }

    private float panelX() {

        return btnX + BTN_W - PANEL_W;
    }

    private float panelY() {
        return btnY + BTN_H + 8f;
    }

    private float editorX() {
        float ex = panelX() - EDITOR_W - 12f;
        return Math.max(6f, ex);
    }

    private float editorY() {
        return panelY();
    }

    public boolean isInside(double mx, double my) {
        if (inside(mx, my, btnX, btnY, BTN_W, BTN_H)) return true;
        if (isOpen() && inside(mx, my, panelX(), panelY(), PANEL_W, PANEL_H)) return true;
        return editorOpen && inside(mx, my, editorX(), editorY(), EDITOR_W, EDITOR_H);
    }

    public void render(double mouseX, double mouseY) {
        btnX += screenSlideX;
        lastMx = mouseX;
        lastMy = mouseY;
        boolean open = isOpen();

        if (open != prevOpen) {
            openAnim.reset();
            float cx = btnX + BTN_W / 2f;
            float cy = btnY + BTN_H / 2f;
            particles.burst(cx, cy, open ? 34 : 26, open ? 1f : -1f);
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
        FontUtil.medium(14).drawInRowY("Themes", btnX + 12, btnY, BTN_H, btnText);

        float chip = 18f, chipX = btnX + BTN_W - chip - 8f, chipY = btnY + (BTN_H - chip) / 2f;
        Render2DUtil.roundedRect(chipX, chipY, chip, chip, 5f,
                open ? ColorUtil.rgba(255, 255, 255, 60) : ColorUtil.rgba(0, 0, 0, 90));
        FontUtil.mitr(20).drawCenteredInRow(open ? "−" : "+", chipX + chip / 2f, chipY - 1, chip, btnText);
        if (btnHov) Cursors.set(Cursors.hand());

        if (t > 0.001f) {
            renderPanel(mouseX, mouseY, t);
        }

        if (editorOpen && !open) editorOpen = false;
        editorAnim.run(editorOpen ? 1f : 0f);
        float et = (float) editorAnim.getValue();
        if (et > 0.001f) {
            renderEditor(mouseX, mouseY, et);
        }

        particles.render();
    }

    private void renderPanel(double mouseX, double mouseY, float t) {
        ThemeManager tm = ThemeManager.get();
        float px = panelX();

        float py = panelY() - (1f - t) * 10f;
        float fa = t;
        px += (1f - t) * 24f;

        boolean interactive = t > 0.85f;

        Render2DUtil.roundedRect(px, py, PANEL_W, PANEL_H, ROUND,
                ColorUtil.multAlpha(ColorUtil.rgba(14, 15, 20, 240), fa));
        Render2DUtil.roundedOutline(px, py, PANEL_W, PANEL_H, ROUND, 1f,
                ColorUtil.multAlpha(Palette.ACCENT, fa * 0.6f));

        PhotonFont hf = FontUtil.mitr(19);
        hf.drawInRowY("Themes", px + PAD, py + 2, 36, ColorUtil.multAlpha(ColorUtil.rgba(245, 245, 250, 255), fa));

        float nW = 50f, nH = 22f, nX = px + PANEL_W - PAD - nW, nY = py + 8;
        boolean nHov = interactive && inside(mouseX, mouseY, nX, nY, nW, nH);
        Render2DUtil.roundedRect(nX, nY, nW, nH, 6f, ColorUtil.multAlpha(
                nHov ? Palette.ACCENT : ColorUtil.rgba(40, 44, 58, 230), fa));
        FontUtil.medium(12).drawCenteredInRow("+ New", nX + nW / 2f, nY, nH,
                ColorUtil.multAlpha(ColorUtil.rgba(238, 240, 248, 255), fa));
        if (nHov) Cursors.set(Cursors.hand());

        colorTop = py + 40;
        colorBottom = py + PANEL_H - PAD;
        float visible = colorBottom - colorTop;
        animatedScroll += (scroll - animatedScroll) * 0.2f;

        ScissorUtil.scissor(px, colorTop, PANEL_W, visible);
        PhotonFont rf = FontUtil.medium(15);
        float ty = colorTop + animatedScroll;
        for (Theme th : tm.getThemes()) {
            boolean sel = th == tm.getActive();
            boolean hov = interactive && inside(mouseX, mouseY, px + PAD, ty, PANEL_W - PAD * 2, THEME_ROW - 4);
            int bg = sel ? Palette.ACCENT : (hov ? ColorUtil.rgba(255, 255, 255, 30) : 0);
            if (((bg >> 24) & 0xFF) != 0) {
                Render2DUtil.roundedRect(px + PAD, ty, PANEL_W - PAD * 2, THEME_ROW - 4, 7f,
                        ColorUtil.multAlpha(bg, fa));
            }
            int tc = ColorUtil.multAlpha(ColorUtil.rgba(238, 240, 248, 255), fa);
            rf.drawInRowY(th.getName(), px + PAD + 8, ty, THEME_ROW - 4, tc);
            if (th.isEditable()) {

                float ex = px + PANEL_W - PAD - 38, ey2 = ty + (THEME_ROW - 4) / 2f - 7f;
                boolean eHov = interactive && inside(mouseX, mouseY, ex, ey2, 14, 14);
                ImageUtil.drawSvg("KiCon/pensil.svg", ex, ey2, 14, 14, ColorUtil.multAlpha(
                        eHov ? Palette.ACCENT : ColorUtil.rgba(170, 178, 200, 255), fa));
                float dx = px + PANEL_W - PAD - 16, dy = ey2;
                boolean delHov = interactive && inside(mouseX, mouseY, dx, dy, 14, 14);
                ImageUtil.drawSvg("KiCon/close.svg", dx + 3, dy + 3, 8, 8, ColorUtil.multAlpha(
                        delHov ? ColorUtil.rgba(255, 150, 150, 255) : ColorUtil.rgba(150, 120, 130, 255), fa));
            }
            if (hov) Cursors.set(Cursors.hand());
            ty += THEME_ROW;
        }
        contentHeight = ty - (colorTop + animatedScroll);
        ScissorUtil.disable();

        float maxScroll = Math.max(0f, contentHeight - visible);
        scroll = clamp(scroll, -maxScroll, 0f);
    }

    private float edScroll, edAnimScroll, edContentH, edTop, edBottom;

    private void renderEditor(double mouseX, double mouseY, float t) {
        ThemeManager tm = ThemeManager.get();
        Theme th = tm.getActive();
        if (!th.isEditable()) { editorOpen = false; return; }

        float ex = editorX() - (1f - t) * 24f;
        float ey = editorY() - (1f - t) * 10f;
        float fa = t;
        boolean interactive = t > 0.85f;

        Render2DUtil.roundedRect(ex, ey, EDITOR_W, EDITOR_H, ROUND,
                ColorUtil.multAlpha(ColorUtil.rgba(12, 13, 18, 244), fa));
        Render2DUtil.roundedOutline(ex, ey, EDITOR_W, EDITOR_H, ROUND, 1f,
                ColorUtil.multAlpha(Palette.ACCENT, fa * 0.7f));

        float clX = ex + EDITOR_W - PAD - 14, clY = ey + 10;
        boolean clHov = interactive && inside(mouseX, mouseY, clX, clY, 16, 16);
        ImageUtil.drawSvg("KiCon/close.svg", clX + 3, clY + 3, 10, 10, ColorUtil.multAlpha(
                clHov ? ColorUtil.rgba(255, 160, 160, 255) : ColorUtil.rgba(190, 195, 210, 255), fa));
        if (clHov) Cursors.set(Cursors.hand());

        float nY = ey + 8, nH = 24f, nW = EDITOR_W - PAD * 2 - 24;
        Render2DUtil.roundedRect(ex + PAD, nY, nW, nH, 6f,
                ColorUtil.multAlpha(ColorUtil.rgba(24, 28, 38, 235), fa));
        Render2DUtil.roundedOutline(ex + PAD, nY, nW, nH, 6f, 1f, ColorUtil.multAlpha(
                nameFocused ? ColorUtil.rgba(110, 150, 255, 200) : ColorUtil.rgba(90, 120, 210, 130), fa));
        String shown = nameBuf + (nameFocused ? "_" : "");
        FontUtil.medium(14).drawInRowY(shown, ex + PAD + 8, nY, nH,
                ColorUtil.multAlpha(ColorUtil.rgba(235, 238, 250, 255), fa));

        edTop = ey + 40;
        edBottom = ey + EDITOR_H - PAD;
        float visible = edBottom - edTop;
        Render2DUtil.roundedRect(ex + PAD - 2, edTop - 4, EDITOR_W - PAD * 2 + 4, visible + 8, 8f,
                ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 150), fa));

        edAnimScroll += (edScroll - edAnimScroll) * 0.2f;
        ScissorUtil.scissor(ex, edTop, EDITOR_W, visible);
        float start = edTop + edAnimScroll;
        float yy = start;
        for (ColorRow row : rows) {
            row.setLayout(ex + PAD, yy, EDITOR_W - PAD * 2, true, fa, interactive);
            row.render(mouseX, mouseY);
            yy += row.getHeight();
        }
        edContentH = yy - start;
        ScissorUtil.disable();

        float maxScroll = Math.max(0f, edContentH - visible);
        edScroll = clamp(edScroll, -maxScroll, 0f);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inside(mouseX, mouseY, btnX, btnY, BTN_W, BTN_H)) {
            toggle();
            return;
        }
        if (button != 0) return;

        ThemeManager tm = ThemeManager.get();
        fun.photon.config.ConfigManager cfg = fun.photon.Photon.getInstance().getConfigManager();

        if (editorOpen && editorAnim.getValue() > 0.6f) {
            float ex = editorX(), ey = editorY();
            float clX = ex + EDITOR_W - PAD - 14, clY = ey + 10;
            if (inside(mouseX, mouseY, clX, clY, 16, 16)) { closeEditor(); return; }

            float nY = ey + 8, nH = 24f, nW = EDITOR_W - PAD * 2 - 24;
            if (inside(mouseX, mouseY, ex + PAD, nY, nW, nH)) { nameFocused = true; return; }

            if (inside(mouseX, mouseY, ex, ey, EDITOR_W, EDITOR_H)) {
                nameFocused = false;
                for (ColorRow row : rows) row.mouseClicked(mouseX, mouseY, button);
                return;
            }
        }

        if (!isOpen() || openAnim.getValue() < 0.6f) return;

        float px = panelX(), py = panelY();

        float nW2 = 50f, nH2 = 22f, nX = px + PANEL_W - PAD - nW2, nY2 = py + 8;
        if (inside(mouseX, mouseY, nX, nY2, nW2, nH2)) {
            Theme t = tm.createCustom();
            tm.select(t);
            cfg.saveThemes();
            openEditor(t);
            return;
        }

        float ty = colorTop + animatedScroll;
        for (Theme th : new java.util.ArrayList<>(tm.getThemes())) {
            if (ty + (THEME_ROW - 4) > colorTop && ty < colorBottom) {
                if (th.isEditable()) {
                    float exb = px + PANEL_W - PAD - 38, eyb = ty + (THEME_ROW - 4) / 2f - 7f;
                    if (inside(mouseX, mouseY, exb, eyb, 14, 14)) {
                        tm.select(th); cfg.saveThemes(); openEditor(th);
                        return;
                    }
                    float dx = px + PANEL_W - PAD - 16;
                    if (inside(mouseX, mouseY, dx, eyb, 14, 14)) {
                        String nm = th.getName();
                        if (tm.remove(th)) {
                            cfg.deleteThemeFile(nm);
                            cfg.saveThemes();
                            if (editorOpen && !tm.getActive().isEditable()) closeEditor();
                        }
                        return;
                    }
                }
                if (inside(mouseX, mouseY, px + PAD, ty, PANEL_W - PAD * 2, THEME_ROW - 4)) {
                    tm.select(th);
                    cfg.saveThemes();
                    return;
                }
            }
            ty += THEME_ROW;
        }
    }

    private void openEditor(Theme t) {
        editorOpen = true;
        nameFocused = false;
        nameBuf = t.getName();
        edScroll = 0;
        edAnimScroll = 0;
        for (ColorRow row : rows) row.collapse();
    }

    private void closeEditor() {
        applyRename();
        editorOpen = false;
        nameFocused = false;
    }

    private void applyRename() {
        ThemeManager tm = ThemeManager.get();
        Theme th = tm.getActive();
        if (th == null || !th.isEditable()) return;
        String want = nameBuf.trim();
        if (want.isEmpty() || want.equals(th.getName())) return;
        String old = th.getName();
        String n = want;
        int i = 2;
        while (tm.findByName(n) != null && tm.findByName(n) != th) n = want + " " + (i++);
        th.setName(n);
        fun.photon.config.ConfigManager cfg = fun.photon.Photon.getInstance().getConfigManager();
        cfg.deleteThemeFile(old);
        cfg.saveThemes();
    }

    public boolean isNameFocused() {
        return editorOpen && nameFocused;
    }

    public void keyPressed(int key) {
        if (!isNameFocused()) return;
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || key == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            applyRename();
            nameFocused = false;
            return;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            if (!nameBuf.isEmpty()) nameBuf = nameBuf.substring(0, nameBuf.length() - 1);
            return;
        }
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
            if (nameBuf.length() < 18) nameBuf += " ";
            return;
        }
        String n = fun.photon.utils.KeyUtil.getName(key);
        if (n != null && n.length() == 1) {
            char c = n.charAt(0);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                if (nameBuf.length() < 18) nameBuf += c;
            }
        }
    }

    public void paste(String s) {
        if (!isNameFocused() || s == null) return;
        for (int i = 0; i < s.length() && nameBuf.length() < 18; i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '_') {
                nameBuf += c;
            }
        }
    }

    private void toggle() {

        setOpen(!isOpen());
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        boolean changed = false;
        for (ColorRow row : rows) changed |= row.mouseReleased();
        if (changed) fun.photon.Photon.getInstance().getConfigManager().saveThemes();
    }

    public void mouseScrolled(double delta) {

        if (editorOpen && inside(lastMx, lastMy, editorX(), editorY(), EDITOR_W, EDITOR_H)) {
            float maxScroll = Math.max(0f, edContentH - (edBottom - edTop));
            edScroll = clamp(edScroll + (float) (delta * 26), -maxScroll, 0f);
            return;
        }
        if (!isOpen()) return;
        float maxScroll = Math.max(0f, contentHeight - (colorBottom - colorTop));
        scroll = clamp(scroll + (float) (delta * 26), -maxScroll, 0f);
    }

    public void onClose() {
        scroll = 0;
        animatedScroll = 0;
        edScroll = 0;
        edAnimScroll = 0;
        editorOpen = false;
        nameBuf = "";
        nameFocused = false;
        particles.clear();
        for (ColorRow row : rows) row.mouseReleased();
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static final class ColorRow {

        private static final float NAME_ROW = 26;
        private static final float SW = 30, SH = 15;

        private static final float SQ_H = 74;
        private static final float HUE_W = 12;
        private static final float ALPHA_H = 12;
        private static final float GAP = 6;
        private static final int SV_COLS = 14, SV_ROWS = 8;
        private static final int HUE_SEG = 16, ALPHA_SEG = 14;

        private static final int G_SV = 0, G_HUE = 1, G_ALPHA = 2;

        private final ThemeSlot slot;
        private float x, y, width, height = NAME_ROW;
        private boolean expanded, editable, interactive;
        private float fade = 1f;
        private int grabbed = -1;

        private float heldHue;

        ColorRow(ThemeSlot slot) {
            this.slot = slot;
        }

        void setLayout(float x, float y, float width, boolean editable, float fade, boolean interactive) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.editable = editable;
            this.fade = fade;
            this.interactive = interactive;
            if (!editable) expanded = false;
        }

        float getHeight() {
            return height;
        }

        private int value() {
            return ThemeManager.get().getActive().get(slot);
        }

        private void value(int c) {
            ThemeManager.get().getActive().set(slot, c);
            ThemeManager.get().reapply();
        }

        private float[] hsv() {
            int c = value();
            float[] hsb = java.awt.Color.RGBtoHSB(ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), null);
            if (hsb[1] > 0.0001f && hsb[2] > 0.0001f) heldHue = hsb[0];
            return hsb;
        }

        private static int hsvColor(float h, float s, float v, int a) {
            int rgb = java.awt.Color.HSBtoRGB(h, s, v);
            return ColorUtil.rgba((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, a);
        }

        private void setHsv(float h, float s, float v) {
            int a = ColorUtil.alpha(value());
            value(hsvColor(h, s, v, a));
        }

        private void setAlpha(float frac) {
            int a = Math.max(0, Math.min(255, Math.round(frac * 255)));
            value(ColorUtil.withAlpha(value(), a));
        }

        void render(double mouseX, double mouseY) {
            int white = ColorUtil.multAlpha(ColorUtil.rgba(235, 238, 248, 255), fade);

            FontUtil.medium(13).drawInRowY(slot.label, x, y, NAME_ROW, white);

            float sx = x + width - SW;
            float sy = y + (NAME_ROW - SH) / 2f;
            drawChecker(sx, sy, SW, SH, 5f, fade);
            Render2DUtil.roundedRect(sx, sy, SW, SH, 4f, ColorUtil.multAlpha(value(), fade));
            Render2DUtil.roundedOutline(sx, sy, SW, SH, 4f, 1f, ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 255), fade));
            if (editable && interactive && inside(mouseX, mouseY, sx, sy, SW, SH)) Cursors.set(Cursors.hand());

            float total = NAME_ROW;
            if (expanded) {
                float[] hsb = hsv();
                float hue = heldHue, sat = hsb[1], val = hsb[2];
                float alpha = ColorUtil.alpha(value()) / 255f;

                float topY = y + NAME_ROW + 2;
                float sqX = x, sqY = topY;
                float sqW = width - HUE_W - GAP;
                float hueX = x + width - HUE_W, hueY = topY;
                float alphaX = x, alphaY = topY + SQ_H + GAP, alphaW = width;

                if (grabbed == G_SV) {
                    float ns = clamp((float) ((mouseX - sqX) / sqW), 0f, 1f);
                    float nv = clamp(1f - (float) ((mouseY - sqY) / SQ_H), 0f, 1f);
                    setHsv(hue, ns, nv);
                    sat = ns; val = nv;
                } else if (grabbed == G_HUE) {
                    float nh = clamp((float) ((mouseY - hueY) / SQ_H), 0f, 1f);
                    heldHue = nh; hue = nh;
                    setHsv(nh, sat, val);
                } else if (grabbed == G_ALPHA) {
                    float na = clamp((float) ((mouseX - alphaX) / alphaW), 0f, 1f);
                    setAlpha(na);
                    alpha = na;
                }

                drawSvSquare(sqX, sqY, sqW, SQ_H, hue, fade);
                drawHueBar(hueX, hueY, HUE_W, SQ_H, fade);
                drawAlphaBar(alphaX, alphaY, alphaW, ALPHA_H, hue, sat, val, fade);

                int markCol = ColorUtil.multAlpha(ColorUtil.rgba(255, 255, 255, 255), fade);
                Render2DUtil.circle(sqX + sat * sqW, sqY + (1f - val) * SQ_H, 3.5f, markCol);
                Render2DUtil.roundedOutline(sqX + sat * sqW - 4, sqY + (1f - val) * SQ_H - 4, 8, 8, 4f, 1f,
                        ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 255), fade));
                float hueMarkY = hueY + hue * SQ_H;
                Render2DUtil.roundedOutline(hueX - 1, hueMarkY - 2, HUE_W + 2, 4, 2f, 1.5f, markCol);
                float alphaMarkX = alphaX + alpha * alphaW;
                Render2DUtil.roundedRect(alphaMarkX - 1.5f, alphaY - 2, 3, ALPHA_H + 4, 1.5f, markCol);

                total = NAME_ROW + 2 + SQ_H + GAP + ALPHA_H + 6;
            }
            height = total;
        }

        private void drawSvSquare(float x, float y, float w, float h, float hue, float fade) {
            float cw = w / SV_COLS, ch = h / SV_ROWS;
            for (int c = 0; c < SV_COLS; c++) {
                float s = (c + 0.5f) / SV_COLS;
                for (int r = 0; r < SV_ROWS; r++) {
                    float v = 1f - (r + 0.5f) / SV_ROWS;
                    Render2DUtil.rect(x + c * cw, y + r * ch, cw + 0.6f, ch + 0.6f,
                            ColorUtil.multAlpha(hsvColor(hue, s, v, 255), fade));
                }
            }
            Render2DUtil.roundedOutline(x, y, w, h, 3f, 1f, ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 200), fade));
        }

        private void drawHueBar(float x, float y, float w, float h, float fade) {
            float seg = h / HUE_SEG;
            for (int i = 0; i < HUE_SEG; i++) {
                float hh = (i + 0.5f) / HUE_SEG;
                Render2DUtil.rect(x, y + i * seg, w, seg + 0.6f,
                        ColorUtil.multAlpha(hsvColor(hh, 1f, 1f, 255), fade));
            }
            Render2DUtil.roundedOutline(x, y, w, h, 3f, 1f, ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 200), fade));
        }

        private void drawAlphaBar(float x, float y, float w, float h, float hue, float s, float v, float fade) {
            drawChecker(x, y, w, h, 5f, fade);
            float seg = w / ALPHA_SEG;
            for (int i = 0; i < ALPHA_SEG; i++) {
                float a = (i + 0.5f) / ALPHA_SEG;
                Render2DUtil.rect(x + i * seg, y, seg + 0.6f, h,
                        ColorUtil.multAlpha(hsvColor(hue, s, v, Math.round(a * 255)), fade));
            }
            Render2DUtil.roundedOutline(x, y, w, h, 3f, 1f, ColorUtil.multAlpha(ColorUtil.rgba(0, 0, 0, 200), fade));
        }

        private static void drawChecker(float x, float y, float w, float h, float cell, float fade) {
            int c1 = ColorUtil.multAlpha(ColorUtil.rgba(120, 120, 120, 255), fade);
            int c2 = ColorUtil.multAlpha(ColorUtil.rgba(80, 80, 80, 255), fade);
            Render2DUtil.roundedRect(x, y, w, h, 4f, c2);
            int rows = (int) Math.ceil(h / cell);
            int cols = (int) Math.ceil(w / cell);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (((r + c) & 1) == 0) continue;
                    float cx = x + c * cell, cy = y + r * cell;
                    float cwid = Math.min(cell, x + w - cx), chh = Math.min(cell, y + h - cy);
                    if (cwid > 0 && chh > 0) Render2DUtil.rect(cx, cy, cwid, chh, c1);
                }
            }
        }

        void mouseClicked(double mouseX, double mouseY, int button) {
            float sx = x + width - SW;
            float sy = y + (NAME_ROW - SH) / 2f;
            if (button == 0 && inside(mouseX, mouseY, sx, sy, SW, SH)) {
                expanded = !expanded;
                return;
            }
            if (expanded && button == 0) {
                float topY = y + NAME_ROW + 2;
                float sqX = x, sqY = topY, sqW = width - HUE_W - GAP;
                float hueX = x + width - HUE_W, hueY = topY;
                float alphaX = x, alphaY = topY + SQ_H + GAP, alphaW = width;

                if (inside(mouseX, mouseY, sqX, sqY, sqW, SQ_H)) grabbed = G_SV;
                else if (inside(mouseX, mouseY, hueX, hueY, HUE_W, SQ_H)) grabbed = G_HUE;
                else if (inside(mouseX, mouseY, alphaX, alphaY - 3, alphaW, ALPHA_H + 6)) grabbed = G_ALPHA;
            }
        }

        boolean mouseReleased() {
            boolean was = grabbed != -1;
            grabbed = -1;
            return was;
        }

        void collapse() {
            expanded = false;
            grabbed = -1;
        }
    }
}
