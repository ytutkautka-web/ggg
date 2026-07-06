package fun.photon.friend;

import java.util.ArrayList;
import java.util.List;

public final class FriendManager {

    private static final FriendManager INSTANCE = new FriendManager();

    public static FriendManager get() {
        return INSTANCE;
    }

    private final List<String> friends = new ArrayList<>();

    public List<String> getFriends() {
        return friends;
    }

    public boolean isFriend(String name) {
        if (name == null) return false;
        for (String f : friends) {
            if (f.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean add(String name) {
        if (name == null || name.isEmpty() || isFriend(name)) return false;
        friends.add(name);
        save();
        return true;
    }

    public boolean remove(String name) {
        boolean removed = friends.removeIf(f -> f.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    public void clear() {
        friends.clear();
        save();
    }

    private void save() {
        try {
            fun.photon.Photon.getInstance().getConfigManager().saveFriends();
        } catch (Throwable ignored) {
        }
    }
}
