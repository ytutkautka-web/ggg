package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {

    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public String get() {
        return getValue();
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void load(JsonElement element) {
        setValue(element.getAsString());
    }
}
