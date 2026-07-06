package fun.photon.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class ScreenUtil {

    private ScreenUtil() {}

    public static Screen current() {
        return Minecraft.getInstance().screen;
    }

    public static boolean isOpen() {
        return current() != null;
    }

    public static boolean inGame() {
        return current() == null;
    }

    public static boolean is(Class<? extends Screen> type) {
        Screen s = current();
        return s != null && type.isInstance(s);
    }

    public static boolean isContainer() {
        return is(AbstractContainerScreen.class);
    }

    public static boolean isChat() {
        return is(ChatScreen.class);
    }

    public static String name() {
        Screen s = current();
        return s == null ? "none" : s.getClass().getSimpleName();
    }
}
