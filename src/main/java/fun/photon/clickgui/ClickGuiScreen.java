package fun.photon.clickgui;

import com.mojang.blaze3d.platform.Window;
import fun.photon.clickgui.component.BindPopup;
import fun.photon.clickgui.component.CategoryComponent;
import fun.photon.clickgui.component.ConfigPanel;
import fun.photon.clickgui.component.ThemePanel;
import fun.photon.module.Category;
import fun.photon.module.impl.render.ClickGui;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import fun.photon.utils.render.glfw.Cursors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private static final float GAP = 14f;

    private final ClickGui owner;
    private final List<CategoryComponent> panels = new ArrayList<>();
    private final ThemePanel themePanel;
    private final ConfigPanel configPanel;
    private final BindPopup bindPopup = new BindPopup();

    private final Animation appear = new Animation(Easing.EASE_OUT_CUBIC, 220);
    private boolean closing;

    public static float OPEN_PROGRESS;

    public static float openProgress() {
        return Minecraft.getInstance().screen instanceof ClickGuiScreen ? OPEN_PROGRESS : 0f;
    }

    public ClickGuiScreen(ClickGui owner) {
        super(Component.literal(""));
        this.owner = owner;
        this.themePanel = new ThemePanel();
        this.configPanel = new ConfigPanel();
        BindPopup.INSTANCE = bindPopup;
        for (Category c : Category.values()) {
            panels.add(new CategoryComponent(c));
        }
    }

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    private static double mouseX() {
        return fun.photon.utils.render.MouseUtil.getX();
    }

    private static double mouseY() {
        return fun.photon.utils.render.MouseUtil.getY();
    }

    @Override
    protected void init() {
        appear.setValue(0);
        appear.reset();
        closing = false;
    }

    private void layout() {
        int n = panels.size();
        float total = n * CategoryComponent.WIDTH + (n - 1) * GAP;
        float baseX = window().getWidth() / 2f - total / 2f;
        float baseY = window().getHeight() / 2f - CategoryComponent.HEIGHT / 2f;
        for (int i = 0; i < n; i++) {
            panels.get(i).setPosition(baseX + i * (CategoryComponent.WIDTH + GAP), baseY);
        }

        themePanel.setAnchor(baseX + total, baseY);
        configPanel.setAnchor(window().getWidth(), window().getHeight());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Cursors.reset();

        appear.run(closing ? 0f : 1f);
        float t = (float) appear.getValue();
        OPEN_PROGRESS = t;

        Render2DUtil.rect(0, 0, window().getWidth(), window().getHeight(),
                ColorUtil.multAlpha(Palette.DIM, t));

        float scale = 0.9f + 0.1f * t;
        RenderTransform.set(scale, window().getWidth() / 2f, window().getHeight() / 2f, t);

        layout();
        double mx = mouseX(), my = mouseY();
        for (CategoryComponent panel : panels) {
            panel.render(mx, my);
        }
        float slide = closing ? (1f - t) : 0f;
        themePanel.setScreenSlide(slide);
        configPanel.setScreenSlide(slide);
        themePanel.render(mx, my);
        configPanel.render(mx, my);

        RenderTransform.reset();

        bindPopup.render(mx, my);

        fun.photon.clickgui.component.Tooltip.renderQueued();

        Cursors.flush();

        if (closing && appear.isFinished()) {
            finishClose();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = mouseX(), my = mouseY();

        if (bindPopup.isOpen()) {
            bindPopup.mouseClicked(mx, my, event.button());
            return true;
        }
        for (CategoryComponent panel : panels) {
            panel.mouseClicked(mx, my, event.button());
        }
        themePanel.mouseClicked(mx, my, event.button());
        configPanel.mouseClicked(mx, my, event.button());
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mx = mouseX(), my = mouseY();
        for (CategoryComponent panel : panels) {
            panel.mouseReleased(mx, my, event.button());
        }
        themePanel.mouseReleased(mx, my, event.button());
        configPanel.mouseReleased(mx, my, event.button());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double mx = mouseX(), my = mouseY();
        for (CategoryComponent panel : panels) {
            if (panel.isInside(mx, my)) panel.addScroll(scrollY);
        }
        if (themePanel.isInside(mx, my)) themePanel.mouseScrolled(scrollY);
        if (configPanel.isInside(mx, my)) configPanel.mouseScrolled(scrollY);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        if (bindPopup.isOpen()) {
            bindPopup.keyPressed(key);
            return true;
        }
        for (CategoryComponent panel : panels) {
            panel.keyPressed(key);
        }
        if (event.isPaste()) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (themePanel.isNameFocused()) { themePanel.paste(clip); return true; }
            if (configPanel.isOpen()) { configPanel.paste(clip); return true; }
        }
        themePanel.keyPressed(key);
        configPanel.keyPressed(key);
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return true;
    }

    @Override
    public void onClose() {
        if (!closing) {
            closing = true;
            appear.reset();
            bindPopup.beginClose();
            return;
        }
        finishClose();
    }

    private void finishClose() {
        for (CategoryComponent panel : panels) panel.onClose();
        bindPopup.onClose();
        themePanel.onClose();
        configPanel.onClose();
        Cursors.set(Cursors.arrow());
        if (owner != null) owner.setEnabled(false);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
