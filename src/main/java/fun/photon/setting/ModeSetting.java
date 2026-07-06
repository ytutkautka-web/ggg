package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {

    private final List<String> options;

    public ModeSetting(String name, String defaultValue, String... options) {
        super(name, defaultValue);
        this.options = Arrays.asList(options);
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode);
    }

    public void cycle() {
        int i = options.indexOf(getValue());
        setValue(options.get((i + 1) % options.size()));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void load(JsonElement element) {
        String v = element.getAsString();
        if (options.contains(v)) {
            setValue(v);
        }
    }
}
