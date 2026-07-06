package fun.photon.command.impl;

import fun.photon.Photon;
import fun.photon.command.Command;
import fun.photon.command.CommandManager;
import fun.photon.module.Module;

public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", ".toggle <модуль>", "Вкл/выкл модуль", "t");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandManager.error("Используй: " + getUsage());
            return;
        }
        Module module = Photon.getInstance().getModuleManager().getModuleByName(args[0]);
        if (module == null) {
            CommandManager.error("Модуль не найден: " + args[0]);
            return;
        }
        module.toggle();
        CommandManager.success(module.getName() + " → " + (module.isEnabled() ? "§aON" : "§cOFF"));
    }
}
