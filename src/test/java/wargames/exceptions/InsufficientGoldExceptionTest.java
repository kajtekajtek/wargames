package wargames.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InsufficientGoldExceptionTest {
    
    private static final int testGoldAmount = 10;

    @Test
    @DisplayName("Constructor sets correct message for have < need")
    void testMessageFormattingWhenValidDeficit() {
        String expectedMessage = String.format("insufficient funds: you have %d, you need %d", testGoldAmount - 1, testGoldAmount);

        InsufficientGoldException ex = new InsufficientGoldException(testGoldAmount - 1, testGoldAmount);

        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when have == needed")
    void testExceptionWhenZeroDeficit() {
        assertThrows(IllegalArgumentException.class, () -> {
            new InsufficientGoldException(testGoldAmount, testGoldAmount);
        });
    }
    
    @Test
    @DisplayName("Constructor throws the IllegalArgumentException when have > needed")
    void testExceptionWhenNegativeDeficit() {
        int have = testGoldAmount + 1;
        int need = testGoldAmount - 1;

        assertThrows(IllegalArgumentException.class, () -> {
            new InsufficientGoldException(have, need);
        });
    }

    @Test
    @DisplayName("getDeficit() returns need - have")
    void testGetDeficit() {
        int have = testGoldAmount - 1;
        int need = testGoldAmount + 1;

        InsufficientGoldException ex = new InsufficientGoldException(have, need);

        assertEquals(need - have, ex.getDeficit());
    }

    @Test
    @DisplayName("Exception is a subclass of Exception and has no cause by default")
    void testInheritanceAndCause() {
        int have = testGoldAmount - 1;
        int need = testGoldAmount + 1;

        InsufficientGoldException ex = new InsufficientGoldException(have, need);

        assertTrue(ex instanceof Exception);
        assertNull(ex.getCause());
    }
}