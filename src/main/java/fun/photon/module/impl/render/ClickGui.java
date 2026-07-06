package fun.photon.module.impl.render;

import fun.photon.clickgui.ClickGuiScreen;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import net.minecraft.client.Minecraft;

@ModuleInfo(
        name = "ClickGui",
        category = Category.RENDER,
        description = "Меню настроек чита"
)
public class ClickGui extends Module {

    private ClickGuiScreen screen;

    @Override
    protected void onEnable() {
        if (screen == null) screen = new ClickGuiScreen(this);
        Minecraft.getInstance().setScreen(screen);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (screen != null && mc.screen == screen) {
            mc.setScreen(null);
        }
    }
}
