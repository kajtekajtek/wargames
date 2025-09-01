package wargames.testutils.testmodels;

import wargames.commands.Command;
import wargames.events.publisher.EventDispatcher;
import wargames.models.General;

public class TestCommand extends Command {

    public TestCommand(General g, EventDispatcher d) { super(g, d); }

    @Override public void execute() { } 

}
