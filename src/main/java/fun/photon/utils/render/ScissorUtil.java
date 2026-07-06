package fun.photon.utils.render;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;

public final class ScissorUtil {

    private ScissorUtil() {}

    private static boolean active;
    private static int x, y, w, h;

    public static void scissor(double px, double py, double pw, double ph) {
        x = (int) Math.round(px);
        y = (int) Math.round(py);
        w = (int) Math.max(0, Math.round(pw));
        h = (int) Math.max(0, Math.round(ph));
        active = true;
    }

    public static void enable() {
        active = true;
    }

    public static void disable() {
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    public static void apply(RenderPass pass) {
        if (!active) return;
        int fbh = Minecraft.getInstance().getWindow().getHeight();
        pass.enableScissor(x, fbh - (y + h), w, h);
    }
}
