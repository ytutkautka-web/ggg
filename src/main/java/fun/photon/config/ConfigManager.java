package fun.photon.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fun.photon.Photon;
import fun.photon.module.Module;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {

    private static final byte[] MAGIC = {0x50, 0x43, 0x46, 0x47};
    private static final byte FORMAT_VERSION = 2;
    private static final int SCHEMA_VERSION = 1;

    public static final String AUTO_CONFIG = "autoconfig";
    private static final String THEMES_CONFIG = "themes";
    private static final String ALTS_CONFIG = "alts";
    private static final String FRIENDS_CONFIG = "friends";
    private static final String META_CONFIG = "_meta";
    private static final String EXT = ".cfg";

    private final java.util.Set<String> starredConfigs = new java.util.HashSet<>();

    private final com.google.gson.Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ConfigCodec codec = new KeyConfigCodec();

    private final Path root;
    private final Path moduleDir;
    private final Path themeDir;
    private final Path altDir;

    private boolean loading = true;

    public ConfigManager() {
        this.root = Photon.DATA_ROOT;
        this.moduleDir = Photon.MODULE_DIR;
        this.themeDir = Photon.THEME_DIR;
        this.altDir = Photon.ALT_DIR;
    }

    public void init() {
        try {
            Files.createDirectories(moduleDir);
            Files.createDirectories(themeDir);
            Files.createDirectories(altDir);
        } catch (IOException e) {
            System.err.println("[Photon] failed to create config dirs under " + root);
            e.printStackTrace();
        }

        loadThemes();
        loadAlts();
        loadFriends();
        loadConfigMeta();

        Path auto = pathFor(AUTO_CONFIG);
        if (Files.exists(auto)) {
            load(AUTO_CONFIG);
        } else {
            System.out.println("[Photon] no autoconfig — creating default at " + auto);
        }
        loading = false;

        save(AUTO_CONFIG);
        saveThemes();
        saveAlts();
    }

    public Path getRoot() {
        return root;
    }

    public Path getModuleDir() {
        return moduleDir;
    }

    public Path getThemeDir() {
        return themeDir;
    }

    private Path pathFor(String name) {
        return moduleDir.resolve(name + EXT);
    }

    public List<String> listConfigs() {
        List<String> out = new ArrayList<>();
        if (!Files.isDirectory(moduleDir)) return out;
        try {
            Files.list(moduleDir)
                    .filter(p -> p.getFileName().toString().endsWith(EXT))
                    .forEach(p -> {
                        String fn = p.getFileName().toString();
                        String name = fn.substring(0, fn.length() - EXT.length());

                        if (name.equals(AUTO_CONFIG) || name.startsWith("_")) return;
                        out.add(name);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public void save(String name) {
        JsonObject root = serializeModules();
        byte[] json = gson.toJson(root).getBytes(StandardCharsets.UTF_8);
        try {
            Files.createDirectories(moduleDir);
            Files.write(pathFor(name), wrap(json));
        } catch (IOException e) {
            System.err.println("[Photon] failed to save config " + name);
            e.printStackTrace();
        }
    }

    public boolean delete(String name) {
        if (AUTO_CONFIG.equals(name) || isConfigStarred(name)) return false;
        try {
            return Files.deleteIfExists(pathFor(name));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConfigStarred(String name) {
        return starredConfigs.contains(name);
    }

    public void toggleConfigStar(String name) {
        if (!starredConfigs.add(name)) starredConfigs.remove(name);
        saveConfigMeta();
    }

    private Path metaPath() {
        return moduleDir.resolve(META_CONFIG + EXT);
    }

    private void saveConfigMeta() {
        JsonObject root = new JsonObject();
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (String s : starredConfigs) arr.add(s);
        root.add("starred", arr);
        byte[] json = gson.toJson(root).getBytes(StandardCharsets.UTF_8);
        try {
            Files.createDirectories(moduleDir);
            Files.write(metaPath(), wrap(json));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfigMeta() {
        Path path = metaPath();
        if (!Files.exists(path)) return;
        try {
            byte[] json = unwrap(Files.readAllBytes(path));
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            starredConfigs.clear();
            if (root.has("starred")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("starred")) {
                    starredConfigs.add(el.getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String name) {
        Path path = pathFor(name);
        boolean prev = loading;
        loading = true;
        try {
            byte[] json = unwrap(Files.readAllBytes(path));
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            applyModules(root);
        } catch (Exception e) {
            System.err.println("[Photon] failed to load config " + name);
            e.printStackTrace();
        } finally {
            loading = prev;
        }
    }

    public void notifyChanged() {
        if (loading) return;
        save(AUTO_CONFIG);
    }

    private static String themeFileName(String name) {
        return "theme_" + name.replaceAll("[^a-zA-Z0-9_]", "_") + EXT;
    }

    public void deleteThemeFile(String name) {
        try {
            Files.deleteIfExists(themeDir.resolve(themeFileName(name)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveThemes() {
        fun.photon.clickgui.theme.ThemeManager tm = fun.photon.clickgui.theme.ThemeManager.get();
        try {
            Files.createDirectories(themeDir);

            JsonObject index = new JsonObject();
            index.addProperty("schema", SCHEMA_VERSION);
            index.addProperty("selected", tm.getActive().getName());
            com.google.gson.JsonArray customs = new com.google.gson.JsonArray();
            for (fun.photon.clickgui.theme.Theme c : tm.getEditable()) {
                customs.add(c.getName());
                JsonObject obj = new JsonObject();
                for (fun.photon.clickgui.theme.ThemeSlot s : fun.photon.clickgui.theme.ThemeSlot.values()) {
                    obj.addProperty(s.name(), c.get(s));
                }
                byte[] cj = gson.toJson(obj).getBytes(StandardCharsets.UTF_8);
                Files.write(themeDir.resolve(themeFileName(c.getName())), wrap(cj));
            }
            index.add("customs", customs);
            byte[] json = gson.toJson(index).getBytes(StandardCharsets.UTF_8);
            Files.write(themeDir.resolve(THEMES_CONFIG + EXT), wrap(json));
        } catch (IOException e) {
            System.err.println("[Photon] failed to save themes");
            e.printStackTrace();
        }
    }

    public void loadThemes() {
        Path path = themeDir.resolve(THEMES_CONFIG + EXT);
        if (!Files.exists(path)) return;
        try {
            byte[] json = unwrap(Files.readAllBytes(path));
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            fun.photon.clickgui.theme.ThemeManager tm = fun.photon.clickgui.theme.ThemeManager.get();

            if (root.has("custom")) {
                applyThemeColors(tm.getOrCreateCustom("Custom"), root.getAsJsonObject("custom"));
            }

            if (root.has("customs")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("customs")) {
                    String name = el.getAsString();
                    Path cf = themeDir.resolve(themeFileName(name));
                    if (!Files.exists(cf)) continue;
                    JsonObject obj = JsonParser.parseString(
                            new String(unwrap(Files.readAllBytes(cf)), StandardCharsets.UTF_8)).getAsJsonObject();
                    applyThemeColors(tm.getOrCreateCustom(name), obj);
                }
            }
            if (root.has("selected")) {
                tm.selectByName(root.get("selected").getAsString());
            }
        } catch (Exception e) {
            System.err.println("[Photon] failed to load themes");
            e.printStackTrace();
        }
    }

    private void applyThemeColors(fun.photon.clickgui.theme.Theme t, JsonObject obj) {
        if (t == null || obj == null) return;
        for (fun.photon.clickgui.theme.ThemeSlot s : fun.photon.clickgui.theme.ThemeSlot.values()) {
            if (obj.has(s.name())) t.set(s, obj.get(s.name()).getAsInt());
        }
    }

    public void saveAlts() {
        fun.photon.alt.AltManager am = fun.photon.alt.AltManager.get();
        JsonObject root = new JsonObject();
        root.addProperty("schema", SCHEMA_VERSION);
        root.addProperty("current", am.getCurrent());
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (fun.photon.alt.Alt a : am.getAlts()) {
            JsonObject o = new JsonObject();
            o.addProperty("name", a.getName());
            o.addProperty("starred", a.isStarred());
            arr.add(o);
        }
        root.add("alts", arr);

        byte[] json = gson.toJson(root).getBytes(StandardCharsets.UTF_8);
        try {
            Files.createDirectories(altDir);
            Files.write(altDir.resolve(ALTS_CONFIG + EXT), wrap(json));
        } catch (IOException e) {
            System.err.println("[Photon] failed to save alts");
            e.printStackTrace();
        }
    }

    public void loadAlts() {
        Path path = altDir.resolve(ALTS_CONFIG + EXT);
        if (!Files.exists(path)) return;
        try {
            byte[] json = unwrap(Files.readAllBytes(path));
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            fun.photon.alt.AltManager am = fun.photon.alt.AltManager.get();
            am.getAlts().clear();
            if (root.has("alts")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("alts")) {
                    JsonObject o = el.getAsJsonObject();
                    if (o.has("name")) {
                        boolean starred = o.has("starred") && o.get("starred").getAsBoolean();
                        am.getAlts().add(new fun.photon.alt.Alt(o.get("name").getAsString(), starred));
                    }
                }
            }
            if (root.has("current")) {
                am.restore(root.get("current").getAsString());
            }
        } catch (Exception e) {
            System.err.println("[Photon] failed to load alts");
            e.printStackTrace();
        }
    }

    public void saveFriends() {
        JsonObject root = new JsonObject();
        root.addProperty("schema", SCHEMA_VERSION);
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (String name : fun.photon.friend.FriendManager.get().getFriends()) {
            arr.add(name);
        }
        root.add("friends", arr);

        byte[] json = gson.toJson(root).getBytes(StandardCharsets.UTF_8);
        try {
            Files.createDirectories(altDir);
            Files.write(altDir.resolve(FRIENDS_CONFIG + EXT), wrap(json));
        } catch (IOException e) {
            System.err.println("[Photon] failed to save friends");
            e.printStackTrace();
        }
    }

    public void loadFriends() {
        Path path = altDir.resolve(FRIENDS_CONFIG + EXT);
        if (!Files.exists(path)) return;
        try {
            byte[] json = unwrap(Files.readAllBytes(path));
            JsonObject root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8)).getAsJsonObject();
            fun.photon.friend.FriendManager fm = fun.photon.friend.FriendManager.get();
            fm.getFriends().clear();
            if (root.has("friends")) {
                for (com.google.gson.JsonElement el : root.getAsJsonArray("friends")) {
                    fm.getFriends().add(el.getAsString());
                }
            }
        } catch (Exception e) {
            System.err.println("[Photon] failed to load friends");
            e.printStackTrace();
        }
    }

    private JsonObject serializeModules() {
        JsonObject root = new JsonObject();
        root.addProperty("schema", SCHEMA_VERSION);
        JsonObject mods = new JsonObject();
        for (Module m : Photon.getInstance().getModuleManager().getModules()) {
            JsonObject mo = new JsonObject();
            mo.addProperty("enabled", m.isEnabled());
            mo.addProperty("hidden", m.isHidden());
            JsonObject settings = new JsonObject();
            for (fun.photon.setting.Setting<?> s : m.getSettings()) {
                settings.add(s.getName(), s.save());
            }
            mo.add("settings", settings);
            m.writeExtra(mo);
            mods.add(m.getName(), mo);
        }
        root.add("modules", mods);
        return root;
    }

    private void applyModules(JsonObject root) {
        if (!root.has("modules")) return;
        JsonObject mods = root.getAsJsonObject("modules");
        for (Module m : Photon.getInstance().getModuleManager().getModules()) {
            if (!mods.has(m.getName())) continue;
            JsonObject mo = mods.getAsJsonObject(m.getName());

            if (mo.has("settings")) {
                JsonObject settings = mo.getAsJsonObject("settings");
                for (fun.photon.setting.Setting<?> s : m.getSettings()) {
                    if (settings.has(s.getName())) {
                        s.load(settings.get(s.getName()));
                    }
                }
            } else if (mo.has("key")) {

                m.getBind().load(mo.get("key"));
            }

            if (mo.has("hidden")) {
                m.setHidden(mo.get("hidden").getAsBoolean());
            }

            if (mo.has("enabled")) {
                m.setEnabled(mo.get("enabled").getAsBoolean());
            }

            m.readExtra(mo);
        }
    }

    private byte[] wrap(byte[] json) {
        byte[] plain = new byte[MAGIC.length + 1 + json.length];
        System.arraycopy(MAGIC, 0, plain, 0, MAGIC.length);
        plain[MAGIC.length] = FORMAT_VERSION;
        System.arraycopy(json, 0, plain, MAGIC.length + 1, json.length);
        return codec.encode(plain);
    }

    private byte[] unwrap(byte[] file) {

        try {
            byte[] plain = codec.decode(file);
            byte[] json = stripMagic(plain);
            if (json != null) return json;
        } catch (Exception ignored) {
        }

        try {
            if (file.length > MAGIC.length + 1 && startsWithMagic(file)) {
                byte[] payload = new byte[file.length - MAGIC.length - 1];
                System.arraycopy(file, MAGIC.length + 1, payload, 0, payload.length);
                return codec.decode(payload);
            }
        } catch (Exception ignored) {
        }

        String s = new String(file, StandardCharsets.UTF_8).trim();
        if (s.startsWith("{")) return file;

        throw new IllegalArgumentException("config unreadable (bad key or corrupted)");
    }

    private boolean startsWithMagic(byte[] b) {
        for (int i = 0; i < MAGIC.length; i++) if (b[i] != MAGIC[i]) return false;
        return true;
    }

    private byte[] stripMagic(byte[] plain) {
        if (plain.length < MAGIC.length + 1) return null;
        if (!startsWithMagic(plain)) return null;
        byte[] json = new byte[plain.length - MAGIC.length - 1];
        System.arraycopy(plain, MAGIC.length + 1, json, 0, json.length);
        return json;
    }
}
