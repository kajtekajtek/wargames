package wargames.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import wargames.exceptions.InsufficientGoldException;
import wargames.factories.SoldierFactory;
import wargames.models.General;
import wargames.models.Rank;
import wargames.models.Soldier;

public class RecruitSoldiersCommandTest {
    
    private static final String TEST_GENERAL_NAME  = "Władysław Sikorski";
    private static final int    TEST_STARTING_GOLD = 200;
    private static final int    COST_PER_RANK      = RecruitSoldiersCommand.COST_PER_RANK;

    private SoldierFactory factory;
    private General        general;

    @BeforeEach
    void setUp() {
        factory = new SoldierFactory();
        general = new General(TEST_GENERAL_NAME, TEST_STARTING_GOLD);
    }
    
    @Test
    @DisplayName("IllegalArgumentException on negative quantity")
    void testRecruitSoldiersCommandNegativeQuantity() {
        int negativeQuantity = -10;
        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, negativeQuantity, Rank.PRIVATE);
        
        assertThrows(IllegalArgumentException.class, () -> { 
            general.executeCommand(recruitSoldiersCommand); 
        });
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertTrue(general.getArmy().isEmpty());
    }

    @Test
    @DisplayName("IllegalArgumentException on quantity equal to zero")
    void testRecruitSoldiersCommandZeroQuantity() {
        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, 0, Rank.PRIVATE);
        
        assertThrows(IllegalArgumentException.class, () -> { 
            general.executeCommand(recruitSoldiersCommand); 
        });
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertTrue(general.getArmy().isEmpty());
    }

    @Test
    @DisplayName("InsufficientGoldException on insufficient gold for recruitment")
    void testRecruitSoldiersCommandInsufficientGold() {
        Rank testRank       = Rank.MAJOR;
        int  costPerSoldier = COST_PER_RANK * testRank.getValue();
        int  quantity       = TEST_STARTING_GOLD / costPerSoldier + 1;

        String expectedMessage = String.format(
            "insufficient funds: you have %d, you need %d",
            TEST_STARTING_GOLD, costPerSoldier * quantity
        );

        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, quantity, testRank);
        Exception e = assertThrows(InsufficientGoldException.class, () -> { 
            general.executeCommand(recruitSoldiersCommand); 
        });
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertTrue(general.getArmy().isEmpty());
        assertEquals(expectedMessage, e.getMessage());
    }
    
    @Test
    @DisplayName("One soldier added to the army on correct recruitment")
    void testRecruitSoldiersCommandOneSoldier() {
        Rank rank = Rank.PRIVATE;
        int soldierCost = rank.getValue() * COST_PER_RANK;
        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, 1, rank);

        assertDoesNotThrow(() -> general.executeCommand(recruitSoldiersCommand));
        
        assertEquals(TEST_STARTING_GOLD - soldierCost, general.getGold());
        assertEquals(1, general.getArmy().getSize());
    }

    @ParameterizedTest(name = "Valid recruitment of every rank")
    @EnumSource(value = Rank.class,
                names = {"PRIVATE","CORPORAL","CAPTAIN", "MAJOR"},
                mode  = EnumSource.Mode.INCLUDE)
    void testRecruitSoldiersCommandEveryRank(Rank rank) {
        int costPerSoldier = rank.getValue() * COST_PER_RANK;         
        int quantity       = TEST_STARTING_GOLD / costPerSoldier;
        int totalCost      = quantity * costPerSoldier;
        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, quantity, rank);
        
        assertDoesNotThrow(() -> general.executeCommand(recruitSoldiersCommand));
        
        assertEquals(TEST_STARTING_GOLD - totalCost, general.getGold());
        assertEquals(quantity, general.getArmy().getSize());
        for (Soldier s : general.getArmy().getSoldiers()) {
            assertTrue(s.getRank() == rank);
        }
    }    
 
    @Test
    @DisplayName("Valid recruitment of very big number of soldiers")
    void testRecruitSoldiersCommandBigQuantity() {
        Rank rank           = Rank.PRIVATE;
        int  costPerSoldier = rank.getValue() * COST_PER_RANK;
        int  quantity       = 100000;
        int  totalCost      = quantity * costPerSoldier;        

        general.addGold(totalCost);
        
        Command recruitSoldiersCommand = new RecruitSoldiersCommand(general, factory, quantity, rank);
        
        assertDoesNotThrow(() -> general.executeCommand(recruitSoldiersCommand));
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertEquals(quantity, general.getArmy().getSize());
    }
}