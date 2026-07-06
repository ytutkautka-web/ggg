package net.minecraft.client.proton;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProtonAccount {
    private final String name;

    public ProtonAccount(String p_name) {
        this.name = p_name;
    }

    public String getName() {
        return this.name;
    }

    public UUID getOfflineId() {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes(StandardCharsets.UTF_8));
    }

    public User toUser() {
        return new User(this.name, this.getOfflineId(), "0", Optional.empty(), Optional.empty());
    }

    public boolean isCurrent() {
        return Minecraft.getInstance().getUser().getName().equals(this.name);
    }
}
