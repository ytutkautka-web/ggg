package fun.photon.module.impl.render;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventUpdate;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@ModuleInfo(
        name = "Fullbright",
        category = Category.RENDER,
        description = "Полная яркость"
)
public class Fullbright extends Module {

    private final ModeSetting mode =
            register(new ModeSetting("Режим", "Gamma", "Gamma", "Night Vision"));

    private static java.lang.reflect.Field GAMMA_VALUE;
    private Double savedGamma;

    @EventTarget
    public void onUpdate(EventUpdate event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mode.is("Gamma")) {
            removeNightVision(player);
            setGammaRaw(15.0);
        } else {
            restoreGamma();
            if (player != null) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
            }
        }
    }

    @Override
    protected void onDisable() {
        restoreGamma();
        removeNightVision(Minecraft.getInstance().player);
    }

    private void setGammaRaw(double value) {
        try {
            OptionInstance<Double> gamma = Minecraft.getInstance().options.gamma();
            if (savedGamma == null) savedGamma = gamma.get();
            if (GAMMA_VALUE == null) {
                GAMMA_VALUE = OptionInstance.class.getDeclaredField("value");
                GAMMA_VALUE.setAccessible(true);
            }
            GAMMA_VALUE.set(gamma, value);
        } catch (Exception ignored) {
        }
    }

    private void restoreGamma() {
        if (savedGamma == null) return;
        try {
            OptionInstance<Double> gamma = Minecraft.getInstance().options.gamma();
            if (GAMMA_VALUE == null) {
                GAMMA_VALUE = OptionInstance.class.getDeclaredField("value");
                GAMMA_VALUE.setAccessible(true);
            }
            GAMMA_VALUE.set(gamma, savedGamma);
        } catch (Exception ignored) {
        }
        savedGamma = null;
    }

    private void removeNightVision(LocalPlayer player) {
        if (player != null && player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
