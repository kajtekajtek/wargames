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

    private static final EventDispatcher dispatcher = EventDispatcher.getInstance();
    private static final ByteArrayOutputStream out  = new ByteArrayOutputStream();

    @BeforeAll
    static void setUp() {
        dispatcher.removeAllSubscribers();
        dispatcher.addSubscriber(new Secretary());

        System.setOut(new PrintStream(out));
    }

    @AfterAll
    static void tearDown() {
        dispatcher.removeAllSubscribers();

        System.setOut(System.out);
    }
    
    @Nested
    @DisplayName("Should log event message to system out on update")
    class UpdateTest {

        /* toy event implementation */
        private class TestEvent implements Event { }

        /* all log messages */
        private final String expectedMessagePrefix = "Secretary: ";
        private final String expectedMessageSuffix = "\n";

        /* event specific */
        private final String eventSubjectTemplate  = "%s event occured";
        private final String BfrCmdSubjectTemplate = "%s is about to execute %s";
        private final String AfrCmdSubjectTemplate = "%s executed %s";

        /* command specific */
        private final String recruitmentDetailsTemplate = ": %d soldiers of rank %s";
        private final String drillDetailsTemplate       = ": %d soldiers for %d gold";
        private final String preAttackDetailsTemplate   = ": attacking %s";
        private final String wonAttackDetailsTemplate   = ": won with %s";
        private final String lostAttackDetailsTemplate  = ": lost with %s";
        private final String drewAttackDetailsTemplate  = ": drew with %s";

        @Test
        @DisplayName("Should log event name using default event template")
        void testEvent() {
            TestEvent testEvent = new TestEvent();

            dispatcher.updateSubscribers(new TestEvent());

            assertExpectedMessage(testEvent);
        }
       
        @Nested
        @DisplayName("Should log command execution messages on CommandEvent update")
        class UpdateCommandEventTest {

            /* toy command implementation */
            private class TestCommand extends Command {

                public TestCommand(General g, EventDispatcher d) { super(g, d); }

                @Override
                public void execute() { }
            }

            private final SoldierFactory soldierFactory = new SoldierFactory();
            private final CommandFactory commandFactory = new CommandFactory(dispatcher, soldierFactory);

            private final String generalName = "Napoleon Bonaparte";
            private final int    generalGold = 128;

            private General general;

            @BeforeEach
            void setUp() {
                general = new General(generalName, generalGold);
            }

            @Test
            @DisplayName("Should log only command name before and after TestCommand")
            void testCommand() {
                TestCommand        testCommand    = new TestCommand(general, dispatcher);
                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(testCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(testCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                assertExpectedMessage(beforeCmdEvent);
                assertExpectedMessage(afterCmdEvent);
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

                assertExpectedMessage(beforeCmdEvent);
                assertExpectedMessage(afterCmdEvent);
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

                assertExpectedMessage(beforeCmdEvent);
                assertExpectedMessage(afterCmdEvent);
            }

            @ParameterizedTest(name = "Logging AttackCommand details with attacked army size = {0}")
            @ValueSource(ints = {0, 1, 2})
            @DisplayName("Should log attacking and attacked general's name and battle outcome")
            void testAttackCommand(int attackedArmySize) {
                Army attackingArmy = general.getArmy();
                attackingArmy.add(soldierFactory.createPrivate());

                General attackedGeneral = new General("Duke of Wellington", generalGold);
                Army    attackedArmy = attackedGeneral.getArmy();
                for (int i = 0; i < attackedArmySize; i++) {
                    attackedArmy.add(soldierFactory.createPrivate());
                }

                AttackCommand aCommand = commandFactory.createAttack(
                    general, attackedGeneral
                );

                BeforeCommandEvent beforeCmdEvent = new BeforeCommandEvent(aCommand);
                AfterCommandEvent  afterCmdEvent  = new AfterCommandEvent(aCommand);

                dispatcher.updateSubscribers(beforeCmdEvent);
                dispatcher.updateSubscribers(afterCmdEvent);

                assertExpectedMessage(beforeCmdEvent);
                assertExpectedMessage(afterCmdEvent);
            }
        }

        private void assertExpectedMessage(Event e) {
            String expectedMessage = prepareExpectedMessage(e);

            String log = out.toString();

            assertTrue(log.contains(expectedMessage)); 
        }

        private String prepareExpectedMessage(Event event) {
            String expectedMessage = "";

            expectedMessage += expectedMessagePrefix;

            if (event instanceof CommandEvent) {
                expectedMessage += prepareExpectedMessageSubject((CommandEvent) event);

            } else {
                expectedMessage += prepareExpectedMessageSubject(event);

            }

            expectedMessage += expectedMessageSuffix;

            return expectedMessage;
        }

        private String prepareExpectedMessageSubject(CommandEvent ce) {
            String messageSubject;

            String subjectTemplate = ce instanceof BeforeCommandEvent ?
                                        BfrCmdSubjectTemplate : AfrCmdSubjectTemplate;

            Command command     = ce.getCommand();
            String  commandName = ce.getCommandName();
            String  generalName = ce.getGeneralName();

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
                recruitmentDetailsTemplate, recruitedQuantity, recruitedRank
            );

            return commandDetails;
        }

        private String prepareCommandDetails(DrillSoldiersCommand cmd) {
            String commandDetails;
            int drilledQuantity = cmd.getQuantity();
            int drillCost       = cmd.getCost();

            commandDetails = String.format(
                drillDetailsTemplate, drilledQuantity, drillCost
            );

            return commandDetails;
        }

        private String prepareCommandDetails(AttackCommand cmd) {
            String  commandDetails, detailsTemplate;
            String  attackingName = cmd.getAttacking().getName();
            String  attackedName  = cmd.getAttacked().getName();
            Boolean attackIsOver  = cmd.isAttackOver();

            if (!attackIsOver) {
                detailsTemplate = preAttackDetailsTemplate;

            } else {
                General winner = cmd.getWinner();

                if (winner == null) {
                    detailsTemplate = drewAttackDetailsTemplate;

                } else {
                    detailsTemplate = winner.getName() == attackingName 
                                      ? wonAttackDetailsTemplate 
                                      : lostAttackDetailsTemplate;
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
            messageSubject = String.format(eventSubjectTemplate, eventName);

            return messageSubject;
        }

    }
}
