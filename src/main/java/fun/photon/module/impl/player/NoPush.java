package fun.photon.module.impl.player;

import fun.photon.Photon;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.MultiBooleanSetting;

import java.util.LinkedHashMap;
import java.util.Map;

@ModuleInfo(
    name = "NoPush",
    category = Category.PLAYER,
    description = "Отменяет влияние на вас от столкновений с игроками, блоками, либо водой (на выбор)"
)
public class NoPush extends Module {

    private static final Map<String, Boolean> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("Игроков", true);
        DEFAULTS.put("Блоков", false);
        DEFAULTS.put("Воды", false);
    }

    private final MultiBooleanSetting modes = register(new MultiBooleanSetting("Не отталкиваться от", DEFAULTS));

    public boolean isOn(String option) {
        return modes.isOn(option);
    }

    private static NoPush get() {
        return Photon.getInstance().getModuleManager().getModule(NoPush.class);
    }

    public static boolean noEntityPush() {
        NoPush m = get();
        return m != null && m.isEnabled() && m.isOn("Игроков");
    }

    public static boolean noBlockPush() {
        NoPush m = get();
        return m != null && m.isEnabled() && m.isOn("Блоков");
    }

    public static boolean noWaterPush() {
        NoPush m = get();
        return m != null && m.isEnabled() && m.isOn("Воды");
    }
}
