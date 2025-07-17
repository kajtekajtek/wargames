package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import wargames.exceptions.InsufficientGoldException;

class GeneralTest {

    private static final String TEST_GENERAL_NAME  = "Ferdinand Foch";
    private static final int    TEST_STARTING_GOLD = 100;

    private General general;

    @BeforeEach
    void setUp() {
        general = new General(TEST_GENERAL_NAME, TEST_STARTING_GOLD);
    }

    @Test
    @DisplayName("Constructor initializes name, gold & empty army")
    void testConstructor() {
        assertEquals(TEST_GENERAL_NAME, general.getName(), 
                "General's name should be set");
        assertEquals(TEST_STARTING_GOLD, general.getGold(),
                "General's gold should be set");
        assertNotNull(general.getArmy(),
                "General's army should not be null");
        assertTrue(general.getArmy().isEmpty(), 
                "General's army should be empty");
        assertEquals(0, general.getArmy().getTotalStrength(), 
                "Empty army's strength should equal to 0");
    }
    
    @Test
    @DisplayName("subtractGold subtracts gold correctly")
    void testSubtractGoldSubtractsGold() {
        int subtrahend = 10;
        int expected   = TEST_STARTING_GOLD - subtrahend;
        
        assertDoesNotThrow(() -> general.subtractGold(subtrahend));
        
        assertEquals(expected, general.getGold());
    }
    
    @Test
    @DisplayName("subtractGold throws an exception when subtraction results in negative value")
    void testSubtractGoldInsufficientGold() {
        int subtrahend = TEST_STARTING_GOLD + 10;
        String expectedMessage = String.format(
            "insufficient funds: you have %d, you need %d",
            TEST_STARTING_GOLD, subtrahend
        );
        
        Exception exception = assertThrows(InsufficientGoldException.class, () -> {
            general.subtractGold(subtrahend);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
    
    @Test
    @DisplayName("subtractGold throws an exception when trying to subtract negative value")
    void testSubtractNegativeAmountOfGold() {
        int subtrahend = -10;
        String expectedMessage = "subtrahend should be a positive value";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            general.subtractGold(subtrahend);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
    
    @Test
    @DisplayName("subtractGold does nothing when subtrahend equals to 0")
    void testSubtractZeroGold() {
        int subtrahend = 0;
        
        assertDoesNotThrow(() -> general.subtractGold(subtrahend));
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
}