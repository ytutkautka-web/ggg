package fun.photon.utils;

public final class TpsTracker {

    private static long lastGameTime = -1;
    private static long lastWall;
    private static float tps = 20f;

    private TpsTracker() {}

    public static void onTimeSync(long gameTime) {
        long now = System.currentTimeMillis();
        if (lastGameTime >= 0 && now > lastWall) {
            long ticks = gameTime - lastGameTime;
            if (ticks > 0) {
                float inst = ticks * 1000f / (now - lastWall);
                tps += (Math.min(inst, 20f) - tps) * 0.35f;
            }
        }
        lastGameTime = gameTime;
        lastWall = now;
    }

    public static void reset() {
        lastGameTime = -1;
        tps = 20f;
    }

    public static float get() {
        return Math.max(0f, tps);
    }
}
