package fun.photon;

import fun.photon.command.CommandManager;
import fun.photon.config.ConfigManager;
import fun.photon.events.EventBus;
import fun.photon.module.ModuleManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Photon {

    public static final String RESOURCE_ROOT = "/Photon";

    public static final String FONTS_PATH = RESOURCE_ROOT + "/fonts";

    public static final String IMAGES_PATH = RESOURCE_ROOT + "/images";

    public static final Path DATA_ROOT = resolveDataRoot();

    public static final Path MODULE_DIR = DATA_ROOT;

    public static final Path THEME_DIR = DATA_ROOT.resolve("Theme");

    public static final Path ALT_DIR = DATA_ROOT.resolve("Alt");

    private static Path resolveDataRoot() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return Paths.get("C:\\Proton");
        }
        return Paths.get(System.getProperty("user.home"), "Proton");
    }

    public static String fontResource(String fileName) {
        return FONTS_PATH + "/" + fileName;
    }

    public static String imageResource(String fileName) {
        return IMAGES_PATH + "/" + fileName;
    }

    private static Photon instance;

    private final EventBus eventBus;
    private final ModuleManager moduleManager;
    private final ConfigManager configManager;
    private final CommandManager commandManager;

    private Photon() {
        this.eventBus = new EventBus();
        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();
        this.commandManager = new CommandManager();
    }

    public static Photon getInstance() {
        if (instance == null) {
            instance = new Photon();
        }
        return instance;
    }

    public void init() {
        System.out.println("[Photon] Initializing client...");
        this.moduleManager.init();
        this.eventBus.register(this);
        this.configManager.init();
        this.commandManager.init();
        System.out.println("[Photon] Initialized successfully!");
    }

    @fun.photon.events.EventTarget
    public void onKey(fun.photon.events.impl.EventKey event) {
        handleBind(event.getKey(), true);
    }

    public void handleBind(int code, boolean pressed) {
        if (code == fun.photon.utils.KeyUtil.NONE) return;
        for (fun.photon.module.Module module : moduleManager.getModules()) {
            if (module.getKey() != code) continue;
            if (module.getBind().isHold()) {
                module.setEnabled(pressed);
            } else if (pressed) {
                module.toggle();
            }
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
