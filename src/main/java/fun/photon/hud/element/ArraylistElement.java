package fun.photon.hud.element;

import fun.photon.Photon;
import fun.photon.clickgui.Palette;
import fun.photon.hud.HudElement;
import fun.photon.module.Module;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.animation.Animation;
import fun.photon.utils.animation.Easing;
import fun.photon.utils.render.ColorUtil;
import fun.photon.utils.render.PhotonFont;
import fun.photon.utils.render.Render2DUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArraylistElement extends HudElement {

    private final Map<String, Animation> slide = new HashMap<>();

    public ArraylistElement() {
        super("arraylist", "Список модулей", 0, 4);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.arraylist.get();
    }

    private Animation slide(String k) {
        return slide.computeIfAbsent(k, x -> new Animation(Easing.EASE_OUT_CUBIC, 250));
    }

    private List<Module> mods() {
        PhotonFont f = font(13);
        List<Module> list = Photon.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .filter(m -> !m.isHidden())
                .sorted(Comparator.comparingDouble((Module m) -> f.getWidth(m.getName())).reversed())
                .collect(Collectors.toList());
        if (list.isEmpty() && EDIT) {
            return Photon.getInstance().getModuleManager().getModules().stream().limit(4).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public float width() {
        PhotonFont f = font(13);
        float max = 0;
        for (Module m : mods()) max = Math.max(max, f.getWidth(m.getName()));
        return max + s(14);
    }

    @Override
    public float height() {
        PhotonFont f = font(13);
        int n = mods().size();
        return n * (f.getHeight() + s(6)) + (n > 0 ? (n - 1) * s(2) : 0);
    }

    @Override
    public void render() {
        PhotonFont f = font(13);
        List<Module> mods = mods();
        float rowH = f.getHeight() + s(6);
        float pad = s(6);
        float w = width();
        float cy = y;

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            String nm = m.getName();
            float tw = f.getWidth(nm);
            float boxW = tw + pad * 2f + s(4);

            Animation a = slide(nm);
            a.run(boxW);
            double cur = a.getValue();
            if (cur < 1) cur = boxW;

            float bx = x + w - (float) cur;
            int col = ColorUtil.interpolate(accent(), accent2(),
                    mods.size() <= 1 ? 0 : i / (double) (mods.size() - 1));

            Render2DUtil.rounded(bx, cy, (float) cur, rowH, 0f, 0f, s(4), s(4), 1f, Palette.PANEL_BG, 0, 0f);
            Render2DUtil.rounded(x + w - s(2), cy, s(2), rowH, 0f, 0f, 0f, 0f, 1f, col, 0, 0f);
            f.drawString(nm, bx + pad, cy + (rowH - f.getHeight()) / 2f, Palette.TEXT_LIGHT);

            cy += rowH + s(2);
        }

        slide.keySet().removeIf(k -> mods.stream().noneMatch(m -> m.getName().equals(k)));
    }
}
