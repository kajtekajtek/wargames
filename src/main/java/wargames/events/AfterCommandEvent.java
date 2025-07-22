package wargames.events;

import wargames.commands.Command;

public class AfterCommandEvent extends CommandEvent {

    public AfterCommandEvent(Command command) { super(command); }

}