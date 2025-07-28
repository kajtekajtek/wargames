package wargames.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.util.*;

import wargames.factories.*;
import wargames.models.*;
import wargames.events.publisher.EventDispatcher;

public class AttackCommandTest {

    private static final String ATTACKER_NAME = "Agamemnon";
    private static final String ATTACKED_NAME = "Priam";
    private static final int    STARTING_GOLD = 100;

    private static final int    TEST_ARMY_SIZE     = 10;
    private static final Rank   TEST_SOLDIERS_RANK = Rank.PRIVATE;

    private static final double GOLD_TRANSFER_MULTIPLIER = 0.1;
    private static final double WINNER_GOLD_MULTIPLIER   = 1 + GOLD_TRANSFER_MULTIPLIER;
    private static final double LOSER_GOLD_MULTIPLIER    = 1 - GOLD_TRANSFER_MULTIPLIER;

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
    
    @Test
    @DisplayName("Loser gives 10% of his gold to the winner; each soldier from the losing army loses 1 exp, and each in the winning army gains 1 exp")
    void testAttackingGeneralWins() {
        General winner   = attacking;
        Army winningArmy = winner.getArmy();

        General loser    = attacked;
        Army losingArmy  = attacked.getArmy();

        winningArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));
        
        AttackCommand cmd = commandFactory.createAttack(winner, loser);
        assertDoesNotThrow(() -> cmd.execute());
        
        assertEquals(STARTING_GOLD * WINNER_GOLD_MULTIPLIER, winner.getGold());
        assertEquals(STARTING_GOLD * LOSER_GOLD_MULTIPLIER, loser.getGold());
        
        // assert exp gained
        // assert exp lost
    }
    
    @Test
    @DisplayName("Loser gives 10% of his gold to the winner; each soldier from the losing army loses 1 exp, and each in the winning army gains 1 exp")
    void testAttackedGeneralWins() {
        Army losingArmy  = attacking.getArmy();
        Army winningArmy = attacked.getArmy();
        winningArmy.add(soldierFactory.createSoldier(TEST_SOLDIERS_RANK));
        
        AttackCommand cmd = commandFactory.createAttack(attacking, attacked);
        assertDoesNotThrow(() -> cmd.execute());
        
    }
    
    @Test
    @DisplayName("In the case of a draw, each general must shoot one randomly selected soldier")
    void testDraw() {
        
    }
    
    @Test
    @DisplayName("IllegalArgumentException should be thrown if the attacking general has an empty army")
    void testAttackerWithEmptyArmy() {

    }
    
    @Test
    @DisplayName("IllegalArgumentException should be thrown if the attacked general has an empty army")
    void testAttackedWithEmptyArmy() {

    }
    
    @Test
    @DisplayName("IllegalArgumentException should be thrown if both of the generals has an empty army")
    void testBothGeneralsWithEmptyArmy() {

    }
    
    @Test
    @DisplayName("If the attacking general loses his army - then what?")
    void testAttackerLoseArmy() {
        
    }
    
    @Test
    @DisplayName("If the attacked general loses his army - then what?")
    void testAttackedLoseArmy() {
        
    }
    
    @Test
    @DisplayName("If both generals lose their armies - then what?")
    void testBothLoseArmy() {

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
            s.increaseExpByN(1);
            army.add(s);
        }
    }
}
