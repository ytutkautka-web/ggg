package fun.photon.command;

import fun.photon.command.impl.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    public static final String PREFIX = ".";

    private final List<Command> commands = new ArrayList<>();

    public void init() {
        register(new HelpCommand());
        register(new ConfigCommand());
        register(new BindCommand());
        register(new ToggleCommand());
        register(new StaffListCommand());
        register(new FriendCommand());
    }

    public void register(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public boolean isCommand(String message) {
        return message.startsWith(PREFIX);
    }

    public Command find(String label) {
        for (Command c : commands) if (c.matches(label)) return c;
        return null;
    }

    public boolean dispatch(String message) {
        if (!isCommand(message)) return false;
        String body = message.substring(PREFIX.length()).trim();
        if (body.isEmpty()) {
            error("Пустая команда. " + PREFIX + "help");
            return true;
        }
        String[] parts = body.split("\\s+");
        String label = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command command = find(label);
        if (command == null) {
            error("Неизвестная команда: " + label + ". " + PREFIX + "help");
            return true;
        }
        try {
            command.execute(args);
        } catch (Exception e) {
            error("Ошибка: " + e.getMessage());
        }
        return true;
    }

    public static void send(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(Component.literal("§7[§bProton§7] §r" + text));
        }
    }

    public static void error(String text) {
        send("§c" + text);
    }

    public static void success(String text) {
        send("§a" + text);
    }
}
