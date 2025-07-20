package wargames.events;

import wargames.models.General;

public class AfterCommandEvent implements Event {
    private final String  commandName;
    private final General general;

    public AfterCommandEvent(General general, String name) {
        this.general = general;
        this.commandName = name;
    }

    public General getGeneral()     { return general; }
    public String  getCommandName() { return commandName; }
}
