package net.minecraft.client.proton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProtonAccountManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ProtonAccountManager INSTANCE = new ProtonAccountManager();

    private final List<ProtonAccount> accounts = new ArrayList<>();
    private final File file;

    private ProtonAccountManager() {
        this.file = new File(Minecraft.getInstance().gameDirectory, "proton_accounts.json");
        this.load();
    }

    public static ProtonAccountManager get() {
        return INSTANCE;
    }

    public List<ProtonAccount> getAccounts() {
        return this.accounts;
    }

    public void add(String p_name) {
        if (p_name == null || p_name.trim().isEmpty()) {
            return;
        }

        String s = p_name.trim();
        for (ProtonAccount protonaccount : this.accounts) {
            if (protonaccount.getName().equalsIgnoreCase(s)) {
                return;
            }
        }

        this.accounts.add(new ProtonAccount(s));
        this.save();
    }

    public void remove(ProtonAccount p_account) {
        this.accounts.remove(p_account);
        this.save();
    }

    public void switchTo(ProtonAccount p_account) {
        Minecraft.getInstance().setUser(p_account.toUser());
    }

    private void load() {
        this.accounts.clear();
        if (!this.file.exists()) {
            return;
        }

        try {
            String s = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
            JsonElement jsonelement = JsonParser.parseString(s);
            if (jsonelement.isJsonArray()) {
                for (JsonElement jsonelement1 : jsonelement.getAsJsonArray()) {
                    String s1 = jsonelement1.getAsJsonObject().get("name").getAsString();
                    this.accounts.add(new ProtonAccount(s1));
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load proton accounts", exception);
        }
    }

    private void save() {
        try {
            JsonArray jsonarray = new JsonArray();
            for (ProtonAccount protonaccount : this.accounts) {
                com.google.gson.JsonObject jsonobject = new com.google.gson.JsonObject();
                jsonobject.addProperty("name", protonaccount.getName());
                jsonarray.add(jsonobject);
            }

            Files.write(this.file.toPath(), GSON.toJson(jsonarray).getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            LOGGER.warn("Failed to save proton accounts", exception);
        }
    }
}
