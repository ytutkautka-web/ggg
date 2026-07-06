package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public double get() {
        return getValue();
    }

    public float getFloat() {
        return getValue().floatValue();
    }

    public int getInt() {
        return (int) Math.round(getValue());
    }

    public void set(double v) {
        setValue(clamp(v));
    }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void load(JsonElement element) {
        set(element.getAsDouble());
    }
}
