package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean get() {
        return getValue();
    }

    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void load(JsonElement element) {
        setValue(element.getAsBoolean());
    }
}
