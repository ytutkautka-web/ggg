package fun.photon.utils.player;

import fun.photon.utils.IMinecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

public class InventoryUtil implements IMinecraft {

    public static Slot getSlot(Item item) {
        if (mc.player == null) return null;
        return mc.player.containerMenu.slots.stream()
                .filter(s -> !s.getItem().isEmpty() && s.getItem().getItem() == item)
                .findFirst()
                .orElse(null);
    }

    public static int getCount(Item item) {
        if (mc.player == null) return 0;
        return mc.player.containerMenu.slots.stream()
                .filter(s -> !s.getItem().isEmpty() && s.getItem().getItem() == item)
                .mapToInt(s -> s.getItem().getCount())
                .sum();
    }

    public static int getItemInHotbar(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (!mc.player.getInventory().getItem(i).isEmpty() && mc.player.getInventory().getItem(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void setHotbarSlot(int slot) {
        if (mc.player != null && slot >= 0 && slot < 9) {
            mc.player.getInventory().setSelectedSlot(slot);
        }
    }

    public static void clickSlot(int windowId, int slotId, int buttonId, ClickType clickType) {
        if (mc.gameMode == null || mc.player == null) return;
        mc.gameMode.handleInventoryMouseClick(windowId, slotId, buttonId, clickType, mc.player);
    }

    public static void clickSlot(int slotId, int buttonId, ClickType clickType) {
        if (mc.player == null) return;
        clickSlot(mc.player.containerMenu.containerId, slotId, buttonId, clickType);
    }

    public static void moveItem(int from, int to) {
        if (from == to || from == -1 || mc.player == null) return;

        clickSlot(from, 0, ClickType.PICKUP);

        clickSlot(to, 0, ClickType.PICKUP);

        clickSlot(from, 0, ClickType.PICKUP);
    }
}