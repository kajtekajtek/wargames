package wargames.events;

import wargames.models.General;
import wargames.commands.Command;

public class BeforeCommandEvent implements Event {
    private final Command command;

    public BeforeCommandEvent(Command command) {
        this.command = command;
    }

    public General getGeneral() { return command.general; }
    public Command getCommand() { return command; }
}
