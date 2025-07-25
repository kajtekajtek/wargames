package wargames.commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.EnumSource;

import wargames.events.publisher.EventDispatcher;
import wargames.exceptions.InsufficientGoldException;
import wargames.factories.*;
import wargames.models.*;

public class RecruitSoldiersCommandTest {
    
    private static final String TEST_GENERAL_NAME  = "Władysław Sikorski";
    private static final int    TEST_STARTING_GOLD = 200;
    private static final int    COST_PER_RANK      = RecruitSoldiersCommand.COST_PER_RANK;

    private final SoldierFactory  soldierFactory = new SoldierFactory();
    private final EventDispatcher dispatcher     = EventDispatcher.getInstance();
    private final CommandFactory  commandFactory = new CommandFactory(dispatcher, soldierFactory);

    private General general;

    @BeforeEach
    void setUp() {
        general = new General(TEST_GENERAL_NAME, TEST_STARTING_GOLD);
    }
    
    @Test
    @DisplayName("IllegalArgumentException on negative quantity")
    void testRecruitSoldiersCommandNegativeQuantity() {
        int     negativeQuantity = -10;
        Command recruitSoldiers  = commandFactory.createRecruitSoldiers(
            general, negativeQuantity, Rank.PRIVATE
        );
        
        assertThrows(IllegalArgumentException.class, () -> { 
            general.executeCommand(recruitSoldiers); 
        });
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertTrue(general.getArmy().isEmpty());
    }

    @Test
    @DisplayName("IllegalArgumentException on quantity equal to zero")
    void testRecruitSoldiersCommandZeroQuantity() {
        Command recruitSoldiers = commandFactory.createRecruitSoldiers(
            general, 0, Rank.PRIVATE
        );
        
        assertThrows(IllegalArgumentException.class, () -> { 
            general.executeCommand(recruitSoldiers); 
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

        Command recruitSoldiers = commandFactory.createRecruitSoldiers(general, quantity, testRank);
        Exception e = assertThrows(InsufficientGoldException.class, () -> { 
            general.executeCommand(recruitSoldiers); 
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
        Command recruitSoldiersCommand = commandFactory.createRecruitSoldiers(
            general, 1, rank
        );

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

        Command recruitSoldiersCommand = commandFactory.createRecruitSoldiers(
            general, quantity, rank
        );
        
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
        
        Command recruitSoldiersCommand = commandFactory.createRecruitSoldiers(
            general, quantity, rank
        );
        
        assertDoesNotThrow(() -> general.executeCommand(recruitSoldiersCommand));
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
        assertEquals(quantity, general.getArmy().getSize());
    }
}