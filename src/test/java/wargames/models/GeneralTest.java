package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import wargames.exceptions.InsufficientGoldException;
import wargames.factories.GeneralFactory;

class GeneralTest {

    private static final GeneralFactory generalFactory = new GeneralFactory();

    private static final String TEST_GENERAL_NAME  = "Ferdinand Foch";
    private static final int    TEST_STARTING_GOLD = 100;

    private General general;

    @BeforeEach
    void setUp() {
        general = generalFactory.createGeneral(
            TEST_GENERAL_NAME, TEST_STARTING_GOLD
        );
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
    @DisplayName("addGold adds gold correctly")
    void testAddGoldAddsGold() {
        int summand  = 10;
        int expected = TEST_STARTING_GOLD + summand;
        
        assertDoesNotThrow(() -> general.addGold(summand));
        
        assertEquals(expected, general.getGold());
    }
    
    @Test
    @DisplayName("addGold throws an exception when addition results in integer overflow")
    void testAddGoldOverflow() {
        int summand = Integer.MAX_VALUE - TEST_STARTING_GOLD + 1;
        String expectedMessage = String.format(
            "Overflow: cannot add %d to current gold: %d",
            summand, TEST_STARTING_GOLD
        );
        
        Exception exception = assertThrows(ArithmeticException.class, () -> {
            general.addGold(summand);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
    
    @Test
    @DisplayName("addGold throws an exception when trying to add negative value")
    void testAddNegativeAmountOfGold() {
        int summand = -10;
        String expectedMessage = "summand should be a positive value";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            general.addGold(summand);
        });
        
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(TEST_STARTING_GOLD, general.getGold());
    }
    
    @Test
    @DisplayName("addGold does nothing when summand equals to 0")
    void testAddZeroGold() {
        int summand = 0;
        
        assertDoesNotThrow(() -> general.addGold(summand));
        
        assertEquals(TEST_STARTING_GOLD, general.getGold());
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