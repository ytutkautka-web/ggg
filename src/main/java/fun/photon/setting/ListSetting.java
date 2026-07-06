package fun.photon.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ListSetting extends Setting<Set<String>> {

    private final List<String> options;

    public ListSetting(String name, List<String> options, String... selected) {
        super(name, new LinkedHashSet<>(Arrays.asList(selected)));
        this.options = options;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isSelected(String option) {
        return value.contains(option);
    }

    public void set(String option, boolean on) {
        if (!options.contains(option)) return;
        boolean changed = on ? value.add(option) : value.remove(option);
        if (changed) onChanged();
    }

    public void toggle(String option) {
        set(option, !isSelected(option));
    }

    @Override
    public JsonElement save() {
        JsonArray arr = new JsonArray();
        value.forEach(arr::add);
        return arr;
    }

    @Override
    public void load(JsonElement element) {
        value.clear();
        for (JsonElement e : element.getAsJsonArray()) {
            String v = e.getAsString();
            if (options.contains(v)) value.add(v);
        }
        onChanged();
    }
}
