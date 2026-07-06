package fun.photon.utils.math;

public final class Interpolator {

    private Interpolator() {}

    public static double lerp(double input, double target, double step) {
        return input + step * (target - input);
    }

    public static float lerp(float input, float target, float step) {
        return input + step * (target - input);
    }
}
