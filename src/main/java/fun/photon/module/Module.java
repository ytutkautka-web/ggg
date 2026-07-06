package fun.photon.module;

import fun.photon.setting.BindSetting;
import fun.photon.setting.Setting;
import fun.photon.utils.KeyUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();

    private final BindSetting bind;
    private int defaultKey;
    private boolean enabled;
    private boolean hidden;

    public Module() {
        Class<? extends Module> clazz = this.getClass();
        if (!clazz.isAnnotationPresent(ModuleInfo.class)) {
            throw new IllegalStateException("Module " + clazz.getName() + " is missing @ModuleInfo annotation");
        }

        ModuleInfo info = clazz.getAnnotation(ModuleInfo.class);
        this.name = info.name();
        this.description = info.description();
        this.category = info.category();
        this.enabled = false;
        this.bind = register(new BindSetting("Bind", 0));
    }

    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        fun.photon.events.EventBus bus = fun.photon.Photon.getInstance().getEventBus();
        if (enabled) {
            bus.register(this);
            onEnable();
        } else {
            onDisable();
            bus.unregister(this);
        }
        notify(enabled);
        onChanged();
    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    public void writeExtra(com.google.gson.JsonObject obj) {

    }

    public void readExtra(com.google.gson.JsonObject obj) {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public BindSetting getBind() {
        return bind;
    }

    public int getKey() {
        return bind.getCode();
    }

    public void setKey(int key) {
        bind.setCode(key);
    }

    public int getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(int key) {
        this.defaultKey = key;
    }

    public boolean hasCustomBind() {
        return getKey() != KeyUtil.NONE && getKey() != defaultKey;
    }

    protected boolean notifyToggle() {
        return getCategory() != Category.RENDER;
    }

    private void notify(boolean enabled) {
        if (!notifyToggle()) return;
        if (enabled) fun.photon.notify.NotificationManager.enable(name);
        else fun.photon.notify.NotificationManager.disable(name);
    }

    private void onChanged() {
        fun.photon.Photon.getInstance().getConfigManager().notifyChanged();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        fun.photon.Photon.getInstance().getConfigManager().notifyChanged();
    }
}
