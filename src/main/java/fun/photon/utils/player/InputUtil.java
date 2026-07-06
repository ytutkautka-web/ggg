package fun.photon.utils.player;

import fun.photon.utils.IMinecraft;

public class InputUtil implements IMinecraft {

    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.input.getMoveVector().lengthSquared() > 0;
    }

    public static double getForward() {
        if (mc.player == null) return 0;
        return mc.player.input.getMoveVector().y;
    }

    public static double getStrafe() {
        if (mc.player == null) return 0;
        return mc.player.input.getMoveVector().x;
    }

    public static boolean isJumping() {
        if (mc.player == null) return false;
        return mc.player.input.keyPresses.jump();
    }

    public static boolean isSneaking() {
        if (mc.player == null) return false;
        return mc.player.input.keyPresses.shift();
    }
}