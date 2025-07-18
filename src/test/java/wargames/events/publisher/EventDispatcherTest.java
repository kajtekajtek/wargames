package wargames.events.publisher;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import wargames.events.Event;
import wargames.events.subscribers.*;

public class EventDispatcherTest {
    
    static class DummyEvent implements Event { }
    
    static class DummySubscriber implements Subscriber {
        boolean called = false;

        @Override
        public void update(Event e) {
            called = true;
        }
    }
    
    private final EventDispatcher dispatcher  = EventDispatcher.getInstance();
    private final DummySubscriber subscriber1 = new DummySubscriber();
    private final DummySubscriber subscriber2 = new DummySubscriber();
    
    @Test
    @DisplayName("getInstance() should return the same instance of the EventDispatcher")
    void getInstanceReturnsSameObject() {
        EventDispatcher newDispatcher = EventDispatcher.getInstance();
        
        assertSame(dispatcher, newDispatcher);
    }
    
    @Test
    @DisplayName("Only subscribers present on the subscribers list should get notified by notifySubscribers()")
    void notifySubscribersNotifiesOnlyAdded() {
        dispatcher.addSubscriber(subscriber1);
        dispatcher.updateSubscribers(new DummyEvent());
        assertTrue(subscriber1.called);
        assertFalse(subscriber2.called);

        subscriber1.called = false;
        dispatcher.removeSubscriber(subscriber1);
        dispatcher.updateSubscribers(new DummyEvent());
        assertFalse(subscriber1.called);    
    }
}