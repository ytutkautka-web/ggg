package fun.photon.alt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Alt {

    private final String name;
    private boolean starred;

    public Alt(String name) {
        this.name = name;
    }

    public Alt(String name, boolean starred) {
        this.name = name;
        this.starred = starred;
    }

    public String getName() {
        return name;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public UUID offlineUuid() {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}
