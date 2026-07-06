package fun.photon.module.impl.player;

import fun.photon.events.EventTarget;
import fun.photon.events.impl.EventRender2D;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import fun.photon.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(
        name = "ItemScroller",
        category = Category.PLAYER,
        description = "Shift + ЛКМ — быстрый перенос предметов по слотам"
)
public class ItemScroller extends Module {

    private final NumberSetting delay =
            register(new NumberSetting("Задержка", 50, 0, 450, 5));

    private static java.lang.reflect.Field HOVERED_SLOT;
    private long lastMove;
    private int lastSlot = -1;

    @EventTarget
    public void onRender(EventRender2D event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> container)) return;
        long handle = mc.getWindow().handle();
        boolean shift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shift) { lastSlot = -1; return; }
        if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            lastSlot = -1;
            return;
        }

        Slot slot = hoveredSlot(container);
        if (slot == null || !slot.hasItem()) return;
        if (slot.index == lastSlot) return;

        long now = System.currentTimeMillis();
        if (now - lastMove < delay.getInt()) return;

        mc.gameMode.handleInventoryMouseClick(
                container.getMenu().containerId, slot.index, 0, ClickType.QUICK_MOVE, mc.player);
        lastMove = now;
        lastSlot = slot.index;
    }

    private static Slot hoveredSlot(AbstractContainerScreen<?> container) {
        try {
            if (HOVERED_SLOT == null) {
                HOVERED_SLOT = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
                HOVERED_SLOT.setAccessible(true);
            }
            Object value = HOVERED_SLOT.get(container);
            if (value instanceof Slot slot) return slot;
        } catch (Exception ignored) {
        }
        return null;
    }
}
