package fun.photon.hud.element;

import fun.photon.hud.ListElement;
import fun.photon.hud.RowList;
import fun.photon.module.impl.render.Hud;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class StaffListElement extends ListElement {

    public static String names = "";

    public StaffListElement() {
        super("stafflist", "Стафф", 6, 280);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.stafflist.get();
    }

    @Override
    protected String header() {
        return "Стафф";
    }

    @Override
    protected int headerColor() {
        return list.cached().isEmpty() ? accent() : 0xFFFF3B30;
    }

    @Override
    protected boolean chip() {
        return true;
    }

    private List<String> manual() {
        List<String> out = new ArrayList<>();
        if (names != null) {
            for (String s : names.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) out.add(t.toLowerCase());
            }
        }
        return out;
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> build() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        if (mc.level == null || mc.player == null) return map;
        List<String> wanted = manual();

        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player) continue;
            String nm = p.getName().getString();
            boolean spec = p.isSpectator();
            boolean ghost = mc.getConnection() != null && mc.getConnection().getPlayerInfo(p.getUUID()) == null;
            boolean manual = wanted.contains(nm.toLowerCase());
            if (!(spec || ghost || manual)) continue;

            String tag = spec ? "спек" : ghost ? "ваниш" : "";
            RowList.Row r = new RowList.Row(nm, tag.isEmpty() ? null : tag, 0, 0);
            r.head = p.getSkin().body().texturePath();
            map.put(p.getUUID().toString(), r);
        }
        return map;
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> sample() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        RowList.Row r = new RowList.Row("Notch", "ваниш", 0, 0);
        map.put("sample", r);
        return map;
    }
}
