package fun.photon.ui;

import com.mojang.blaze3d.platform.Window;
import fun.photon.alt.Alt;
import fun.photon.alt.AltManager;
import fun.photon.alt.SkinHeads;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.renderer.texture.DynamicTexture;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.MouseUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.ScissorUtil;
import fun.photon.utils.render.glfw.Cursors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class AltManagerScreen extends Screen {

    private static final String ICON = "KiCon/altmanager.svg";
    private static final String ICON_DEL = "KiCon/close.svg";
    private static final String ICON_STAR = "KiCon/star.svg";
    private static final int GOLD = ColorUtil.rgba(255, 200, 60, 255);

    private final Screen parent;
    private String input = "";
    private float scroll;
    private boolean fieldFocused = true;
    private final Animation appear = new Animation(Easing.EASE_OUT_CUBIC, 240);
    private boolean closing;
    private final java.util.Map<String, Animation> starAnims = new java.util.HashMap<>();

    private static final float PANEL_W = 360f;
    private static final float PANEL_H = 380f;
    private static final float ROW_H = 38f;
    private static final float HEADER = 52f;
    private static final float FOOTER = 56f;

    public AltManagerScreen(Screen parent) {
        super(Component.literal("AltManager"));
        this.parent = parent;
    }

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    private float px() { return window().getWidth() / 2f - PANEL_W / 2f; }
    private float py() { return window().getHeight() / 2f - PANEL_H / 2f; }

    private float listTop() { return py() + HEADER; }
    private float listBottom() { return py() + PANEL_H - FOOTER; }

    @Override
    protected void init() {
        appear.setValue(0);
        appear.reset();
        closing = false;

        for (Alt a : AltManager.get().getAlts()) SkinHeads.get(a.getName());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Cursors.reset();
        double mx = MouseUtil.getX();
        double my = MouseUtil.getY();

        appear.run(closing ? 0f : 1f);
        float t = (float) appear.getValue();

        if (t < 0.999f) PhotonBackground.render();
        AltBackground.render(mx, my, t);

        float scale = 0.92f + 0.08f * t;
        RenderTransform.set(scale, window().getWidth() / 2f, window().getHeight() / 2f, t);

        float x = px(), y = py();

        Render2DUtil.roundedRect(x, y, PANEL_W, PANEL_H, 12f, ColorUtil.rgba(14, 16, 22, 225));
        Render2DUtil.roundedOutline(x, y, PANEL_W, PANEL_H, 12f, 1f, ColorUtil.rgba(80, 110, 200, 90));

        PhotonFont title = FontUtil.medium(22f);
        ImageUtil.drawSvg(ICON, x + 16, y + 14, 24, 24, ColorUtil.rgba(120, 160, 255, 255));
        title.drawInRowY("AltManager", x + 48, y + 12, 28, ColorUtil.rgba(235, 238, 250, 255));

        String cur = AltManager.get().getCurrent();
        if (!cur.isEmpty()) {
            float chs = 20f;
            float chx = x + PANEL_W - 16 - chs;
            float chy = y + 16;
            Render2DUtil.roundedRect(chx, chy, chs, chs, 4f, ColorUtil.rgba(0, 0, 0, 120));
            DynamicTexture chead = SkinHeads.get(cur);
            if (chead != null) ImageUtil.drawTexture(chead, chx, chy, chs, chs, ColorUtil.rgba(255, 255, 255, 255));
            FontUtil.medium(13f).drawRightInRow(cur, chx - 8, chy, chs, ColorUtil.rgba(180, 190, 210, 255));
        }

        renderList(x, mx, my);
        renderFooter(x, y, mx, my);

        RenderTransform.reset();

        Cursors.flush();

        if (closing && appear.isFinished()) {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    private void renderList(float x, double mx, double my) {
        java.util.List<Alt> alts = AltManager.get().getAlts();
        float top = listTop();
        float bottom = listBottom();
        float listX = x + 14;
        float listW = PANEL_W - 28;

        float contentH = alts.size() * (ROW_H + 6);
        float view = bottom - top;
        float maxScroll = Math.max(0, contentH - view);
        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;

        ScissorUtil.scissor(listX, top, listW, view);
        float yy = top - scroll;
        for (Alt a : alts) {
            renderRow(a, listX, yy, listW, mx, my);
            yy += ROW_H + 6;
        }
        ScissorUtil.scissor(0, 0, window().getWidth(), window().getHeight());

        if (alts.isEmpty()) {
            FontUtil.medium(14f).drawCenteredInRow("Нет аккаунтов — добавь ник ниже",
                    x + PANEL_W / 2f, top, 40, ColorUtil.rgba(120, 130, 150, 255));
        }
    }

    private void renderRow(Alt a, float rx, float ry, float rw, double mx, double my) {
        boolean hov = MouseUtil.isInside(mx, my, rx, ry, rw, ROW_H);
        boolean isCur = a.getName().equals(AltManager.get().getCurrent());
        boolean star = a.isStarred();

        int base;
        if (star) base = ColorUtil.rgba(58, 46, 16, 225);
        else base = isCur ? ColorUtil.rgba(34, 54, 96, 220) : ColorUtil.rgba(26, 30, 40, 210);
        if (hov) base = ColorUtil.brighten(base, 18);
        Render2DUtil.roundedRect(rx, ry, rw, ROW_H, 8f, base);
        Render2DUtil.roundedOutline(rx, ry, rw, ROW_H, 8f, 1f,
                star ? ColorUtil.rgba(255, 200, 60, 110) : ColorUtil.rgba(255, 255, 255, 28));

        float hs = 24f;
        float hx = rx + 8f;
        float hy = ry + (ROW_H - hs) / 2f;
        Render2DUtil.roundedRect(hx, hy, hs, hs, 4f, ColorUtil.rgba(0, 0, 0, 120));
        DynamicTexture head = SkinHeads.get(a.getName());
        if (head != null) {
            ImageUtil.drawTexture(head, hx, hy, hs, hs, ColorUtil.rgba(255, 255, 255, 255));
        }

        FontUtil.medium(16f).drawInRowY(a.getName(), hx + hs + 10, ry, ROW_H,
                star ? GOLD : ColorUtil.rgba(235, 238, 250, 255));

        float starY = ry + ROW_H / 2f - 10f;
        float starX = rx + rw - 56f;
        boolean starHov = MouseUtil.isInside(mx, my, starX, starY, 20, 20);

        Animation sa = starAnims.computeIfAbsent(a.getName(),
                k -> new Animation(Easing.EASE_OUT_CUBIC, 320));
        sa.run(star ? 1d : 0d);
        float v = (float) sa.getValue();

        float cxs = starX + 10f, cys = starY + 10f;

        float scl = 0.85f + 0.30f * v - 0.15f * v * (1f - v);
        float ss = 16f * scl;
        int starCol = ColorUtil.interpolate(
                starHov ? ColorUtil.rgba(180, 185, 200, 255) : ColorUtil.rgba(95, 100, 115, 255), GOLD, v);
        ImageUtil.drawSvg(ICON_STAR, cxs - ss / 2f, cys - ss / 2f, ss, ss, starCol);

        float del = ry + ROW_H / 2f - 10f;
        float delX = rx + rw - 30f;
        boolean delHov = !star && MouseUtil.isInside(mx, my, delX, del, 20, 20);
        int delBg = star ? ColorUtil.rgba(40, 40, 46, 140)
                : (delHov ? ColorUtil.rgba(180, 60, 60, 230) : ColorUtil.rgba(60, 40, 44, 200));
        Render2DUtil.roundedRect(delX, del, 20, 20, 5f, delBg);
        int delIc = star ? ColorUtil.rgba(110, 110, 120, 160)
                : (delHov ? ColorUtil.rgba(255, 235, 235, 255) : ColorUtil.rgba(225, 180, 180, 255));
        ImageUtil.drawSvg(ICON_DEL, delX + 5, del + 5, 10, 10, delIc);

        if (hov || starHov) Cursors.set(Cursors.hand());
    }

    private void renderFooter(float x, float y, double mx, double my) {
        float fy = y + PANEL_H - FOOTER + 12f;
        float fx = x + 14;
        float fieldW = PANEL_W - 28 - 92;
        float fieldH = 30f;

        boolean fieldHov = MouseUtil.isInside(mx, my, fx, fy, fieldW, fieldH);
        Render2DUtil.roundedRect(fx, fy, fieldW, fieldH, 7f, ColorUtil.rgba(24, 28, 38, 235));
        Render2DUtil.roundedOutline(fx, fy, fieldW, fieldH, 7f, 1f,
                fieldFocused ? ColorUtil.rgba(110, 150, 255, 200) : ColorUtil.rgba(90, 120, 210, 120));

        PhotonFont ff = FontUtil.medium(15f);
        if (input.isEmpty() && !fieldFocused) {
            ff.drawInRowY("Введите ник", fx + 8, fy, fieldH, ColorUtil.rgba(110, 120, 140, 255));
        } else {
            float tw = ff.getWidth(input);
            ff.drawInRowY(input, fx + 8, fy, fieldH, ColorUtil.rgba(235, 238, 250, 255));
            if (fieldFocused) {
                Render2DUtil.rect(fx + 8 + tw + 1, fy + 7, 1.5f, fieldH - 14,
                        ColorUtil.rgba(200, 215, 255, 235));
            }
        }

        float bx = fx + fieldW + 10;
        float bw = 82f;
        boolean addHov = MouseUtil.isInside(mx, my, bx, fy, bw, fieldH);
        Render2DUtil.roundedRect(bx, fy, bw, fieldH, 7f,
                addHov ? ColorUtil.rgba(60, 90, 200, 235) : ColorUtil.rgba(40, 60, 140, 220));
        ff.drawCenteredInRow("Добавить", bx + bw / 2f, fy, fieldH, ColorUtil.rgba(235, 238, 250, 255));

        if (fieldHov || addHov) Cursors.set(Cursors.hand());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return true;
        double mx = MouseUtil.getX();
        double my = MouseUtil.getY();
        float x = px(), y = py();

        java.util.List<Alt> alts = AltManager.get().getAlts();
        float listX = x + 14;
        float listW = PANEL_W - 28;
        float top = listTop();
        float bottom = listBottom();
        float yy = top - scroll;
        for (int i = 0; i < alts.size(); i++) {
            Alt a = alts.get(i);
            if (yy + ROW_H > top && yy < bottom) {
                float starX = listX + listW - 56f;
                float starY = yy + ROW_H / 2f - 10f;
                if (MouseUtil.isInside(mx, my, starX, starY, 20, 20)) {
                    AltManager.get().toggleStar(a);
                    return true;
                }
                float delX = listX + listW - 30f;
                float del = yy + ROW_H / 2f - 10f;
                if (!a.isStarred() && MouseUtil.isInside(mx, my, delX, del, 20, 20)) {
                    AltManager.get().remove(a);
                    return true;
                }
                if (MouseUtil.isInside(mx, my, listX, yy, listW, ROW_H)) {
                    AltManager.get().login(a);
                    return true;
                }
            }
            yy += ROW_H + 6;
        }

        float fy = y + PANEL_H - FOOTER + 12f;
        float fx = x + 14;
        float fieldW = PANEL_W - 28 - 92;
        float bx = fx + fieldW + 10;
        if (MouseUtil.isInside(mx, my, fx, fy, fieldW, 30f)) {
            fieldFocused = true;
            return true;
        }
        if (MouseUtil.isInside(mx, my, bx, fy, 82f, 30f)) {
            tryAdd();
            return true;
        }
        fieldFocused = false;
        return true;
    }

    private void tryAdd() {
        if (AltManager.get().add(input)) {
            SkinHeads.get(input);
            input = "";
        }
    }

    private void append(String s) {
        for (int i = 0; i < s.length() && input.length() < 16; i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
                input += c;
            }
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (fieldFocused) append(event.codepointAsString());
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (!fieldFocused) return true;

        if (event.isPaste()) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip != null) append(clip.trim());
            return true;
        }
        if (event.isCopy() || event.isCut()) {
            if (!input.isEmpty()) Minecraft.getInstance().keyboardHandler.setClipboard(input);
            if (event.isCut()) input = "";
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (!input.isEmpty()) input = input.substring(0, input.length() - 1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            tryAdd();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= (float) scrollY * 22f;
        return true;
    }

    @Override
    public void onClose() {
        if (!closing) {
            closing = true;
            appear.reset();
            return;
        }
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
