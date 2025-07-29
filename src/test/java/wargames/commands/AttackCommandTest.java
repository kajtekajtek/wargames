package wargames.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import wargames.factories.*;
import wargames.models.*;
import wargames.events.publisher.EventDispatcher;
import wargames.exceptions.InsufficientGoldException;

public class AttackCommandTest {

    private static final String ATTACKER_NAME  = "Agamemnon";
    private static final String ATTACKED_NAME  = "Priam";
    private static final int    STARTING_GOLD  = 100;
    private static final int    TEST_ARMY_SIZE = 10;
    private static final Rank   TEST_SOLDIERS_RANK = Rank.PRIVATE;

    private static final double GOLD_LOOT_PERCENTAGE   = AttackCommand.GOLD_LOOT_PERCENTAGE;

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
        assertEquals((int) (STARTING_GOLD + loser.getGold() * GOLD_LOOT_PERCENTAGE),
                     winner.getGold());
        assertEquals((int) (STARTING_GOLD * (1 - GOLD_LOOT_PERCENTAGE)),
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
    
    @ParameterizedTest(name = "{0} with empty army throws IllegalArgumentException")
    @ValueSource(strings = {"attacker", "attacked", "both"})
    @DisplayName("IllegalArgumentException when {0} has an empty army")
    void testEmptyArmyThrows(String roleWithEmpty) {
        General withoutArmy = new General("General without an army", STARTING_GOLD);
        switch (roleWithEmpty) {
            case "attacker":
                attacking = withoutArmy;
                break;

            case "attacked":
                attacked = withoutArmy;
                break;

            case "both":
                attacking = new General("Attacking without an army", STARTING_GOLD);
                attacked  = new General("Attacked without an army", STARTING_GOLD);
                break;
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
    @DisplayName("Insufficient gold exception if the attacking general has no gold")
    void testAttackerHasNoGold() {
        assertDoesNotThrow(() -> attacking.subtractGold(STARTING_GOLD));

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertThrows(InsufficientGoldException.class, cmd::execute);

        assertEquals(0, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
        assertEquals(TEST_ARMY_SIZE, attacking.getArmy().getSize());
        assertEquals(TEST_ARMY_SIZE, attacked.getArmy().getSize());
    }
    
    @ParameterizedTest(name = "attacked has no gold and the {0} wins")
    @ValueSource(strings = {"attacker", "attacked"}) 
    @DisplayName("attacked general has no gold; the attacker wins -> winner does not get the loot; else attacked general gets the loot normally")
    void testAttackedHasNoGoldAndLoses(String winnerRole) {
        assertDoesNotThrow(() -> attacked.subtractGold(STARTING_GOLD));

        General winner = "attacker".equals(winnerRole) ? attacking : attacked;
        General loser  = "attacked".equals(winnerRole) ? attacking : attacked;

        Army victoriousArmy = winner.getArmy();
        Army defeatedArmy   = loser.getArmy();
        victoriousArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        int expectedVictoriousStrength = victoriousArmy.getSize()
                                         * TEST_SOLDIERS_RANK.getValue()
                                         * 2;
        int expectedDefeatedStrength = 0;

        assertEquals(expectedVictoriousStrength, victoriousArmy.getTotalStrength());
        assertEquals(expectedDefeatedStrength, defeatedArmy.getTotalStrength());

        if (winnerRole == "attacker") {
            assertEquals(STARTING_GOLD, winner.getGold());
            assertEquals(0, loser.getGold());
        } else {
            assertEquals((int) (STARTING_GOLD + loser.getGold() * GOLD_LOOT_PERCENTAGE), 
                         winner.getGold());
            assertEquals((int) (STARTING_GOLD * (1 - GOLD_LOOT_PERCENTAGE)),
                         loser.getGold());
        }
    }

    @Test
    @DisplayName("Loot rounding - fractional loot is floored")
    void testLootRounding() {
        attacked.addGold(1); // 101 * 0.1 = 10.1
        attacking.getArmy().add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        int expectedLoot = (int)((STARTING_GOLD + 1) * GOLD_LOOT_PERCENTAGE);
        assertEquals(STARTING_GOLD + expectedLoot, attacking.getGold());
        assertEquals(101 - expectedLoot, attacked.getGold());
    }

    @Test
    @DisplayName("Attack when each side has one soldier leads to draw")
    void testSingleSoldierDraw() {
        attacking = new General(ATTACKER_NAME, STARTING_GOLD);
        attacked  = new General(ATTACKED_NAME, STARTING_GOLD);
        Soldier sa = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
        Soldier sb = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
        attacking.getArmy().add(sa);
        attacked.getArmy().add(sb);

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        assertEquals(0, attacking.getArmy().getSize());
        assertEquals(0, attacked.getArmy().getSize());
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
    }

    @Test
    @DisplayName("Multiple consecutive draws decrease armies cumulatively")
    void testConsecutiveDraws() {
        AttackCommand cmd1 = commandFactory.createAttack(attacking, attacked);
        AttackCommand cmd2 = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd1::execute);
        assertDoesNotThrow(cmd2::execute);

        assertEquals(TEST_ARMY_SIZE - 2, attacking.getArmy().getSize());
        assertEquals(TEST_ARMY_SIZE - 2, attacked.getArmy().getSize());
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
    }
    
    void recruitTestArmy(General general) {
        Army army = general.getArmy();
        for (int i = 0; i < TEST_ARMY_SIZE; i++) {
            Soldier s = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
            army.add(s);
        }
    }
}
