package fun.photon.module.impl.player;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.utils.IMinecraft;
import net.minecraft.client.gui.screens.DeathScreen;

@ModuleInfo(
    name = "AutoRespawn",
    category = Category.PLAYER,
    description = "Автоматически возрождает вас после смерти"
)
public class AutoRespawn extends Module implements IMinecraft {

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;
        if (mc.player.isDeadOrDying() || mc.screen instanceof DeathScreen) {
            mc.player.respawn();
            if (mc.screen instanceof DeathScreen) {
                mc.setScreen(null);
            }
        }
    }
}
