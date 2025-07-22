package wargames.events;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import wargames.models.General;
import wargames.commands.Command;
import wargames.events.publisher.EventDispatcher;

public class CommandEventTest {

    @Nested
    class BeforeAndAfterCommandEvent {

        private class TestCommand extends Command {

            public TestCommand(General g, EventDispatcher d) { super(g, d); }

            @Override
            public void execute() { }
        }

        private final String  generalName = "Helm Hammerhand";
        private final int     generalGold = 64;
        private final General general     = new General(generalName, generalGold);

        private final EventDispatcher dispatcher = EventDispatcher.getInstance();

        private final Command command     = new TestCommand(general, dispatcher);
        private final String  commandName = command.getClass().getSimpleName();

        @Test
        @DisplayName("BeforeCommandEvent should carry General and Command parameters")
        void testBeforeCommandGeneralAndName() {
            BeforeCommandEvent bef = new BeforeCommandEvent(command);

            assertSame(general, bef.getGeneral());
            assertEquals(commandName, bef.getCommandName());
            assertEquals(command, bef.getCommand());
        }
        
        @Test
        @DisplayName("AfterCommandEvent should carry General and Command parameters")
        void testAfterCommandGeneralAndName() {
            AfterCommandEvent aft = new AfterCommandEvent(command);

            assertSame(general, aft.getGeneral());
            assertEquals(commandName, aft.getCommandName());
            assertEquals(command, aft.getCommand());
        }
    }
        
}
