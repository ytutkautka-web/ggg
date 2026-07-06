package fun.photon.hud;

import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RowList {

    public static final class Row {
        public String left;
        public String right;
        public int color;
        public float pct;
        public net.minecraft.client.renderer.texture.TextureAtlasSprite sprite;
        public net.minecraft.world.item.ItemStack stack;
        public net.minecraft.resources.Identifier head;
        public int lvl;
        public boolean bad;
        public float blink = 1f;

        public Row(String left, String right, int color, float pct) {
            this.left = left;
            this.right = right;
            this.color = color;
            this.pct = pct;
        }
    }

    public static final class Item {
        public final Row row;
        public final double v;

        Item(Row row, double v) {
            this.row = row;
            this.v = v;
        }
    }

    private final long dur;
    private final Map<String, Animation> anims = new HashMap<>();
    private final Map<String, Row> data = new HashMap<>();
    private final List<String> order = new ArrayList<>();
    private List<Item> cached = new ArrayList<>();

    public RowList(long dur) {
        this.dur = dur;
    }

    public List<Item> sync(LinkedHashMap<String, Row> current) {
        Set<String> set = current.keySet();
        for (Map.Entry<String, Row> e : current.entrySet()) {
            anims.computeIfAbsent(e.getKey(), k -> new Animation(Easing.EASE_OUT_CUBIC, dur));
            data.put(e.getKey(), e.getValue());
        }
        List<String> fading = new ArrayList<>();
        for (String k : order) if (!set.contains(k) && anims.containsKey(k)) fading.add(k);
        order.clear();
        order.addAll(current.keySet());
        order.addAll(fading);

        List<Item> out = new ArrayList<>();
        List<String> dead = new ArrayList<>();
        for (String k : order) {
            Animation a = anims.get(k);
            boolean present = set.contains(k);
            a.run(present ? 1.0 : 0.0);
            double v = a.getValue();
            if (!present && v <= 0.02) {
                dead.add(k);
                continue;
            }
            out.add(new Item(data.get(k), v));
        }
        for (String k : dead) {
            anims.remove(k);
            data.remove(k);
            order.remove(k);
        }
        cached = out;
        return out;
    }

    public List<Item> cached() {
        return cached;
    }
}
