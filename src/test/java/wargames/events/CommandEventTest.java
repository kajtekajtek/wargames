package wargames.events;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import wargames.events.*;
import wargames.models.General;

public class CommandEventTest {

    
    @Test
    void beforeAndAfterCommandEvent_shouldCarryGeneralAndName() {
        

        
    }

    @Nested
    class BeforeAndAfterCommandEvent {

        private final String commandName = "Test Command";
        private final String generalName = "Helm Hammerhand";
        private final int    generalGold = 64;

        private General general;

        @BeforeEach
        void setUp() {
            general = new General(generalName, generalGold);
        }

        @Test
        @DisplayName("BeforeCommandEvent should carry General and command name parameters")
        void testBeforeCommandGeneralAndName() {
            BeforeCommandEvent bef = new BeforeCommandEvent(general, commandName);

            assertSame(general, bef.getGeneral());
            assertEquals(commandName, bef.getCommandName());
        }
        
        @Test
        @DisplayName("AfterCommandEvent should carry General and command name parameters")
        void testAfterCommandGeneralAndName() {
            AfterCommandEvent aft = new AfterCommandEvent(general, commandName);

            assertSame(general, aft.getGeneral());
            assertEquals(commandName, aft.getCommandName());
        }
    }
        
}
