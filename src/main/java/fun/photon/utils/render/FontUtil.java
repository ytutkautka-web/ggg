package fun.photon.utils.render;

import net.minecraft.client.gui.GuiGraphics;

import java.awt.Font;
import java.io.InputStream;

public final class FontUtil {

    private FontUtil() {}

    private static final java.util.Map<Integer, PhotonFont> mediumBySize = new java.util.HashMap<>();
    private static final java.util.Map<Integer, PhotonFont> mitrBySize = new java.util.HashMap<>();
    private static Font baseMedium;
    private static Font baseMitr;

    private static Font loadTtf(String path) {
        try (InputStream is = FontUtil.class.getResourceAsStream(path)) {
            if (is == null) {
                System.out.println("[Photon] font not found on classpath: " + path);
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PhotonFont medium(float sizePx) {
        if (baseMedium == null) {
            baseMedium = loadTtf(fun.photon.Photon.fontResource("Ubuntu-Medium.ttf"));
        }
        if (baseMedium == null) return null;
        int key = Math.round(sizePx);
        return mediumBySize.computeIfAbsent(key,
                k -> new PhotonFont(baseMedium.deriveFont(Font.PLAIN, (float) k), true));
    }

    public static PhotonFont mitr(float sizePx) {
        if (baseMitr == null) {
            baseMitr = loadTtf(fun.photon.Photon.fontResource("Mitr-Regular.ttf"));
        }
        if (baseMitr == null) return medium(sizePx);
        int key = Math.round(sizePx);
        return mitrBySize.computeIfAbsent(key,
                k -> new PhotonFont(baseMitr.deriveFont(Font.PLAIN, (float) k), true));
    }

    public static void draw(GuiGraphics ignored, String text, float x, float y, int color) {
        PhotonFont f = medium(18f);
        if (f != null) f.drawString(text, x, y, color);
    }
}
