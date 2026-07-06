package fun.photon.hud;

import fun.photon.clickgui.Palette;
import fun.photon.utils.IMinecraft;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.FontUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;

public abstract class HudElement implements IMinecraft {

    public static boolean EDIT;
    public static float SCALE = 1f;
    public static net.minecraft.client.gui.GuiGraphics GRAPHICS;

    public final String id;
    public final String name;
    public final Animation vis = new Animation(Easing.EASE_OUT_CUBIC, 220);
    protected float x, y;

    protected HudElement(String id, String name, float defX, float defY) {
        this.id = id;
        this.name = name;
        this.x = defX;
        this.y = defY;
    }

    public abstract float width();

    public abstract float height();

    public abstract void render();

    public abstract boolean enabled();

    public boolean active() {
        return enabled();
    }

    public void onRightClick() {}

    public boolean settingsOpen;
    public final Animation settingsAnim = new Animation(Easing.EASE_OUT_CUBIC, 200);

    public boolean hasSettings() { return false; }

    public java.util.List<Opt> options() { return java.util.Collections.emptyList(); }

    public static final class Opt {
        public final String name;
        public final java.util.function.BooleanSupplier state;
        public final Runnable toggle;
        public Opt(String name, java.util.function.BooleanSupplier state, Runnable toggle) {
            this.name = name;
            this.state = state;
            this.toggle = toggle;
        }
    }

    public float getX() { return x; }

    public float getY() { return y; }

    public void setPosition(float nx, float ny) {
        this.x = nx;
        this.y = ny;
    }

    protected PhotonFont font(float size) {
        return FontUtil.medium(size * SCALE);
    }

    protected float s(float v) {
        return v * SCALE;
    }

    protected int accent() {
        return Palette.ACCENT;
    }

    protected int accent2() {
        return ColorUtil.interpolate(Palette.ACCENT, 0xFFFFFFFF, 0.35);
    }

    protected static final int PANEL = ColorUtil.rgba(22, 23, 28, 140);

    protected void panel(float px, float py, float pw, float ph) {
        Render2DUtil.rounded(px, py, pw, ph, s(4), s(4), s(4), s(4), 1f, PANEL, 0, 0f);
    }

    protected void accentBar(float px, float py, float ph) {
        Render2DUtil.rounded(px, py, s(2), ph, 0f, 0f, s(4), s(4), 1f, accent(), 0, 0f);
    }

    protected void itemIcon(net.minecraft.world.item.ItemStack stack, float px, float py, float size) {
        if (GRAPHICS == null || stack == null || stack.isEmpty()) return;
        double gs = mc.getWindow().getGuiScale();
        if (gs <= 0) return;
        float tx = fun.photon.utils.render.RenderTransform.tx(px);
        float ty = fun.photon.utils.render.RenderTransform.ty(py);
        float sz = size * fun.photon.utils.render.RenderTransform.scale;
        org.joml.Matrix3x2fStack pose = GRAPHICS.pose();
        pose.pushMatrix();
        pose.scale((float) (1.0 / gs), (float) (1.0 / gs));
        pose.translate(tx, ty);
        pose.scale(sz / 16f, sz / 16f);
        GRAPHICS.renderFakeItem(stack, 0, 0);
        pose.popMatrix();
    }

    protected void playerHead(net.minecraft.resources.Identifier skin, float px, float py, float size) {
        if (skin == null) return;
        fun.photon.utils.render.ImageUtil.drawTextureRegion(skin, px, py, size, size,
                8f / 64f, 8f / 64f, 16f / 64f, 16f / 64f, -1);
        fun.photon.utils.render.ImageUtil.drawTextureRegion(skin, px, py, size, size,
                40f / 64f, 8f / 64f, 48f / 64f, 16f / 64f, -1);
    }
}
