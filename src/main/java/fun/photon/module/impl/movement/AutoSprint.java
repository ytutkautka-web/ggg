package fun.photon.module.impl.movement;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.utils.IMinecraft;
import fun.photon.utils.player.MoveUtil;
import fun.photon.utils.player.InputUtil;

@ModuleInfo(
    name = "AutoSprint",
    category = Category.MOVEMENT
)
public class AutoSprint extends Module implements IMinecraft {

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;
        if (InputUtil.isMoving() && !mc.player.isCrouching() && !mc.player.horizontalCollision) {
            MoveUtil.setSprinting(true);
        }
    }

    @Override
    protected void onDisable() {
        MoveUtil.setSprinting(false);
    }
}