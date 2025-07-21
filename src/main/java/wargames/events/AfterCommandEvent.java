package wargames.events;

import wargames.models.General;
import wargames.commands.Command;

public class AfterCommandEvent implements Event {
    private final Command command;

    public AfterCommandEvent(Command command) {
        this.command = command;
    }

    public General getGeneral() { return command.general; }
    public Command getCommand() { return command; }
}
