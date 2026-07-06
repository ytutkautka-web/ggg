package fun.photon.ui;

import com.mojang.blaze3d.platform.Window;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.ImageUtil;
import fun.photon.utils.render.MouseUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PhotonTitleScreen extends Screen {

    private static final String ALT_ICON = "KiCon/altmanager.svg";

    private final List<PhotonButton> buttons = new ArrayList<>();
    private final Animation altHover = new Animation(Easing.EASE_OUT_CUBIC, 180);
    private final Animation appear = new Animation(Easing.EASE_OUT_CUBIC, 240);
    private long animStart = System.currentTimeMillis();

    private float altX, altY, altSize;

    public PhotonTitleScreen() {
        super(Component.literal("Proton"));
    }

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    @Override
    protected void init() {
        appear.setValue(0);
        appear.reset();
        animStart = System.currentTimeMillis();
        buttons.clear();
        Minecraft mc = this.minecraft;

        buttons.add(new PhotonButton("Одиночная игра",
                () -> mc.setScreen(new SelectWorldScreen(this))));

        buttons.add(new PhotonButton("Сетевая игра", () -> {
            Screen next = mc.options.skipMultiplayerWarning
                    ? new JoinMultiplayerScreen(this)
                    : new SafetyScreen(this);
            mc.setScreen(next);
        }));

        buttons.add(new PhotonButton("Настройки",
                () -> mc.setScreen(new OptionsScreen(this, mc.options))));

        buttons.add(new PhotonButton("Выйти из игры", mc::stop));
    }

    private void layout() {
        Window win = window();
        float bw = 280f;
        float bh = 36f;
        float gap = 12f;
        float cx = win.getWidth() / 2f;
        float totalH = buttons.size() * bh + (buttons.size() - 1) * gap;
        float startY = win.getHeight() / 2f - totalH / 2f + 40f;

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBounds(cx - bw / 2f, startY + i * (bh + gap), bw, bh);
        }

        PhotonButton mp = buttons.get(1);
        altSize = bh;
        altX = mp.getX() + mp.getW() + 10f;
        altY = mp.getY();
    }

    private boolean altHovered(double mx, double my) {
        return MouseUtil.isInside(mx, my, altX, altY, altSize, altSize);
    }

    private void renderAlt(double mx, double my) {
        altHover.run(altHovered(mx, my) ? 1d : 0d);
        float k = (float) altHover.getValue();

        int fill = ColorUtil.interpolate(
                ColorUtil.rgba(22, 24, 38, 170), ColorUtil.rgba(60, 90, 200, 210), k);
        Render2DUtil.roundedRect(altX, altY, altSize, altSize, 6f, fill);
        Render2DUtil.roundedOutline(altX, altY, altSize, altSize, 6f, 1f,
                ColorUtil.multAlpha(ColorUtil.rgba(120, 150, 255, 120), 0.4f + 0.6f * k));

        float pad = altSize * 0.22f;
        ImageUtil.drawSvg(ALT_ICON, altX + pad, altY + pad, altSize - pad * 2, altSize - pad * 2,
                ColorUtil.rgba(235, 238, 250, 255));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PhotonBackground.render();

        Window win = window();
        float cx = win.getWidth() / 2f;
        float titleY = win.getHeight() / 2f - 150f;

        appear.run(1f);
        float t = (float) appear.getValue();
        float scale = 0.92f + 0.08f * t;
        RenderTransform.set(scale, win.getWidth() / 2f, win.getHeight() / 2f, t);

        PhotonFont title = FontUtil.mitr(56f);
        if (title != null) {
            title.drawCenteredString("Proton", cx, titleY,
                    ColorUtil.rgba(120, 160, 255, 255));
        }

        layout();
        double mx = MouseUtil.getX();
        double my = MouseUtil.getY();

        long elapsed = System.currentTimeMillis() - animStart;
        for (int i = 0; i < buttons.size(); i++) {
            PhotonButton button = buttons.get(i);
            double local = Math.max(0, Math.min(1, (elapsed - i * 70.0) / 260.0));
            float e = (float) (double) Easing.EASE_OUT_CUBIC.getFunction().apply(local);
            float slide = (1f - e) * 16f;

            float bx = button.getX(), by = button.getY(), bw = button.getW(), bh = button.getH();
            button.setBounds(bx, by + slide, bw, bh);
            RenderTransform.set(0.96f + 0.04f * e, bx + bw / 2f, by + bh / 2f, e);
            button.render(mx, my);
            button.setBounds(bx, by, bw, bh);
        }

        RenderTransform.set(scale, win.getWidth() / 2f, win.getHeight() / 2f, t);
        renderAlt(mx, my);

        RenderTransform.reset();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = MouseUtil.getX();
        double my = MouseUtil.getY();
        if (event.button() == 0 && altHovered(mx, my)) {
            this.minecraft.setScreen(new AltManagerScreen(this));
            return true;
        }
        for (PhotonButton button : buttons) {
            if (button.click(mx, my, event.button())) {
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return true;
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
