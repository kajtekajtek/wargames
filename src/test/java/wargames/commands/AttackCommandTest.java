package wargames.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import wargames.factories.*;
import wargames.models.*;
import wargames.events.publisher.EventDispatcher;

public class AttackCommandTest {

    private static final String ATTACKER_NAME = "Agamemnon";
    private static final String ATTACKED_NAME = "Priam";
    private static final int    STARTING_GOLD = 100;

    private static final int    TEST_ARMY_SIZE     = 10;
    private static final Rank   TEST_SOLDIERS_RANK = Rank.PRIVATE;

    private static final double GOLD_LOOT_PERCENTAGE = AttackCommand.GOLD_LOOT_PERCENTAGE;
    private static final double WINNER_GOLD_MULTIPLIER   = 1 + GOLD_LOOT_PERCENTAGE;
    private static final double LOSER_GOLD_MULTIPLIER    = 1 - GOLD_LOOT_PERCENTAGE;

    private final SoldierFactory  soldierFactory = new SoldierFactory();
    private final EventDispatcher dispatcher     = EventDispatcher.getInstance();
    private final CommandFactory  commandFactory = new CommandFactory(dispatcher, soldierFactory);
    
    private General attacking;
    private General attacked;

    @BeforeEach
    void setUp() {
        attacking = new General(ATTACKER_NAME, STARTING_GOLD);
        attacked  = new General(ATTACKED_NAME, STARTING_GOLD);
        
        recruitTestArmy(attacking);
        recruitTestArmy(attacked);
    }
    
    @ParameterizedTest(name = "{0} wins the battle")
    @CsvSource({
        "attacker, attacked",
        "attacked, attacker"
    })
    @DisplayName("Loser gives 10% of gold to the winner; losing army -> exp--, winning army -> exp++")
    void testGeneralWins(String winnerRole, String loserRole) {
        General winner = "attacker".equals(winnerRole) ? attacking : attacked;
        General loser  = "attacker".equals(loserRole)  ? attacking : attacked;

        Army victoriousArmy = winner.getArmy();
        Army defeatedArmy   = loser.getArmy();
        victoriousArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));

        AttackCommand cmd = commandFactory.createAttack(winner, loser);
        assertDoesNotThrow(cmd::execute);

        int expectedVictoriousStrength = victoriousArmy.getSize()
                                         * TEST_SOLDIERS_RANK.getValue()
                                         * 2;
        int expectedDefeatedStrength = 0;

        assertEquals(expectedVictoriousStrength,
                     victoriousArmy.getTotalStrength());
        assertEquals(expectedDefeatedStrength,
                     defeatedArmy.getTotalStrength());
        assertEquals(STARTING_GOLD * WINNER_GOLD_MULTIPLIER,
                     winner.getGold());
        assertEquals(STARTING_GOLD * LOSER_GOLD_MULTIPLIER,
                     loser.getGold());
    }
    
    @Test
    @DisplayName("In the case of a draw, each general must shoot one randomly selected soldier")
    void testDraw() {
        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        assertEquals(TEST_ARMY_SIZE - 1, attacking.getArmy().getSize());
        assertEquals(TEST_ARMY_SIZE - 1, attacked.getArmy().getSize());
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
    }
    
    @Test
    @DisplayName("IllegalArgumentException should be thrown if the attacking general has an empty army")
    void testAttackerWithEmptyArmy() {
        attacking = new General("General without an army", STARTING_GOLD);

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertThrows(IllegalArgumentException.class, cmd::execute);
    }
    
    @Test
    @DisplayName("IllegalArgumentException should be thrown if the attacked general has an empty army")
    void testAttackedWithEmptyArmy() {
        attacked = new General("General without an army", STARTING_GOLD);

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertThrows(IllegalArgumentException.class, cmd::execute);

    }

    @ParameterizedTest(name = "{0} with empty army throws IllegalArgumentException")
    @ValueSource(strings = {"attacker", "attacked", "both"})
    @DisplayName("IllegalArgumentException when {0} has an empty army")
    void testEmptyArmyThrows(String roleWithEmpty) {
        General withoutArmy = new General("General without an army", STARTING_GOLD);
        if ("attacker".equals(roleWithEmpty)) {
            attacking = withoutArmy;
        } else if ("attacked".equals(roleWithEmpty)) {
            attacked = withoutArmy;
        } else {
            attacking = new General("Attacking without an army", STARTING_GOLD);
            attacked = new General("Attacked without an army", STARTING_GOLD);
        }

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertThrows(IllegalArgumentException.class, cmd::execute);
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
    }
    
    @Test
    @DisplayName("IllegalArgumentException thrown on the general being the attacking and attacked at the same time")
    void testGeneralAttacksItself() {
        AttackCommand cmd = commandFactory.createAttack(attacking, attacking);     
        assertThrows(IllegalArgumentException.class, cmd::execute);
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(TEST_ARMY_SIZE, attacking.getArmy().getSize());
    }
    
    @Test
    @DisplayName("If the attacking general has no gold - then what?")
    void testAttackerHasNoGold() {
        
    }
    
    @Test
    @DisplayName("If the attacked general has no gold - then what?")
    void testAttackedHasNoGold() {
        
    }
    
    @Test
    @DisplayName("If loser's gold < 10 - then what?")
    void testLoserHasLowGold() {

    }

    void recruitTestArmy(General general) {
        Army army = general.getArmy();
        for (int i = 0; i < TEST_ARMY_SIZE; i++) {
            Soldier s = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
            army.add(s);
        }
    }
}
