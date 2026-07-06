package fun.photon.utils.render.glfw;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class Cursors {

    private Cursors() {}

    private static long arrow, hand, resizeH, ibeam;
    private static long currentSet = -1;
    private static long pending = -1;

    private static long create(int shape) {
        return GLFW.glfwCreateStandardCursor(shape);
    }

    public static long arrow()   { return arrow   == 0 ? (arrow   = create(GLFW.GLFW_ARROW_CURSOR))   : arrow; }
    public static long hand()    { return hand    == 0 ? (hand    = create(GLFW.GLFW_HAND_CURSOR))    : hand; }
    public static long resizeH() { return resizeH == 0 ? (resizeH = create(GLFW.GLFW_HRESIZE_CURSOR)) : resizeH; }
    public static long ibeam()   { return ibeam   == 0 ? (ibeam   = create(GLFW.GLFW_IBEAM_CURSOR))   : ibeam; }

    public static void reset() {
        pending = arrow();
    }

    public static void set(long cursor) {
        pending = cursor;
    }

    public static void flush() {
        if (pending == -1 || pending == currentSet) return;
        currentSet = pending;
        GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().handle(), pending);
    }
}
