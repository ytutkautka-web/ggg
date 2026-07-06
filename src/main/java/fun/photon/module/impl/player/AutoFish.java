package fun.photon.module.impl.player;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.NumberSetting;
import fun.photon.utils.IMinecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;

@ModuleInfo(
    name = "AutoFish",
    category = Category.PLAYER,
    description = "Автономно вылавливает добычу из воды удочкой"
)
public class AutoFish extends Module implements IMinecraft {

    private final NumberSetting recastDelay = register(new NumberSetting("Задержка заброса", 500.0, 100.0, 2000.0, 50.0));
    private final NumberSetting sensitivity = register(new NumberSetting("Чувствительность", 0.10, 0.02, 0.5, 0.01));
    private final fun.photon.setting.BooleanSetting checkFullInv = register(new fun.photon.setting.BooleanSetting("Полный инвентарь", true));

    private long lastAction;

    @Override
    protected void onEnable() {
        lastAction = System.currentTimeMillis();
    }

    @EventTarget
    @SuppressWarnings("unused")
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.getMainHandItem().is(Items.FISHING_ROD)) return;

        // Пауза, если инвентарь полон
        if (checkFullInv.get() && mc.player.getInventory().getFreeSlot() == -1) return;

        long now = System.currentTimeMillis();
        FishingHook hook = mc.player.fishing;

        // Нет поплавка в воде -> забросить удочку
        if (hook == null) {
            if (now - lastAction >= recastDelay.get()) {
                use();
                lastAction = now;
            }
            return;
        }

        // Ловим поклёвку по резкому погружению поплавка
        boolean inWater = hook.isInWater();
        double motionY = hook.getDeltaMovement().y;
        if (inWater && motionY < -sensitivity.get() && now - lastAction >= 400) {
            // Подсечка + повторный заброс
            use();
            lastAction = now;
        }
    }

    private void use() {
        if (mc.gameMode == null) return;
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
