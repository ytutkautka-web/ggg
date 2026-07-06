package fun.photon.hud.element;

import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;
import fun.photon.utils.render.RenderTransform;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorElement extends HudElement {

    public ArmorElement() {
        super("armor", "Броня", 6, 90);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.armor.get();
    }

    @Override
    public boolean active() {
        return enabled() && (EDIT || !pieces().isEmpty());
    }

    private List<ItemStack> pieces() {
        List<ItemStack> out = new ArrayList<>();
        if (mc.player == null) return out;
        net.minecraft.world.entity.EquipmentSlot[] slots = {
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET
        };
        for (net.minecraft.world.entity.EquipmentSlot sl : slots) {
            ItemStack s = mc.player.getItemBySlot(sl);
            if (s != null && !s.isEmpty()) out.add(s);
        }
        ItemStack hand = mc.player.getMainHandItem();
        if (hand != null && !hand.isEmpty()) out.add(hand);
        return out;
    }

    private List<ItemStack> sample() {
        List<ItemStack> out = new ArrayList<>();
        out.add(new ItemStack(net.minecraft.world.item.Items.NETHERITE_HELMET));
        out.add(new ItemStack(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE));
        out.add(new ItemStack(net.minecraft.world.item.Items.NETHERITE_LEGGINGS));
        out.add(new ItemStack(net.minecraft.world.item.Items.NETHERITE_BOOTS));
        out.add(new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
        return out;
    }

    private List<ItemStack> shown() {
        List<ItemStack> p = pieces();
        return p.isEmpty() && EDIT ? sample() : p;
    }

    private float icon() { return s(20); }
    private float gap() { return s(4); }
    private float pad() { return s(6); }

    private String dura(ItemStack s) {
        if (!s.isDamageableItem() || s.getDamageValue() <= 0) return null;
        return String.valueOf(s.getMaxDamage() - s.getDamageValue());
    }

    @Override
    public float width() {
        int n = Math.max(1, shown().size());
        return pad() * 2f + n * icon() + (n - 1) * gap();
    }

    @Override
    public float height() {
        return pad() * 2f + icon() + font(9).getHeight() + s(2);
    }

    @Override
    public void render() {
        List<ItemStack> items = shown();
        if (items.isEmpty()) return;
        float w = width();
        float h = height();
        panel(x, y, w, h);

        float a = RenderTransform.alpha;
        PhotonFont df = font(9);
        float cx = x + pad();
        float iy = y + pad();
        float ic = icon();
        for (ItemStack s : items) {
            float sz = ic * a;
            itemIcon(s, cx + (ic - sz) / 2f, iy + (ic - sz) / 2f, sz);
            String d = dura(s);
            if (d != null) {
                int col = duraColor(s);
                float tx = cx + ic / 2f - df.getWidth(d) / 2f;
                df.drawString(d, tx, iy + ic + s(1), col);
            }
            cx += ic + gap();
        }
    }

    private int duraColor(ItemStack s) {
        float p = 1f - (float) s.getDamageValue() / Math.max(1, s.getMaxDamage());
        if (p > 0.5f) return Palette.TEXT_LIGHT;
        if (p > 0.25f) return 0xFFFFD23B;
        return 0xFFFF554B;
    }
}
