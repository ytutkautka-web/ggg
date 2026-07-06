package fun.photon.module.impl.render;

import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.IMinecraft;

@ModuleInfo(
    name = "FreeCam",
    category = Category.RENDER,
    description = "Отвязывает камеру от игрока и позволяет свободно летать"
)
public class FreeCam extends Module implements IMinecraft {

    private final NumberSetting speed = register(new NumberSetting("Скорость", 1.0, 0.1, 5.0, 0.1));

    private double x, y, z;
    private float yaw, pitch;
    private long lastFrameNanos = -1;

    public boolean isActive() {
        return isEnabled() && mc.player != null;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;
        x = mc.player.getX();
        y = mc.player.getY() + mc.player.getEyeHeight();
        z = mc.player.getZ();
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        lastFrameNanos = -1;
    }

    // Вызывается из Camera.setup() каждый КАДР рендера (а не игровой тик 20/сек) —
    // движение считается по реальной прошедшей дельте времени, поэтому полёт
    // остаётся плавным независимо от fps/tps и не дёргается.
    public void syncFrame() {
        if (mc.player == null) return;

        long now = System.nanoTime();
        double deltaSeconds = lastFrameNanos < 0 ? 0.0 : (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;
        // защита от рывка после лаг-спайка/сворачивания окна
        deltaSeconds = Math.min(deltaSeconds, 0.1);

        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();

        double spd = speed.get() * 20.0 * deltaSeconds; // "Скорость" задана в блоках/тик, приводим к блокам/сек
        double forward = 0.0;
        double strafe = 0.0;
        if (mc.options.keyUp.isDown()) forward += 1.0;
        if (mc.options.keyDown.isDown()) forward -= 1.0;
        if (mc.options.keyLeft.isDown()) strafe += 1.0;
        if (mc.options.keyRight.isDown()) strafe -= 1.0;

        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        x += (forward * -sin + strafe * -cos) * spd;
        z += (forward * cos + strafe * -sin) * spd;

        if (mc.options.keyJump.isDown()) y += spd;
        if (mc.options.keyShift.isDown()) y -= spd;


    }
}
