package fun.photon.module.impl.render;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventRender2D;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@ModuleInfo(
        name = "SeeInvisible",
        category = Category.RENDER,
        description = "Показывает невидимых"
)
public class SeeInvisible extends Module {

    public static SeeInvisible INSTANCE;

    private final BooleanSetting onlyPlayers = register(new BooleanSetting("Только игроки", true));
    private final BooleanSetting transparent = register(new BooleanSetting("Полупрозрачность", true));

    public SeeInvisible() {
        INSTANCE = this;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!transparent.get()) reveal();
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!transparent.get()) reveal();
    }

    public static boolean shouldGhost(Entity e) {
        SeeInvisible m = INSTANCE;
        if (m == null || !m.isEnabled() || !m.transparent.get()) return false;
        if (m.onlyPlayers.get() && !(e instanceof Player)) return false;
        return e.isInvisible();
    }

    private void reveal() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        boolean only = onlyPlayers.get();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (only && !(e instanceof Player)) continue;
            if (e.isInvisible()) e.setInvisible(false);
        }
    }
}
