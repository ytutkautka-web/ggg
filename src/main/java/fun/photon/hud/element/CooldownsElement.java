package fun.photon.hud.element;

import fun.photon.hud.ListElement;
import fun.photon.hud.RowList;
import fun.photon.module.impl.render.Hud;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;

public class CooldownsElement extends ListElement {

    public CooldownsElement() {
        super("cooldowns", "Откаты", 6, 360);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.cooldowns.get();
    }

    @Override
    protected String header() {
        return "Откаты";
    }

    @Override
    protected boolean chip() {
        return true;
    }

    private String time(int ticks) {
        int sec = ticks / 20;
        if (sec >= 60) return String.format("%d:%02d", sec / 60, sec % 60);
        float s = ticks / 20f;
        return String.format(java.util.Locale.US, "%.1f", s);
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> build() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        if (mc.player == null) return map;
        for (int i = 0; i < 9; i++) {
            ItemStack st = mc.player.getInventory().getItem(i);
            if (st.isEmpty()) continue;
            int ticks = mc.player.getCooldowns().getCooldownRemainingTicks(st);
            if (ticks <= 0) continue;
            String nm = st.getHoverName().getString();
            if (map.containsKey(nm)) continue;
            RowList.Row r = new RowList.Row(nm, time(ticks), accent(), 0);
            r.stack = st;
            map.put(nm, r);
        }
        return map;
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> sample() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        RowList.Row r = new RowList.Row("Ender Pearl", "0:08", accent(), 0);
        r.stack = new ItemStack(Items.ENDER_PEARL);
        map.put("sample", r);
        return map;
    }
}
