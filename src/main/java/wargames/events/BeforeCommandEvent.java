package wargames.events;

import wargames.commands.Command;

public class BeforeCommandEvent extends CommandEvent {

    public BeforeCommandEvent(Command command) { super(command); }

}