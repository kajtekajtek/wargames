package wargames.events.subscribers;

import wargames.events.Event;

public interface Subscriber {
    void update(Event e); 
}