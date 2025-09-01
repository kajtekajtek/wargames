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

    private static final double GOLD_LOOT_PERCENTAGE   = AttackCommand.GOLD_LOOT_PERCENTAGE;

    private static final String ATTACKER_NAME = "Agamemnon";
    private static final String ATTACKED_NAME = "Priam";
    private static final int    STARTING_GOLD = 100;

    private static final int  TEST_ARMY_SIZE     = 10;
    private static final Rank TEST_SOLDIERS_RANK = Rank.PRIVATE;

    private final EventDispatcher dispatcher = EventDispatcher.getInstance();

    private final SoldierFactory soldierFactory = new SoldierFactory();
    private final CommandFactory commandFactory = new CommandFactory(dispatcher, soldierFactory);
    private final GeneralFactory generalFactory = new GeneralFactory();
    
    private General attacking;
    private General attacked;

    @BeforeEach
    void setUp() {
        attacking = generalFactory.createGeneral(ATTACKER_NAME, STARTING_GOLD);
        attacked  = generalFactory.createGeneral(ATTACKED_NAME, STARTING_GOLD);
        
        setUpTestArmy(attacking);
        setUpTestArmy(attacked);
    }
    
    @ParameterizedTest(name = "{0} wins the battle")
    @CsvSource({
        "attacker, attacked",
        "attacked, attacker"
    })
    @DisplayName("Loser should give 10% of gold to the winner. 1 exp should be gained and lost in winner's and loser's armies respectively")
    void testGeneralWins(String winnerRole, String loserRole) {
        General winner = "attacker".equals(winnerRole) ? attacking : attacked;
        General loser  = "attacker".equals(loserRole)  ? attacking : attacked;

        Army winnerArmy = winner.getArmy();
        Army loserArmy  = loser.getArmy();
        winnerArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        int expectedVictoriousStrength = winnerArmy.getSize()
                                         * TEST_SOLDIERS_RANK.getValue()
                                         * 2;
        int expectedDefeatedStrength = 0;

        assertEquals(expectedVictoriousStrength,
                     winnerArmy.getTotalStrength());
        assertEquals(expectedDefeatedStrength,
                     loserArmy.getTotalStrength());
        assertAttackingAndAttacked(cmd);
        assertWinnerEqualsTo(cmd, winner);
        assertGoldTransfer(loser, winner);
    }
    
    @Test
    @DisplayName("In the case of a draw, each general must shoot one randomly selected soldier")
    void testDraw() {
        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        assertAttackingAndAttacked(cmd);
        assertDraw(cmd);
        assertBothArmiesSizeEqualsTo(TEST_ARMY_SIZE - 1);
        assertGoldNotChanged();
    }
    
    @ParameterizedTest(name = "{0} with empty army throws IllegalArgumentException")
    @ValueSource(strings = {"attacker", "attacked", "both"})
    @DisplayName("IllegalArgumentException when {0} has an empty army")
    void testEmptyArmyThrows(String roleWithEmpty) {
        General withoutArmy = generalFactory.createGeneral(
            "General without an army", STARTING_GOLD
        );
        switch (roleWithEmpty) {
            case "attacker":
                attacking = withoutArmy;
                break;

            case "attacked":
                attacked = withoutArmy;
                break;

            case "both":
                attacking = generalFactory.createGeneral(
                    "Attacking without an army", STARTING_GOLD
                );
                attacked  = generalFactory.createGeneral(
                    "Attacked without an army", STARTING_GOLD
                );
                break;
        }

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertThrows(IllegalArgumentException.class, cmd::execute);

        assertAttackingAndAttacked(cmd);
        assertGoldNotChanged();
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

        assertAttackingAndAttacked(cmd);
        assertEquals(0, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
        assertBothArmiesSizeEqualsTo(TEST_ARMY_SIZE);
    }
    
    @ParameterizedTest(name = "attacked has no gold and the {0} wins")
    @ValueSource(strings = {"attacker", "attacked"}) 
    @DisplayName("attacked general has no gold; the attacker wins -> winner does not get the loot; else attacked general gets the loot normally")
    void testAttackedHasNoGold(String winnerRole) {
        assertDoesNotThrow(() -> attacked.subtractGold(STARTING_GOLD));

        General winner = "attacker".equals(winnerRole) ? attacking : attacked;
        General loser  = "attacked".equals(winnerRole) ? attacking : attacked;

        Army winnerArmy = winner.getArmy();
        Army loserArmy  = loser.getArmy();
        winnerArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        int expectedVictoriousStrength = winnerArmy.getSize()
                                         * TEST_SOLDIERS_RANK.getValue()
                                         * 2;
        int expectedDefeatedStrength = 0;

        assertAttackingAndAttacked(cmd);
        assertWinnerEqualsTo(cmd, winner);
        assertEquals(expectedVictoriousStrength, winnerArmy.getTotalStrength());
        assertEquals(expectedDefeatedStrength, loserArmy.getTotalStrength());

        if (winnerRole == "attacker") {
            assertEquals(STARTING_GOLD, winner.getGold());
            assertEquals(0, loser.getGold());

        } else if (winnerRole == "attacked") {
            assertGoldTransfer(loser, winner);    
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
    @DisplayName("Should draw on to armies with 1 soldier")
    void testSingleSoldierDraw() {
        attacking = generalFactory.createGeneral(
            ATTACKER_NAME, STARTING_GOLD
        );
        attacked  = generalFactory.createGeneral(
            ATTACKED_NAME, STARTING_GOLD
        );
        Soldier sa = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
        Soldier sb = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
        attacking.getArmy().add(sa);
        attacked.getArmy().add(sb);

        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd::execute);

        assertAttackingAndAttacked(cmd);
        assertDraw(cmd);
        assertBothArmiesSizeEqualsTo(0);
        assertGoldNotChanged();
    }

    @Test
    @DisplayName("Should decrease armies cumulatively on multiple consecutive draws")
    void testConsecutiveDraws() {
        AttackCommand cmd1 = commandFactory.createAttack(attacking, attacked);
        AttackCommand cmd2 = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(cmd1::execute);
        assertDoesNotThrow(cmd2::execute);

        assertDraw(cmd1);
        assertDraw(cmd2);
        assertBothArmiesSizeEqualsTo(TEST_ARMY_SIZE - 2);
        assertGoldNotChanged();
    }

    private void assertBothArmiesSizeEqualsTo(int size) {
        assertEquals(size, attacking.getArmy().getSize());
        assertEquals(size, attacked.getArmy().getSize());
    }

    private void assertGoldTransfer(General loser, General winner) {
        assertEquals((int) (STARTING_GOLD * (1 - GOLD_LOOT_PERCENTAGE)),
                     loser.getGold());
        assertEquals((int) (STARTING_GOLD + (STARTING_GOLD * GOLD_LOOT_PERCENTAGE)), 
                     winner.getGold());
    }

    private void assertGoldNotChanged() {
        assertEquals(STARTING_GOLD, attacking.getGold());
        assertEquals(STARTING_GOLD, attacked.getGold());
    }

    private void assertAttackingAndAttacked(AttackCommand cmd) {
        assertSame(attacking, cmd.getAttacking());
        assertSame(attacked, cmd.getAttacked());
    }

    private void assertWinnerEqualsTo(AttackCommand cmd, General winner) {
        assertTrue(cmd.isAttackOver());
        assertSame(winner, cmd.getWinner());
    }

    private void assertDraw(AttackCommand cmd) {
        assertTrue(cmd.isDraw());
    }
    
    private void setUpTestArmy(General general) {
        Army army = general.getArmy();
        for (int i = 0; i < TEST_ARMY_SIZE; i++) {
            Soldier s = soldierFactory.createSoldier(TEST_SOLDIERS_RANK);
            army.add(s);
        }
    }
}
