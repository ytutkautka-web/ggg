package fun.photon.alt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fun.photon.Photon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class AltManager {

    private static AltManager instance;

    private final List<Alt> alts = new ArrayList<>();
    private String current = "";

    private AltManager() {}

    public static AltManager get() {
        if (instance == null) instance = new AltManager();
        return instance;
    }

    public List<Alt> getAlts() {
        return alts;
    }

    public String getCurrent() {
        return current;
    }

    public boolean add(String name) {
        if (name == null) return false;
        name = name.trim();
        if (name.length() < 3 || name.length() > 16) return false;
        for (Alt a : alts) {
            if (a.getName().equalsIgnoreCase(name)) return false;
        }
        alts.add(new Alt(name));
        save();
        return true;
    }

    public void remove(Alt alt) {
        if (alt.isStarred()) return;
        if (alts.remove(alt)) save();
    }

    public void toggleStar(Alt alt) {
        alt.setStarred(!alt.isStarred());
        save();
    }

    public void login(Alt alt) {

        User offline = new User(alt.getName(), alt.offlineUuid(), "0",
                Optional.empty(), Optional.empty());
        Minecraft.getInstance().setUser(offline);
        current = alt.getName();
        save();

        String name = alt.getName();
        Thread t = new Thread(() -> {
            UUID premium = resolvePremium(name);
            if (premium == null) return;
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                if (!name.equals(current)) return;
                mc.setUser(new User(name, premium, "0", Optional.empty(), Optional.empty()));
            });
        }, "photon-uuid-" + name);
        t.setDaemon(true);
        t.start();
    }

    public void restore(String name) {
        if (name == null || name.isEmpty()) return;
        Alt found = null;
        for (Alt a : alts) {
            if (a.getName().equals(name)) { found = a; break; }
        }
        if (found == null) return;
        Minecraft.getInstance().setUser(new User(found.getName(), found.offlineUuid(), "0",
                Optional.empty(), Optional.empty()));
        current = found.getName();

        final String n = found.getName();
        Thread t = new Thread(() -> {
            UUID premium = resolvePremium(n);
            if (premium == null) return;
            Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                if (!n.equals(current)) return;
                mc.setUser(new User(n, premium, "0", Optional.empty(), Optional.empty()));
            });
        }, "photon-uuid-" + n);
        t.setDaemon(true);
        t.start();
    }

    private static UUID resolvePremium(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(4000);
            c.setRequestProperty("User-Agent", "Proton-Client");
            if (c.getResponseCode() != 200) return null;
            String body = new String(c.getInputStream().readAllBytes());
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            if (!o.has("id")) return null;
            String hex = o.get("id").getAsString();
            String dashed = hex.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                    "$1-$2-$3-$4-$5");
            return UUID.fromString(dashed);
        } catch (Exception e) {
            return null;
        }
    }

    private void save() {
        Photon.getInstance().getConfigManager().saveAlts();
    }
}
