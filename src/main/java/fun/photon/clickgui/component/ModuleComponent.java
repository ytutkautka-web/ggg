package fun.photon.clickgui.component;

import fun.photon.clickgui.Palette;
import fun.photon.module.Module;
import fun.photon.module.impl.render.ClickGui;
import fun.photon.setting.BindSetting;
import fun.photon.setting.BooleanSetting;
import fun.photon.setting.BoundsSetting;
import fun.photon.setting.ColorSetting;
import fun.photon.setting.ListSetting;
import fun.photon.setting.ModeSetting;
import fun.photon.setting.MultiBooleanSetting;
import fun.photon.setting.NumberSetting;
import fun.photon.setting.Setting;
import fun.photon.setting.StringSetting;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.RectUtil;
import fun.photon.utils.render.ScissorUtil;
import fun.photon.utils.render.glfw.Cursors;

import java.util.ArrayList;
import java.util.List;

public class ModuleComponent {

    public static final float ROW_HEIGHT = 36f;
    private static final float ROUND = 8f;

    private final Module module;
    private final List<ValueComponent> values = new ArrayList<>();

    private float x, y, width, height = ROW_HEIGHT;
    private boolean expanded;
    private final Animation expandAnim = new Animation(Easing.EASE_OUT_CUBIC, 100);

    public ModuleComponent(Module module, float width) {
        this.module = module;
        this.width = width;
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BindSetting) continue;
            if (s instanceof BooleanSetting b) values.add(new BooleanValueComponent(b, width));
            else if (s instanceof NumberSetting n) values.add(new NumberValueComponent(n, width));
            else if (s instanceof ModeSetting m) values.add(new ModeValueComponent(m, width));
            else if (s instanceof ColorSetting c) values.add(new ColorValueComponent(c, width));
            else if (s instanceof BoundsSetting bo) values.add(new BoundsValueComponent(bo, width));
            else if (s instanceof ListSetting l) values.add(new ListValueComponent(l, width));
            else if (s instanceof MultiBooleanSetting mb) values.add(new MultiBooleanValueComponent(mb, width));
            else if (s instanceof StringSetting st) values.add(new StringValueComponent(st, width));
        }
    }

    public Module getModule() {
        return module;
    }

    public float getHeight() {
        return height;
    }

    public void setLayout(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void render(double mouseX, double mouseY, float panelX, float panelW, float clipTop, float clipBottom) {
        boolean enabled = module.isEnabled();
        boolean rowVisible = (y + ROW_HEIGHT > clipTop) && (y < clipBottom);
        boolean hovered = rowVisible && hover(mouseX, mouseY, x, y, width, ROW_HEIGHT);

        float valuesH = 0f;
        for (ValueComponent v : values) valuesH += v.getHeight();
        expandAnim.run(expanded ? 1 : 0);
        float k = Math.max(0f, Math.min(1f, (float) expandAnim.getValue()));
        height = ROW_HEIGHT + (valuesH + 6) * k;

        boolean open = height > ROW_HEIGHT + 0.5f;

        int headerBase = enabled ? ColorUtil.brighten(Palette.MODULE_OFF, 70) : Palette.MODULE_OFF;
        if (open) {
            float fullHeight = ROW_HEIGHT + valuesH + 6;
            float clipB = Math.min(clipBottom, y + height);
            int bodyFill = ColorUtil.withAlpha(Palette.CARD_FILL, Palette.CARD_A_BODY);
            int headerFill = ColorUtil.withAlpha(headerBase, 255);

            ScissorUtil.scissor(x, Math.max(clipTop, y), width, clipB - Math.max(clipTop, y));

            RectUtil.drawSmoothRoundedRect(x, y, x + width, y + fullHeight, ROUND, bodyFill);

            RectUtil.drawRoundedRectCorners(x, y, x + width, y + ROW_HEIGHT, 0f, ROUND, 0f, ROUND, headerFill);

            RectUtil.drawSmoothRoundedOutline(x, y, x + width, y + fullHeight, ROUND, 1f, Palette.CARD_OUTLINE);
            ScissorUtil.scissor(panelX, clipTop, panelW, clipBottom - clipTop);
        } else {
            int headerFill = ColorUtil.withAlpha(headerBase, 255);

            RectUtil.drawSmoothRoundedRect(x, y, x + width, y + ROW_HEIGHT, ROUND, headerFill);
            RectUtil.drawSmoothRoundedOutline(x, y, x + width, y + ROW_HEIGHT, ROUND, 1f, Palette.CARD_OUTLINE);
        }
        if (hovered) {

            if (open) {
                RectUtil.drawRoundedRectCorners(x, y, x + width, y + ROW_HEIGHT, 0f, ROUND, 0f, ROUND, Palette.HOVER);
            } else {
                RectUtil.drawSmoothRoundedRect(x, y, x + width, y + ROW_HEIGHT, ROUND, Palette.HOVER);
            }
            Cursors.set(Cursors.hand());
        }

        PhotonFont f = font(16);
        f.drawInRowY(module.getName(), x + 12, y, ROW_HEIGHT, Palette.TEXT_WHITE);

        if (hovered && !module.getDescription().isEmpty()) {
            Tooltip.queue(module.getDescription(), mouseX, mouseY);
        }

        if (height > ROW_HEIGHT + 0.5f) {
            float clipY1 = Math.max(clipTop, y + ROW_HEIGHT);
            float clipY2 = Math.min(clipBottom, y + height);
            if (clipY2 > clipY1) {
                ScissorUtil.scissor(x, clipY1, width, clipY2 - clipY1);
                int fg = Palette.TEXT_WHITE;
                float yy = y + ROW_HEIGHT + 3;
                for (ValueComponent v : values) {
                    v.setForeground(fg);
                    v.setPosition(x, yy);
                    v.render(mouseX, mouseY);
                    yy += v.getHeight();
                }

                ScissorUtil.scissor(panelX, clipTop, panelW, clipBottom - clipTop);
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button, float clipTop, float clipBottom) {
        boolean rowVisible = (y + ROW_HEIGHT > clipTop) && (y < clipBottom);
        if (rowVisible && hover(mouseX, mouseY, x, y, width, ROW_HEIGHT)) {
            if (button == 0) {
                if (!(module instanceof ClickGui)) module.toggle();
            } else if (button == 1) {
                if (!values.isEmpty()) expanded = !expanded;
            } else if (button == 2 && BindPopup.INSTANCE != null) {
                BindPopup.INSTANCE.toggleFor(module.getBind(), module, x + width + 6, y);
            }
        }
        if (expanded) {
            for (ValueComponent v : values) v.mouseClicked(mouseX, mouseY, button);
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (ValueComponent v : values) v.mouseReleased(mouseX, mouseY, button);
    }

    public void keyPressed(int key) {
        if (expanded) for (ValueComponent v : values) v.keyPressed(key);
    }

    public void onClose() {
        for (ValueComponent v : values) v.onClose();
    }

    private static boolean hover(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static PhotonFont font(float size) {
        return fun.photon.utils.render.FontUtil.medium(size);
    }
}
