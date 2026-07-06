package fun.photon.module.impl.player;

import fun.photon.Photon;
import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.ModeSetting;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.ChatUtil;
import fun.photon.utils.IMinecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

@ModuleInfo(
    name = "AutoLeave",
    category = Category.PLAYER,
    description = "Покидает сервер при обнаружении рядом игрока"
)
public class AutoLeave extends Module implements IMinecraft {

    private final ModeSetting mode = register(new ModeSetting("Режим", "Disconnect", "Disconnect", "/spawn"));
    private final NumberSetting radius = register(new NumberSetting("Радиус", 30.0, 5.0, 128.0, 1.0));

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player == mc.player) continue;
            if (player.isSpectator() || !player.isAlive()) continue;

            double dist = mc.player.distanceTo(player);
            if (dist <= radius.get()) {
                trigger(player, dist);
                return;
            }
        }
    }

    private void trigger(AbstractClientPlayer player, double dist) {
        ChatUtil.add("§f" + this.getName() + " §7-> Обнаружен игрок §r"
            + player.getDisplayName().getString() + " §7на расстоянии " + (int) dist + "m");

        // Выключаем все модули
        for (Module m : Photon.getInstance().getModuleManager().getModules()) {
            if (m.isEnabled()) m.setEnabled(false);
        }

        if (mode.is("/spawn")) {
            if (mc.getConnection() != null) {
                mc.getConnection().sendCommand("spawn");
            }
        } else {
            // Откладываем дисконнект: вызов внутри тика уровня обнуляет level и крашит клиент
            mc.execute(() -> mc.disconnect(new TitleScreen(), false));
        }
    }
}
