package fun.photon.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtil {

    private ChatUtil() {}

    public static void add(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(Component.literal(text));
        }
    }

    public static void prefixed(String text) {
        add("§7[§bProton§7] §r" + text);
    }
}
