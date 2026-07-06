package fun.photon.command.impl;

import fun.photon.Photon;
import fun.photon.command.Command;
import fun.photon.command.CommandManager;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", ".help", "Список команд", "?");
    }

    @Override
    public void execute(String[] args) {
        CommandManager.send("§bКоманды:");
        for (Command c : Photon.getInstance().getCommandManager().getCommands()) {
            CommandManager.send("§7" + c.getUsage() + " §8— " + c.getDescription());
        }
    }
}
