package wargames.events.publisher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import wargames.events.*;
import wargames.events.subscribers.*;

// Thread-safe implementation of Publisher with lazy loading
// https://refactoring.guru/design-patterns/singleton/java/example#example-2
public class EventDispatcher implements Publisher { 

    private static volatile EventDispatcher instance;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public static EventDispatcher getInstance() {
        /* 
            The reason for using the local reference result is that
            in cases where instance is already initialized,
            the volatile field is only accessed once, which can improve the 
            method's overall performance by as much as 40%

            https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        */
        EventDispatcher result = instance;
        if (result != null) {
            return result;
        }
        synchronized(EventDispatcher.class) {
            if (instance == null) {
                instance = new EventDispatcher();
            }
            return instance;
        }
    }
    
    @Override
    public void addSubscriber(Subscriber s) {
        this.subscribers.add(s);
        
    }
    
    public void removeAllSubscribers() {
        for (Subscriber s : subscribers) {
            removeSubscriber(s);
        }
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        this.subscribers.remove(s);
    }

    @Override
    public void updateSubscribers(Event e) {
        for (Subscriber s : this.subscribers) {
            s.update(e);
        }
    }
}