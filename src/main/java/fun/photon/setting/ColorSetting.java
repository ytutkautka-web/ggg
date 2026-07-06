package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, int defaultArgb) {
        super(name, defaultArgb);
    }

    public int get() {
        return getValue();
    }

    public void set(int argb) {
        setValue(argb);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void load(JsonElement element) {
        setValue(element.getAsInt());
    }
}
