package fun.photon.setting;

import com.google.gson.JsonElement;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {

    private final String name;
    protected T value;
    private BooleanSupplier visible = () -> true;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        if (Objects.equals(this.value, newValue)) return;
        this.value = newValue;
        onChanged();
    }

    public Setting<T> visibleWhen(BooleanSupplier condition) {
        this.visible = condition;
        return this;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    protected void onChanged() {
        fun.photon.Photon.getInstance().getConfigManager().notifyChanged();
    }

    public abstract JsonElement save();

    public abstract void load(JsonElement element);
}
