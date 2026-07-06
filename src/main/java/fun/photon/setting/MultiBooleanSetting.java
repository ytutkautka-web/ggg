package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class MultiBooleanSetting extends Setting<Map<String, Boolean>> {

    public MultiBooleanSetting(String name, Map<String, Boolean> defaults) {
        super(name, new LinkedHashMap<>(defaults));
    }

    public boolean isOn(String option) {
        return Boolean.TRUE.equals(value.get(option));
    }

    public void set(String option, boolean on) {
        if (!value.containsKey(option)) return;
        if (isOn(option) != on) {
            value.put(option, on);
            onChanged();
        }
    }

    public void toggle(String option) {
        set(option, !isOn(option));
    }

    public Map<String, Boolean> getStates() {
        return value;
    }

    @Override
    public JsonElement save() {
        JsonObject o = new JsonObject();
        value.forEach(o::addProperty);
        return o;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject o = element.getAsJsonObject();
        for (String key : value.keySet()) {
            if (o.has(key)) value.put(key, o.get(key).getAsBoolean());
        }
        onChanged();
    }
}
