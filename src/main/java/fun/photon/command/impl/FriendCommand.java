package fun.photon.command.impl;

import fun.photon.command.Command;
import fun.photon.command.CommandManager;
import fun.photon.friend.FriendManager;

import java.util.List;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend", ".friend <add|remove|clear|list> [ник]", "Управление списком друзей", "f");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandManager.error("Используй: " + getUsage());
            return;
        }
        FriendManager fm = FriendManager.get();
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "add": {
                if (args.length < 2) { CommandManager.error("Укажи ник"); return; }
                if (fm.add(args[1])) CommandManager.success("Друг добавлен: " + args[1]);
                else CommandManager.error(args[1] + " уже в друзьях");
                break;
            }
            case "remove":
            case "del":
            case "delete": {
                if (args.length < 2) { CommandManager.error("Укажи ник"); return; }
                if (fm.remove(args[1])) CommandManager.success("Друг удалён: " + args[1]);
                else CommandManager.error(args[1] + " не найден в друзьях");
                break;
            }
            case "clear": {
                fm.clear();
                CommandManager.success("Список друзей очищен");
                break;
            }
            case "list": {
                List<String> friends = fm.getFriends();
                if (friends.isEmpty()) {
                    CommandManager.error("Список друзей пуст");
                } else {
                    CommandManager.success("Друзья (" + friends.size() + "): " + String.join(", ", friends));
                }
                break;
            }
            default:
                CommandManager.error("Используй: " + getUsage());
        }
    }
}
