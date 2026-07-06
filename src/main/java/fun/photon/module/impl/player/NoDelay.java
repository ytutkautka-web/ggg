package fun.photon.module.impl.player;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.MultiBooleanSetting;
import fun.photon.utils.IMinecraft;

import java.util.LinkedHashMap;
import java.util.Map;

@ModuleInfo(
        name = "NoDelay",
        category = Category.PLAYER,
        description = "Убирает задержку действий"
)
public class NoDelay extends Module implements IMinecraft {

    private final MultiBooleanSetting actions = register(new MultiBooleanSetting("Действия", defaults()));

    private static Map<String, Boolean> defaults() {
        Map<String, Boolean> m = new LinkedHashMap<>();
        m.put("ПКМ", false);
        m.put("Опыт", true);
        m.put("Прыжок", false);
        return m;
    }

    private static java.lang.reflect.Field RIGHT;
    private static java.lang.reflect.Field JUMP;

    private boolean holdingExp() {
        return mc.player.getMainHandItem().is(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE)
                || mc.player.getOffhandItem().is(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE);
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;
        try {
            boolean right = actions.isOn("ПКМ") || (actions.isOn("Опыт") && holdingExp());
            if (right) {
                if (RIGHT == null) {
                    RIGHT = net.minecraft.client.Minecraft.class.getDeclaredField("rightClickDelay");
                    RIGHT.setAccessible(true);
                }
                RIGHT.setInt(mc, 0);
            }
            if (actions.isOn("Прыжок")) {
                if (JUMP == null) {
                    JUMP = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("noJumpDelay");
                    JUMP.setAccessible(true);
                }
                JUMP.setInt(mc.player, 0);
            }
        } catch (Exception ignored) {
        }
    }
}
