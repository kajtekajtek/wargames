package wargames.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import wargames.events.publisher.EventDispatcher;
import wargames.exceptions.InsufficientGoldException;
import wargames.factories.*;
import wargames.models.*;


public class DrillSoldiersCommandTest {

    private static final String TEST_GENERAL_NAME  = "Joshua";
    private static final int    TEST_STARTING_GOLD = 40;
    private static final int    EXP_INCREASE       = DrillSoldiersCommand.EXP_INCREASE;

    private final SoldierFactory  soldierFactory = new SoldierFactory();
    private final EventDispatcher dispatcher     = EventDispatcher.getInstance();
    private final CommandFactory  commandFactory = new CommandFactory(dispatcher, soldierFactory);
    
    private General general;
    private Soldier s1, s2, s3, s4;

    @BeforeEach
    void setUp() {
        general   = new General(TEST_GENERAL_NAME, TEST_STARTING_GOLD);
        Army army = general.getArmy();

        s1 = soldierFactory.createPrivate();
        s2 = soldierFactory.createCorporal();
        s3 = soldierFactory.createCaptain();
        s4 = soldierFactory.createMajor();
        
        army.add(s1); army.add(s2); army.add(s3); army.add(s4);
    }
    
    @Test
    @DisplayName("DrillSoldiersCommand should increase exp of given soldiers")
    void testDrillOnlyGivenSubset() {
        List<Soldier> armySubset = List.of(s1, s3);
        int drillCost = getDrillCost(armySubset);
        DrillSoldiersCommand cmd = commandFactory.createDrillSoldiers(general, armySubset);

        assertDoesNotThrow(() -> cmd.execute());

        assertEquals(1 + EXP_INCREASE, s1.getExp());
        assertEquals(1 + EXP_INCREASE, s3.getExp());
        assertEquals(1, s2.getExp());
        assertEquals(1, s4.getExp());
        assertEquals(TEST_STARTING_GOLD - drillCost, general.getGold());
        assertEquals(drillCost, cmd.getCost());
        assertEquals(2, cmd.getQuantity());
    }

    @Test
    @DisplayName("DrillSoldiersCommand should increase exp of all soldiers in army")
    void testDrillWholeArmy() {
        List<Soldier> soldiersToDrill = general.getArmy().getSoldiers();
        int drillCost = getDrillCost(soldiersToDrill);
        DrillSoldiersCommand cmd = commandFactory.createDrillSoldiers(general, soldiersToDrill);

        assertDoesNotThrow(() -> cmd.execute());

        assertEquals(1 + EXP_INCREASE, s1.getExp());
        assertEquals(1 + EXP_INCREASE, s2.getExp());
        assertEquals(1 + EXP_INCREASE, s3.getExp());
        assertEquals(1 + EXP_INCREASE, s4.getExp());
        assertEquals(TEST_STARTING_GOLD - drillCost, general.getGold());
    }

    @Test
    @DisplayName("DrillSoldiersCommand should not increase exp of soldiers outside of executing general's army")
    void testDoNotDrillOutsiders() {
        Soldier outsider = soldierFactory.createPrivate();
        DrillSoldiersCommand drillSoldiers = commandFactory.createDrillSoldiers(general, List.of(outsider));

        assertThrows(IllegalArgumentException.class, () -> { drillSoldiers.execute(); });
        
        assertEquals(1, outsider.getExp());
        assertEquals(TEST_STARTING_GOLD - outsider.getRank().getValue(), general.getGold());
    }
    
    @Test
    @DisplayName("DrillSoldiersCommand should throw exception on null general argument")
    void testThrowExceptionOnNullGeneral() {
        DrillSoldiersCommand cmdNullGeneral  = commandFactory.createDrillSoldiers(null, new ArrayList<Soldier>());

        assertThrows(IllegalArgumentException.class, () -> { cmdNullGeneral.execute(); });
    }
    
    @Test
    @DisplayName("DrillSoldiersCommand should throw exception on null soldiers argument")
    void testThrowExceptionOnNullSoldiers() {
        DrillSoldiersCommand cmdNullSoldiers = commandFactory.createDrillSoldiers(general, null);

        assertThrows(IllegalArgumentException.class, () -> { cmdNullSoldiers.execute(); });
    }
    
    @Test
    @DisplayName("DrillSoldiersCommand should do nothing on empty soldiers list")
    void testDoNothingOnEmptySoldiersList() {
        DrillSoldiersCommand cmd = commandFactory.createDrillSoldiers(general, new ArrayList<Soldier>());

        assertDoesNotThrow(() -> cmd.execute());
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
    
    @Test
    @DisplayName("InsufficientGoldException on insufficient gold for drill")
    void testInsufficientGold() {
        DrillSoldiersCommand cmd = commandFactory.createDrillSoldiers(general, List.of(s1));
        assertDoesNotThrow(() -> general.subtractGold(TEST_STARTING_GOLD));

        assertThrows(InsufficientGoldException.class, () -> cmd.execute());

        assertEquals(0, general.getGold());
        assertEquals(1, s1.getExp());
    }
    
    private int getDrillCost(List<Soldier> soldiers) {
        return soldiers.stream()
            .mapToInt(s -> s.getRank().getValue())
            .sum();
    }
}