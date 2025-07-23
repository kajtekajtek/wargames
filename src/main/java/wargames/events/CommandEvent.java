package wargames.events;

import wargames.commands.Command;
import wargames.models.General;

public class CommandEvent implements Event {
    private final Command command;

    public CommandEvent(Command command) { this.command = command; }

    public String  getGeneralName() { return command.getGeneral().getName(); }
    public General getGeneral()     { return command.getGeneral(); }

    public String  getCommandName() { return command.getClass().getSimpleName(); }
    public Command getCommand()     { return command; }
}
