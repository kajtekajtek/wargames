package wargames.events.subscriber;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.util.List;

import wargames.commands.*;
import wargames.models.*;
import wargames.factories.*;
import wargames.events.*;
import wargames.events.publisher.*;
import wargames.events.subscribers.*;

public class SecretaryTest {

    /* toy event implementation */
    private class TestEvent implements Event { }
    
    /* toy command implementation */
    private class TestCommand extends Command {

            public TestCommand(General g, EventDispatcher d) { super(g, d); }

            @Override
            public void execute() { }
    }

    private final EventDispatcher dispatcher = EventDispatcher.getInstance();
    private final ByteArrayOutputStream out  = new ByteArrayOutputStream();

    private final String generalName = "Napoleon Bonaparte";
    private final int    generalGold = 128;

    private General general;

    @BeforeEach
    void setUp() {
        general = new General(generalName, generalGold);
    }
    
    @Nested
    @DisplayName("Should log event information message to system out")
    class testUpdate {

        private final String messagePrefix = "Secretary: ";
        private final String messageSuffix = "\n";

        private final String defaultEventSubject  = "%s event occured";
        private final String beforeCommandSubject = "%s is about to execute %s";
        private final String afterCommandSubject  = "%s executed %s";

        private final String recruitSoldiersDetails = ": %d soldiers of rank %s";
        private final String drillSoldiersDetails   = ": %d soldiers for %d gold";
        private final String beforeAttackDetails    = ": attacking %s";
        private final String afterAttackDetailsWon  = ": won with %s";
        private final String afterAttackDetailsLost = ": lost with %s";
        private final String afterAttackDetailsDrew = ": drew with %s";

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
        @DisplayName("Should log event name using default event template")
        void testEvent() {
            TestEvent testEvent = new TestEvent();

            dispatcher.updateSubscribers(new TestEvent());

            String expectedMessage = prepareExpectedMessage(testEvent);
            String log = out.toString();
            assertTrue(log.contains(expectedMessage));
        }
       
        @Nested
        @DisplayName("Should log command execution messages on CommandEvent update")
        class testCommandEvent {

            private final SoldierFactory soldierFactory = new SoldierFactory();
            private final CommandFactory commandFactory = new CommandFactory(dispatcher, soldierFactory);

            @Test
            @DisplayName("Should log only command name before and after TestCommand")
            void testCommand() {
                TestCommand        testCommand    = new TestCommand(general, dispatcher);
                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(testCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(testCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                String expectedBeforeMsg = prepareExpectedMessage(beforeCmdEvent);
                String expectedAfterMsg  = prepareExpectedMessage(afterCmdEvent);
                String log = out.toString();
                assertTrue(log.contains(expectedBeforeMsg));
                assertTrue(log.contains(expectedAfterMsg));
            }
            
            @Test
            @DisplayName("Should log recruited soldiers quantity and rank before and affter RecruitSoldiersCommand")
            void testRecruitSoldiersCommand() {
                int  quantity = 1;
                Rank rank     = Rank.PRIVATE;            
                RecruitSoldiersCommand rsCommand = commandFactory.createRecruitSoldiers(
                    general, quantity, rank
                );

                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(rsCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(rsCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                String expectedBeforeMessage = prepareExpectedMessage(beforeCmdEvent);
                String expectedAfterMessage  = prepareExpectedMessage(afterCmdEvent);
                String log = out.toString();
                assertTrue(log.contains(expectedBeforeMessage));
                assertTrue(log.contains(expectedAfterMessage));
            }
            
            @Test
            @DisplayName("Should log drilled soldiers amount and drill's cost before and after DrillSoldiersCommand")
            void testDrillSoldiersCommand() {
                Army          generalArmy     = general.getArmy();
                List<Soldier> soldiersToDrill = generalArmy.getSoldiers();

                Rank rank          = Rank.PRIVATE;
                int  amountToDrill = 10;

                for (int i = 0; i < amountToDrill; i++) {
                    Soldier s = soldierFactory.createSoldier(rank);
                    generalArmy.add(s);
                }

                DrillSoldiersCommand dCommand = commandFactory.createDrillSoldiers(
                    general, soldiersToDrill
                );

                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(dCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(dCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                String expectedBeforeMessage = prepareExpectedMessage(beforeCmdEvent);
                String expectedAfterMessage  = prepareExpectedMessage(afterCmdEvent);
                String log = out.toString();
                assertTrue(log.contains(expectedBeforeMessage));
                assertTrue(log.contains(expectedAfterMessage));
            }

            @ParameterizedTest(name = "Logging AttackCommand details with attacked army size = {0}")
            @ValueSource(ints = {0, 1, 2})
            @DisplayName("Should log attacking and attacked general's name and battle outcome")
            void testAttackCommand(int attackedArmySize) {
                Army attackingArmy = general.getArmy();
                attackingArmy.add(soldierFactory.createPrivate());

                General attackedGeneral = new General("Duke off Wellington", generalGold);
                Army    attackedArmy = attackedGeneral.getArmy();
                for (int i = 0; i < attackedArmySize; i++) {
                    attackedArmy.add(soldierFactory.createPrivate());
                }

                AttackCommand aCommand = commandFactory.createAttack(general, attackedGeneral);

                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(aCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(aCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                String expectedBeforeMessage = prepareExpectedMessage(beforeCmdEvent);
                String expectedAfterMessage  = prepareExpectedMessage(afterCmdEvent);
                String log = out.toString();
                assertTrue(log.contains(expectedBeforeMessage));
                assertTrue(log.contains(expectedAfterMessage));
            }
        }

        private String prepareExpectedMessage(Event event) {
            String expectedMessage = "";

            expectedMessage += messagePrefix;

            if (event instanceof CommandEvent) {
                expectedMessage += prepareExpectedMessageSubject((CommandEvent) event);

            } else {
                expectedMessage += prepareExpectedMessageSubject(event);

            }

            expectedMessage += messageSuffix;

            return expectedMessage;
        }

        private String prepareExpectedMessageSubject(CommandEvent ce) {
            String messageSubject;

            String subjectTemplate = ce instanceof BeforeCommandEvent ?
                                        beforeCommandSubject : afterCommandSubject;

            Command command     = ce.getCommand();
            String  commandName = ce.getCommandName();

            messageSubject = String.format(subjectTemplate, generalName, commandName);

            String messageDetails;
            if (command instanceof RecruitSoldiersCommand) {
                messageDetails = prepareCommandDetails((RecruitSoldiersCommand) command);
                
            } else if (command instanceof DrillSoldiersCommand) {
                messageDetails = prepareCommandDetails((DrillSoldiersCommand) command);

            } else if (command instanceof AttackCommand) {
                messageDetails = prepareCommandDetails((AttackCommand) command);

            } else {
                messageDetails = "";

            }
            messageSubject += messageDetails;

            return messageSubject;
        }

        private String prepareCommandDetails(RecruitSoldiersCommand cmd) {
            String commandDetails;
            int  recruitedQuantity = cmd.getQuantity();
            Rank recruitedRank     = cmd.getRank();

            commandDetails = String.format(
                recruitSoldiersDetails, recruitedQuantity, recruitedRank
            );

            return commandDetails;
        }

        private String prepareCommandDetails(DrillSoldiersCommand cmd) {
            String commandDetails;
            int drilledQuantity = cmd.getQuantity();
            int drillCost       = cmd.getCost();

            commandDetails = String.format(
                drillSoldiersDetails, drilledQuantity, drillCost
            );

            return commandDetails;
        }

        private String prepareCommandDetails(AttackCommand cmd) {
            String  commandDetails, detailsTemplate;
            String  attackingName = cmd.getAttacking().getName();
            String  attackedName  = cmd.getAttacked().getName();
            Boolean attackIsOver  = cmd.isAttackOver();

            if (!attackIsOver) {
                detailsTemplate = beforeAttackDetails;

            } else {
                General winner = cmd.getWinner();

                if (winner == null) {
                    detailsTemplate = afterAttackDetailsDrew;

                } else {
                    detailsTemplate = winner.getName() == attackingName 
                                      ? afterAttackDetailsWon 
                                      : afterAttackDetailsLost;
                }
            }

            commandDetails = String.format(
                detailsTemplate, attackedName
            );

            return commandDetails;
        }

        private String prepareExpectedMessageSubject(Event e) {
            String eventName, messageSubject;

            eventName = e.getClass().getSimpleName();
            messageSubject = String.format(defaultEventSubject, eventName);

            return messageSubject;
        }

    }
}
