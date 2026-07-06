package fun.photon.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fun.photon.utils.KeyUtil;

public class BindSetting extends Setting<Integer> {

    private boolean hold;

    public BindSetting(String name, int code) {
        super(name, code);
    }

    public int getCode() {
        return getValue();
    }

    public void setCode(int code) {
        setValue(code);
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public void toggleMode() {
        this.hold = !this.hold;
    }

    public String getKeyName() {
        return KeyUtil.getName(getValue());
    }

    @Override
    public JsonElement save() {
        JsonObject o = new JsonObject();
        o.addProperty("key", KeyUtil.getName(getValue()));
        o.addProperty("hold", hold);
        return o;
    }

    @Override
    public void load(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject o = element.getAsJsonObject();
            if (o.has("key")) setValue(KeyUtil.getCode(o.get("key").getAsString()));
            hold = o.has("hold") && o.get("hold").getAsBoolean();
            return;
        }

        if (element.getAsJsonPrimitive().isNumber()) {
            setValue(element.getAsInt());
        } else {
            setValue(KeyUtil.getCode(element.getAsString()));
        }
    }
}
