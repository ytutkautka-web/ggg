package fun.photon.module;

import fun.photon.module.impl.movement.*;
import fun.photon.module.impl.player.*;
import fun.photon.module.impl.render.*;
import fun.photon.module.impl.сombat.NoFriendDamage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {

        register(new AutoSprint(), "");
        register(new NoPush(), "");
        register(new NoDelay(), "");
        register(new RenderTest(), "");
        register(new Fullbright(), "");
        register(new Chams(), "");
        register(new ChinaHat(), "");
        register(new SeeInvisible(), "");
        register(new BlockOverlay(), "");
        register(new Hud(), "");
        register(new ItemScroller(), "");
        register(new DeathCoords(), "");
        register(new AutoRespawn(), "");
        register(new AutoLeave(), "");
        register(new AutoFish(), "");
        register(new NoFriendDamage(), "");
        register(new FreeCam(), "");
        register(new ClickGui(), "RSHIFT");
    }

    public void register(Module module) {
        this.modules.add(module);
    }

    public void register(Module module, String defaultKey) {
        int code = fun.photon.utils.KeyUtil.getCode(defaultKey);
        module.setKey(code);
        module.setDefaultKey(code);
        this.modules.add(module);
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(module -> module.getCategory() == category)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (module.getClass() == clazz) {
                return (T) module;
            }
        }
        return null;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}