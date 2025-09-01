package wargames.testutils.testmodels;

import java.util.ArrayList;
import java.util.List;

import wargames.events.subscribers.Subscriber;
import wargames.events.Event;

public class TestSubscriber implements Subscriber {

    final public List<Event> events = new ArrayList<>();

    @Override public void update(Event e) { events.add(e); }    

}
