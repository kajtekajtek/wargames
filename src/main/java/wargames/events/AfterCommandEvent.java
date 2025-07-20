package wargames.events;

import wargames.models.General;
import wargames.commands.Command;

public class AfterCommandEvent implements Event {
    private final Command command;
    private final General general;

    public AfterCommandEvent(General general, Command name) {
        this.general = general;
        this.command = name;
    }

    public General getGeneral()     { return general; }
    public Command getCommand() { return command; }
}
