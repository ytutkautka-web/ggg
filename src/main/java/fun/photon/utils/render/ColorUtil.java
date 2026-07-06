package fun.photon.utils.render;

public final class ColorUtil {

    private ColorUtil() {}

    public static int alpha(int c) { return (c >> 24) & 0xFF; }
    public static int red(int c)   { return (c >> 16) & 0xFF; }
    public static int green(int c) { return (c >> 8) & 0xFF; }
    public static int blue(int c)  { return c & 0xFF; }

    public static float alphaf(int c) { return alpha(c) / 255f; }
    public static float redf(int c)   { return red(c) / 255f; }
    public static float greenf(int c) { return green(c) / 255f; }
    public static float bluef(int c)  { return blue(c) / 255f; }

    public static int rgba(int r, int g, int b, int a) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static int rgba(float r, float g, float b, float a) {
        return rgba(Math.round(r * 255f), Math.round(g * 255f), Math.round(b * 255f), Math.round(a * 255f));
    }

    public static int gray(int brightness) {
        return rgba(brightness, brightness, brightness, 255);
    }

    public static int gray(int brightness, int a) {
        return rgba(brightness, brightness, brightness, a);
    }

    public static int withAlpha(int c, int a) {
        return (clamp(a) << 24) | (c & 0x00FFFFFF);
    }

    public static int multAlpha(int c, float factor) {
        return withAlpha(c, Math.round(alpha(c) * factor));
    }

    public static int brighten(int c, int amount) {
        return rgba(red(c) + amount, green(c) + amount, blue(c) + amount, alpha(c));
    }

    public static int darken(int c, float factor) {
        return rgba(Math.round(red(c) * factor), Math.round(green(c) * factor),
                Math.round(blue(c) * factor), alpha(c));
    }

    public static int interpolate(int c1, int c2, double t) {
        t = Math.max(0, Math.min(1, t));
        int a = (int) Math.round(alpha(c1) + (alpha(c2) - alpha(c1)) * t);
        int r = (int) Math.round(red(c1) + (red(c2) - red(c1)) * t);
        int g = (int) Math.round(green(c1) + (green(c2) - green(c1)) * t);
        int b = (int) Math.round(blue(c1) + (blue(c2) - blue(c1)) * t);
        return rgba(r, g, b, a);
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    public static int getColor(int r, int g, int b, int a) { return rgba(r, g, b, a); }
    public static int getColor(int r, int g, int b)        { return rgb(r, g, b); }
    public static int getColor(int brightness, int a)      { return gray(brightness, a); }
    public static int getColor(int brightness)             { return gray(brightness); }

    public static int replAlpha(int c, int a) { return withAlpha(c, a); }

    public static int overCol(int c1, int c2, float t) { return interpolate(c1, c2, t); }
}
