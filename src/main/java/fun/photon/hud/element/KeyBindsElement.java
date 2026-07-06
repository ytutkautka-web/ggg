package fun.photon.hud.element;

import fun.photon.Photon;
import fun.photon.hud.ListElement;
import fun.photon.hud.RowList;
import fun.photon.module.Module;
import fun.photon.module.impl.render.Hud;
import fun.photon.utils.KeyUtil;

import java.util.LinkedHashMap;

public class KeyBindsElement extends ListElement {

    public KeyBindsElement() {
        super("keybinds", "Бинды", 6, 200);
    }

    @Override
    public boolean enabled() {
        return Hud.INSTANCE != null && Hud.INSTANCE.keybinds.get();
    }

    @Override
    protected String header() {
        return "Бинды";
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> build() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        for (Module m : Photon.getInstance().getModuleManager().getModules()) {
            if (m.hasCustomBind() && !m.isHidden() && m.isEnabled()) {
                map.put(m.getName(), new RowList.Row(m.getName(), "[" + KeyUtil.getName(m.getKey()) + "]", 0, 0));
            }
        }
        return map;
    }

    @Override
    protected LinkedHashMap<String, RowList.Row> sample() {
        LinkedHashMap<String, RowList.Row> map = new LinkedHashMap<>();
        map.put("sample", new RowList.Row("ClickGui", "[RSHIFT]", 0, 0));
        return map;
    }
}
