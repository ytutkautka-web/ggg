package fun.photon.command.impl;

import fun.photon.Photon;
import fun.photon.command.Command;
import fun.photon.command.CommandManager;
import fun.photon.config.ConfigManager;

import java.util.List;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", ".config <save|load|delete|list> [имя]", "Управление конфигами", "cfg");
    }

    @Override
    public void execute(String[] args) {
        ConfigManager cfg = Photon.getInstance().getConfigManager();
        if (args.length == 0) {
            CommandManager.error("Используй: " + getUsage());
            return;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> {
                List<String> list = cfg.listConfigs();
                if (list.isEmpty()) CommandManager.send("Конфигов нет");
                else CommandManager.send("Конфиги: " + String.join(", ", list));
            }
            case "save" -> {
                if (args.length < 2) { CommandManager.error("Укажи имя: .config save <имя>"); return; }
                String name = args[1];
                cfg.save(name);
                CommandManager.success("Сохранён конфиг '" + name + "'");
            }
            case "load" -> {
                if (args.length < 2) { CommandManager.error("Укажи имя: .config load <имя>"); return; }
                String name = args[1];
                cfg.load(name);
                CommandManager.success("Загружен конфиг '" + name + "'");
            }
            case "delete", "del" -> {
                if (args.length < 2) { CommandManager.error("Укажи имя: .config delete <имя>"); return; }
                String name = args[1];
                boolean ok = cfg.delete(name);
                if (ok) CommandManager.success("Удалён конфиг '" + name + "'");
                else CommandManager.error("Нельзя удалить '" + name + "'");
            }
            default -> CommandManager.error("Используй: " + getUsage());
        }
    }
}
