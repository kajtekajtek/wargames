package wargames.events.subscriber;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.*;

import wargames.commands.*;
import wargames.models.General;
import wargames.models.Rank;
import wargames.factories.*;
import wargames.events.*;
import wargames.events.publisher.*;
import wargames.events.subscribers.*;

public class SecretaryTest {

    private final String  generalName = "Napoleon Bonaparte";
    private final int     generalGold = 128;
    private final General general     = new General(generalName, generalGold);

    private final EventDispatcher dispatcher = EventDispatcher.getInstance();
    private final ByteArrayOutputStream out  = new ByteArrayOutputStream();

    
    private class TestCommand extends Command {

            public TestCommand(General g, EventDispatcher d) { super(g, d); }

            @Override
            public void execute() { }
    }
    
    @Nested
    class testUpdate {

        @BeforeEach
        void setUp() {
            dispatcher.removeAllSubscribers();
            dispatcher.addSubscriber(new Secretary());

            System.setOut(new PrintStream(out));
        }

        @AfterAll
        void tearDown() {
            dispatcher.removeAllSubscribers();

            System.setOut(System.out)
        }
       
        @Nested
        class BeforeAndAfterCommandEvent {

            private final String beforeCommandMessage = "Secretary: %s is about to execute %s";
            private final String afterCommandMessage  = "Secretary: %s executed %s";

            @Test
            void testCommand() {
                TestCommand testCommand = new TestCommand(general, dispatcher);
                String      commandName = testCommand.getClass().getSimpleName();

                dispatcher.updateSubscribers(new BeforeCommandEvent(testCommand));
                dispatcher.updateSubscribers(new AfterCommandEvent(testCommand));

                String log = out.toString();
                assertTrue(log.contains(String.format(
                    beforeCommandMessage, generalName, commandName
                )));
                assertTrue(log.contains(String.format(
                    afterCommandMessage, generalName, commandName
                )));
            }
            
            @Test
            void testRecruitSoldiersCommand() {
                int  quantity = 1;
                Rank rank     = Rank.PRIVATE;            

                CommandFactory cmdFactory = new CommandFactory(
                    dispatcher, new SoldierFactory()
                );
                RecruitSoldiersCommand rsCommand  = cmdFactory.createRecruitSoldiers(
                    general, quantity, rank
                );

                String commandName       = rsCommand.getClass().getSimpleName();
                String commandSubMessage = String.format(": %d soldiers of rank %s", quantity, rank);

                String beforeRecruitmentMessage = String.format(
                    beforeCommandMessage, generalName, commandName
                ) + commandSubMessage;
                String afterRecruitmentMessage  = String.format(
                    afterCommandMessage, generalName, commandName
                ) + commandSubMessage;

                dispatcher.updateSubscribers(new BeforeCommandEvent(rsCommand));
                dispatcher.updateSubscribers(new AfterCommandEvent(rsCommand));

                String log = out.toString();
                assertTrue(log.contains(beforeRecruitmentMessage));
                assertTrue(log.contains(afterRecruitmentMessage));
            }           
        }
    }
}
