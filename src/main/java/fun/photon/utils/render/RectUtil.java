package fun.photon.utils.render;

public final class RectUtil {

    private RectUtil() {}

    public static void drawRect(float x, float y, float x2, float y2, int color) {
        Render2DUtil.rect(x, y, x2 - x, y2 - y, color);
    }

    public static void drawSmoothRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
        Render2DUtil.roundedRect(x, y, x2 - x, y2 - y, radius, color);
    }

    public static void drawRoundedRectCorners(float x, float y, float x2, float y2,
                                              float rTR, float rBR, float rTL, float rBL, int color) {
        Render2DUtil.rounded(x, y, x2 - x, y2 - y, rTR, rBR, rTL, rBL, 1f, color, 0, 0f);
    }

    public static void drawSmoothRoundedOutline(float x, float y, float x2, float y2,
                                                float radius, float thickness, int color) {
        Render2DUtil.roundedOutline(x, y, x2 - x, y2 - y, radius, thickness, color);
    }

    public static void drawRoundedOutlineCorners(float x, float y, float x2, float y2,
                                                 float rTR, float rBR, float rTL, float rBL,
                                                 float thickness, int color) {
        Render2DUtil.rounded(x, y, x2 - x, y2 - y, rTR, rBR, rTL, rBL, 1f, 0, color, thickness);
    }

    public static void drawSmoothRoundedRect(float x, float y, float x2, float y2, float radius,
                                             int fill, int outline, float thickness) {
        Render2DUtil.rounded(x, y, x2 - x, y2 - y, radius, radius, radius, radius, 1f, fill, outline, thickness);
    }

    public static void drawSmoothRoundedRect(float x, float y, float x2, float y2, float radius,
                                             int c1, int c2, int c3, int c4) {
        Render2DUtil.roundedRect(x, y, x2 - x, y2 - y, radius, c1);
    }

    public static void drawRoundedRectShadowed(float x, float y, float x2, float y2,
                                               float round, float shadowSize,
                                               int c1, int c2, int c3, int c4,
                                               boolean bloom, boolean sageColor,
                                               boolean rect, boolean shadow) {
        if (rect) {
            Render2DUtil.roundedRect(x, y, x2 - x, y2 - y, round, c1);
        }
    }

    public static void drawGradientV(float x, float y, float x2, float y2,
                                     int color, int color2, boolean bloom) {
        Render2DUtil.rect(x, y, x2 - x, y2 - y, color);
    }

    public static void drawShadowSegmentsExtract(float x, float y, float x2, float y2,
                                                 double radiusStart, double radiusEnd,
                                                 int c1, int c2, int c3, int c4,
                                                 boolean sageColor, boolean bloom) {

    }
}
