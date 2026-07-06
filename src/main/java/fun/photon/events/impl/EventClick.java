package fun.photon.events.impl;

public class EventClick {

    public static final int ACTION_RELEASE = 0;
    public static final int ACTION_PRESS = 1;

    private final double x;
    private final double y;
    private final int button;
    private final int action;
    private boolean cancelled;

    public EventClick(double x, double y, int button, int action) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.action = action;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public boolean isPressed() {
        return action == ACTION_PRESS;
    }

    public boolean isReleased() {
        return action == ACTION_RELEASE;
    }

    public boolean isLeft() {
        return button == 0;
    }

    public boolean isRight() {
        return button == 1;
    }

    public boolean isInside(float rx, float ry, float w, float h) {
        return x >= rx && x <= rx + w && y >= ry && y <= ry + h;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
