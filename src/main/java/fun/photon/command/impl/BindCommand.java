package fun.photon.command.impl;

import fun.photon.Photon;
import fun.photon.command.Command;
import fun.photon.command.CommandManager;
import fun.photon.module.Module;
import fun.photon.utils.KeyUtil;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", ".bind <модуль> <клавиша|none>", "Назначить бинд модулю", "b");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            CommandManager.error("Используй: " + getUsage());
            return;
        }
        Module module = Photon.getInstance().getModuleManager().getModuleByName(args[0]);
        if (module == null) {
            CommandManager.error("Модуль не найден: " + args[0]);
            return;
        }
        String key = args[1];
        if (key.equalsIgnoreCase("none") || key.equalsIgnoreCase("null")) {
            module.setKey(KeyUtil.NONE);
            Photon.getInstance().getConfigManager().notifyChanged();
            CommandManager.success("Бинд '" + module.getName() + "' очищен");
            return;
        }
        int code = KeyUtil.getCode(key);
        if (code == KeyUtil.NONE) {
            CommandManager.error("Неизвестная клавиша: " + key);
            return;
        }
        module.setKey(code);
        Photon.getInstance().getConfigManager().notifyChanged();
        CommandManager.success("Бинд '" + module.getName() + "' → " + KeyUtil.getName(code));
    }
}
