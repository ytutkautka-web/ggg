package fun.photon.notify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotificationManager {

    private static final List<Notification> list = new CopyOnWriteArrayList<>();

    private NotificationManager() {}

    public static void push(Notification n) {
        list.add(n);
    }

    public static void info(String title, String message) {
        push(new Notification(title, message, Notification.Type.INFO, 3000));
    }

    public static void error(String title, String message) {
        push(new Notification(title, message, Notification.Type.ERROR, 4000));
    }

    public static void enable(String module) {
        push(new Notification(module, "Включено", Notification.Type.ENABLE, 2500));
    }

    public static void disable(String module) {
        push(new Notification(module, "Выключено", Notification.Type.DISABLE, 2500));
    }

    public static List<Notification> get() {
        return list;
    }

    public static void remove(Notification n) {
        list.remove(n);
    }
}
