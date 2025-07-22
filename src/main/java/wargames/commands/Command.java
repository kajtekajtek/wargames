package wargames.commands;

import wargames.models.*;
import wargames.events.*;
import wargames.events.publisher.EventDispatcher;

public abstract class Command {

    protected final General         general;
    protected final EventDispatcher dispatcher;

    public Command(General general, EventDispatcher dispatcher) {
        this.general    = general;
        this.dispatcher = dispatcher;
    }

    public final void executeAndUpdate() throws Exception {
        dispatcher.updateSubscribers(new BeforeCommandEvent(this));
        execute();
        dispatcher.updateSubscribers(new AfterCommandEvent(this));
    }
    
    public abstract void execute() throws Exception;

    public final General getGeneral() { return this.general; }
}