package fun.photon.command.impl;

import fun.photon.Photon;
import fun.photon.command.Command;
import fun.photon.command.CommandManager;
import fun.photon.module.impl.render.Hud;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class StaffListCommand extends Command {

    public StaffListCommand() {
        super("stafflist", ".stafflist <add|remove|clear|list> [ник]", "Управление списком стаффа", "staff", "sl");
    }

    private Set<String> names() {
        Set<String> set = new LinkedHashSet<>();
        String raw = fun.photon.hud.element.StaffListElement.names;
        if (raw != null) {
            for (String s : raw.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) set.add(t);
            }
        }
        return set;
    }

    private void save(Set<String> set) {
        fun.photon.hud.element.StaffListElement.names = String.join(",", set);
        Photon.getInstance().getConfigManager().notifyChanged();
    }

    @Override
    public void execute(String[] args) {
        if (Hud.INSTANCE == null) {
            CommandManager.error("HUD не загружен");
            return;
        }
        if (args.length < 1) {
            CommandManager.error("Используй: " + getUsage());
            return;
        }
        String sub = args[0].toLowerCase();
        Set<String> set = names();
        switch (sub) {
            case "add": {
                if (args.length < 2) { CommandManager.error("Укажи ник"); return; }
                if (set.add(args[1])) {
                    save(set);
                    CommandManager.success("Добавлен: " + args[1]);
                } else {
                    CommandManager.error("Уже в списке: " + args[1]);
                }
                break;
            }
            case "remove":
            case "del": {
                if (args.length < 2) { CommandManager.error("Укажи ник"); return; }
                boolean removed = set.removeIf(n -> n.equalsIgnoreCase(args[1]));
                if (removed) {
                    save(set);
                    CommandManager.success("Удалён: " + args[1]);
                } else {
                    CommandManager.error("Нет в списке: " + args[1]);
                }
                break;
            }
            case "clear": {
                save(new LinkedHashSet<>());
                CommandManager.success("Список стаффа очищен");
                break;
            }
            case "list": {
                if (set.isEmpty()) CommandManager.send("Список пуст");
                else CommandManager.send("Стафф (" + set.size() + "): " + String.join(", ", new ArrayList<>(set)));
                break;
            }
            default:
                CommandManager.error("Используй: " + getUsage());
        }
    }
}
