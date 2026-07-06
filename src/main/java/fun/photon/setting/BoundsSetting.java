package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BoundsSetting extends Setting<double[]> {

    private final double min;
    private final double max;
    private final double step;

    public BoundsSetting(String name, double low, double high, double min, double max, double step) {
        super(name, new double[]{low, high});
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getLow()  { return value[0]; }
    public double getHigh() { return value[1]; }
    public double getMin()  { return min; }
    public double getMax()  { return max; }
    public double getStep() { return step; }

    public void setLow(double low) {
        low = clamp(Math.min(low, value[1]));
        if (low != value[0]) { value[0] = low; onChanged(); }
    }

    public void setHigh(double high) {
        high = clamp(Math.max(high, value[0]));
        if (high != value[1]) { value[1] = high; onChanged(); }
    }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public JsonElement save() {
        JsonObject o = new JsonObject();
        o.addProperty("low", value[0]);
        o.addProperty("high", value[1]);
        return o;
    }

    @Override
    public void load(JsonElement element) {
        JsonObject o = element.getAsJsonObject();
        if (o.has("low"))  value[0] = clamp(o.get("low").getAsDouble());
        if (o.has("high")) value[1] = clamp(o.get("high").getAsDouble());
        onChanged();
    }
}
