package fun.photon.notify;

public class Notification {

    public enum Type { ENABLE, DISABLE, INFO, ERROR }

    public final String title;
    public final String message;
    public final Type type;
    public final long created;
    public final long duration;

    public Notification(String title, String message, Type type, long duration) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = duration;
        this.created = System.currentTimeMillis();
    }

    public long age() {
        return System.currentTimeMillis() - created;
    }

    public boolean expired() {
        return age() > duration;
    }
}
