package wargames.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.util.*;

import wargames.events.subscribers.Subscriber;
import wargames.events.publisher.EventDispatcher;
import wargames.events.*;
import wargames.commands.Command;
import wargames.models.General;

public class CommandSubscriberTest {

    private final String  generalName = "Julius Caesar";
    private final int     generalGold = 0;
    private final General general     = new General(generalName, generalGold);

    private final EventDispatcher dispatcher = EventDispatcher.getInstance();

    static class TestCommand extends Command {
        public TestCommand(General g, EventDispatcher d) {
            super(g, d);
        }

        @Override public void execute() { }
    }

    static class TestSubscriber implements Subscriber {
        final List<Event> events = new ArrayList<>();

        @Override public void update(Event e) { events.add(e); }
    }

    private final TestSubscriber testSubscriber = new TestSubscriber();

    @BeforeEach
    void setUp() {
        dispatcher.removeAllSubscribers();
        dispatcher.addSubscriber(testSubscriber);
    }
    
    @Test
    @DisplayName("executeAndUpdate() should notify all of the dispatcher's subscribers before and after command execution")
    void testTestCommandExecuteAndUpdate() {
        TestCommand testCommand = new TestCommand(general, dispatcher);

        assertDoesNotThrow(() -> testCommand.executeAndUpdate());

        List<Event> eventsDispatched = testSubscriber.events;
        assertEquals(2, eventsDispatched.size());
        assertTrue(eventsDispatched.get(0) instanceof BeforeCommandEvent);
        assertTrue(eventsDispatched.get(1) instanceof AfterCommandEvent);

        BeforeCommandEvent bef = (BeforeCommandEvent) eventsDispatched.get(0);
        AfterCommandEvent  aft = (AfterCommandEvent)  eventsDispatched.get(1);
        assertSame(testCommand, bef.getCommand());
        assertSame(testCommand, aft.getCommand());
        assertSame(general, bef.getGeneral());
        assertSame(general, aft.getGeneral());
    }
}
