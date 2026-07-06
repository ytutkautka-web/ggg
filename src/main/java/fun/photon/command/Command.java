package fun.photon.command;

public abstract class Command {

    private final String name;
    private final String[] aliases;
    private final String usage;
    private final String description;

    protected Command(String name, String usage, String description, String... aliases) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public boolean matches(String label) {
        if (label.equalsIgnoreCase(name)) return true;
        for (String a : aliases) if (label.equalsIgnoreCase(a)) return true;
        return false;
    }

    public abstract void execute(String[] args);
}
