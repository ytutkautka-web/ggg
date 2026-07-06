package fun.photon.utils.player;

import fun.photon.utils.IMinecraft;
import net.minecraft.world.phys.Vec3;

public class MoveUtil implements IMinecraft {

    public static void setSpeed(double speed) {
        if (mc.player == null) return;
        float yaw = mc.player.getYRot();
        double forward = InputUtil.getForward();
        double strafe = InputUtil.getStrafe();

        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += (float) (forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += (float) (forward > 0.0D ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }

        if (forward == 0.0D && strafe == 0.0D) {
            mc.player.setDeltaMovement(0.0D, mc.player.getDeltaMovement().y, 0.0D);
        } else {
            double sin = Math.sin(Math.toRadians((double) (yaw + 90.0F)));
            double cos = Math.cos(Math.toRadians((double) (yaw + 90.0F)));
            mc.player.setDeltaMovement(
                (forward * speed * cos + strafe * speed * sin),
                mc.player.getDeltaMovement().y,
                (forward * speed * sin - strafe * speed * cos)
            );
        }
    }

    public static double getSpeed() {
        if (mc.player == null) return 0;
        Vec3 velocity = mc.player.getDeltaMovement();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    public static void setSprinting(boolean sprinting) {
        if (mc.player == null) return;
        mc.player.setSprinting(sprinting);
    }
}