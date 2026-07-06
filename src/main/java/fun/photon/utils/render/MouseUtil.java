package fun.photon.utils.render;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public final class MouseUtil {

    private MouseUtil() {}

    private static Window window() {
        return Minecraft.getInstance().getWindow();
    }

    public static double getX() {
        Window win = window();
        double raw = Minecraft.getInstance().mouseHandler.xpos();
        int screen = win.getScreenWidth();
        return screen == 0 ? raw : raw * win.getWidth() / screen;
    }

    public static double getY() {
        Window win = window();
        double raw = Minecraft.getInstance().mouseHandler.ypos();
        int screen = win.getScreenHeight();
        return screen == 0 ? raw : raw * win.getHeight() / screen;
    }

    public static boolean isInside(double px, double py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public static boolean isHovered(float x, float y, float w, float h) {
        return isInside(getX(), getY(), x, y, w, h);
    }
}
