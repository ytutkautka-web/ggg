package fun.photon.utils.render;

public final class RenderTransform {

    public static float scale = 1f;
    public static float pivotX = 0f;
    public static float pivotY = 0f;
    public static float alpha = 1f;

    private RenderTransform() {}

    public static void set(float scale, float pivotX, float pivotY, float alpha) {
        RenderTransform.scale = scale;
        RenderTransform.pivotX = pivotX;
        RenderTransform.pivotY = pivotY;
        RenderTransform.alpha = alpha;
    }

    public static void reset() {
        scale = 1f;
        pivotX = 0f;
        pivotY = 0f;
        alpha = 1f;
    }

    public static float tx(float x) {
        return pivotX + (x - pivotX) * scale;
    }

    public static float ty(float y) {
        return pivotY + (y - pivotY) * scale;
    }
}
