package net.minecraft.client.proton;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProtonSkinLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Supplier<PlayerSkin>> CACHE = new ConcurrentHashMap<>();

    public static PlayerSkin getSkin(ProtonAccount p_account) {
        String s = p_account.getName();
        Supplier<PlayerSkin> supplier = CACHE.get(s.toLowerCase());
        if (supplier == null) {
            CACHE.put(s.toLowerCase(), () -> DefaultPlayerSkin.get(p_account.getOfflineId()));
            resolve(p_account);
            return DefaultPlayerSkin.get(p_account.getOfflineId());
        }

        return supplier.get();
    }

    private static void resolve(ProtonAccount p_account) {
        String s = p_account.getName();
        Util.nonCriticalIoPool().execute(() -> {
            try {
                UUID uuid = fetchUuid(s);
                if (uuid == null) {
                    return;
                }

                Minecraft minecraft = Minecraft.getInstance();
                ProfileResult profileresult = minecraft.services().sessionService().fetchProfile(uuid, false);
                if (profileresult == null) {
                    return;
                }

                GameProfile gameprofile = profileresult.profile();
                minecraft.execute(() -> CACHE.put(s.toLowerCase(), minecraft.getSkinManager().createLookup(gameprofile, false)));
            } catch (Exception exception) {
                LOGGER.warn("Failed to resolve skin for {}", s, exception);
            }
        });
    }

    private static UUID fetchUuid(String p_name) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + p_name);
        try (InputStream inputstream = url.openStream()) {
            String s = new String(inputstream.readAllBytes(), StandardCharsets.UTF_8);
            if (s.isEmpty()) {
                return null;
            }

            JsonObject jsonobject = JsonParser.parseString(s).getAsJsonObject();
            if (!jsonobject.has("id")) {
                return null;
            }

            String s1 = jsonobject.get("id").getAsString();
            if (s1.length() != 32) {
                return null;
            }

            String s2 = s1.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            return UUID.fromString(s2);
        }
    }
}
