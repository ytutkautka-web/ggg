package fun.photon.utils.math;

public final class Mathf {

    private Mathf() {}

    public static double clamp(double min, double max, double n) {
        return Math.max(min, Math.min(max, n));
    }

    public static float clamp01(float x) {
        return Math.max(0f, Math.min(1f, x));
    }

    public static double step(final double value, final double steps) {
        double a = Math.round(value / steps) * steps;
        a *= 1000;
        a = (int) a;
        return a / 1000.0;
    }

    public static float lerp(float min, float max, float delta) {
        return min + (max - min) * delta;
    }

    public static double lerp(double min, double max, double delta) {
        return min + (max - min) * delta;
    }

    public static float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public static float limit(float current, float inMin, float inMax, float outMin, float outMax) {
        return outMin + (outMax - outMin) * ((current - inMin) / (inMax - inMin));
    }
}
