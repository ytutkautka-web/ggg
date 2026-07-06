package fun.photon.alt;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.BufferUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkinHeads {

    private SkinHeads() {}

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();

    private static final class Entry {
        volatile byte[] png;
        volatile DynamicTexture tex;
        volatile boolean loading;
        volatile boolean failed;
    }

    public static DynamicTexture get(String name) {
        String key = name.toLowerCase();
        Entry e = CACHE.computeIfAbsent(key, k -> new Entry());

        if (e.tex != null) return e.tex;
        if (e.png != null) {
            try {
                ByteBuffer data = BufferUtils.createByteBuffer(e.png.length).put(e.png);
                data.flip();
                e.tex = new DynamicTexture(() -> "photon-head-" + key, NativeImage.read(data));
            } catch (Exception ex) {
                e.failed = true;
                e.png = null;
            }
            return e.tex;
        }
        if (!e.loading && !e.failed) {
            e.loading = true;
            Thread t = new Thread(() -> download(key, e), "photon-skin-" + key);
            t.setDaemon(true);
            t.start();
        }
        return null;
    }

    private static void download(String name, Entry e) {
        try {
            URL url = new URL("https://minotar.net/helm/" + name + "/64.png");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(4000);
            c.setRequestProperty("User-Agent", "Proton-Client");
            try (InputStream is = c.getInputStream()) {
                e.png = is.readAllBytes();
            }
        } catch (Exception ex) {
            e.failed = true;
        } finally {
            e.loading = false;
        }
    }
}
