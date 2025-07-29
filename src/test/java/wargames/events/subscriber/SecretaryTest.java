package wargames.events.subscriber;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;

import wargames.commands.*;
import wargames.models.*;
import wargames.factories.*;
import wargames.events.*;
import wargames.events.publisher.*;
import wargames.events.subscribers.*;

public class SecretaryTest {

    private class TestEvent implements Event { }
    
    private class TestCommand extends Command {

            public TestCommand(General g, EventDispatcher d) { super(g, d); }

            @Override
            public void execute() { }
    }

    private final EventDispatcher dispatcher = EventDispatcher.getInstance();
    private final ByteArrayOutputStream out  = new ByteArrayOutputStream();

    private final String  generalName = "Napoleon Bonaparte";
    private final int     generalGold = 128;

    private General general;

    @BeforeEach
    void setUp() {
        general = new General(generalName, generalGold);
    }
    
    @Nested
    class testUpdate {

        private final String beforeCommandMessage   = "Secretary: %s is about to execute %s";
        private final String afterCommandMessage    = "Secretary: %s executed %s";
        private final String recruitSoldiersMessage = ": %d soldiers of rank %s";
        private final String drillSoldiersMessage   = ": drilled %d soldiers for %d gold";
        private final String defaultEventMessage    = "%s event occured";

        @BeforeEach
        void setUp() {
            dispatcher.removeAllSubscribers();
            dispatcher.addSubscriber(new Secretary());

            System.setOut(new PrintStream(out));
        }

        @AfterEach
        void tearDown() {
            dispatcher.removeAllSubscribers();

            System.setOut(System.out);
        }

        @Test
        @DisplayName("TestEvent")
        void testEvent() {
            String eventName       = TestEvent.class.getSimpleName();
            String expectedMessage = String.format(defaultEventMessage, eventName);

            dispatcher.updateSubscribers(new TestEvent());

            String log = out.toString();
            assertTrue(log.contains(expectedMessage));
        }
       
        @Nested
        class BeforeAndAfterCommandEvent {

            private final SoldierFactory soldierFactory = new SoldierFactory();
            private final CommandFactory commandFactory = new CommandFactory(dispatcher, soldierFactory);

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

                RecruitSoldiersCommand rsCommand = commandFactory.createRecruitSoldiers(
                    general, quantity, rank
                );

                String commandName       = rsCommand.getClass().getSimpleName();
                String commandSubMessage = String.format(recruitSoldiersMessage, quantity, rank);

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
            
            @Test
            void testDrillSoldiersCommand() {
                Army          generalArmy     = general.getArmy();
                List<Soldier> soldiersToDrill = generalArmy.getSoldiers();

                Rank rank          = Rank.PRIVATE;
                int  amountToDrill = 10;
                int  drillCost     = amountToDrill * rank.getValue();

                for (int i = 0; i < amountToDrill; i++) {
                    Soldier s = soldierFactory.createSoldier(rank);
                    generalArmy.add(s);
                }

                DrillSoldiersCommand drillCmd = commandFactory.createDrillSoldiers(
                    general, soldiersToDrill
                );

                String commandName       = drillCmd.getClass().getSimpleName();
                String commandSubMessage = String.format(drillSoldiersMessage, amountToDrill, drillCost);

                String beforeDrillMessage = String.format(
                    beforeCommandMessage, generalName, commandName
                ) + commandSubMessage;
                String afterDrillMessage  = String.format(
                    afterCommandMessage, generalName, commandName
                ) + commandSubMessage;

                dispatcher.updateSubscribers(new BeforeCommandEvent(drillCmd));
                dispatcher.updateSubscribers(new AfterCommandEvent(drillCmd));

                String log = out.toString();
                assertTrue(log.contains(beforeDrillMessage));
                assertTrue(log.contains(afterDrillMessage));
            }           
        }
    }
}
