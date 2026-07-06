package fun.photon.module.impl.player;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.utils.ChatUtil;
import fun.photon.utils.IMinecraft;

@ModuleInfo(
    name = "DeathCoords",
    category = Category.PLAYER,
    description = "Выводит ваши посмертные координаты в чат"
)
public class DeathCoords extends Module implements IMinecraft {

    private boolean wasAlive;

    @Override
    protected void onEnable() {
        wasAlive = mc.player != null && mc.player.isAlive();
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) {
            wasAlive = false;
            return;
        }

        boolean alive = mc.player.isAlive();
        if (wasAlive && !alive) {
            int x = (int) Math.floor(mc.player.getX());
            int y = (int) Math.floor(mc.player.getY());
            int z = (int) Math.floor(mc.player.getZ());
            ChatUtil.add("Координаты смерти -> §cX: " + x + ", Y: " + y + ", Z: " + z);
        }
        wasAlive = alive;
    }
}
