package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {

    @ParameterizedTest(name = "{0}.getValue() returns {1}")
    @CsvSource({
        "PRIVATE,1",
        "CORPORAL,2",
        "CAPTAIN,3",
        "MAJOR,4"
    })
    @DisplayName("getValue() returns correct value for each rank")
    void testGetValue(Rank rank, int expectedValue) {
        assertEquals(expectedValue, rank.getValue());
    }

    @ParameterizedTest(name = "fromValue({0}) returns {1}")
    @CsvSource({
        "1,PRIVATE",
        "2,CORPORAL",
        "3,CAPTAIN",
        "4,MAJOR"
    })
    @DisplayName("fromValue() returns correct enum for each value")
    void testFromValueValid(int value, Rank expected) {
        assertEquals(expected, Rank.fromValue(value));
    }

    @ParameterizedTest(name = "fromValue({0}) throws exception")
    @ValueSource(ints = {0, -1, 5, 999})
    @DisplayName("fromValue() throws IllegalArgumentException for incorrect values")
    void testFromValueInvalid(int invalidValue) {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> Rank.fromValue(invalidValue));

        assertTrue(ex.getMessage().contains(String.valueOf(invalidValue)));
    }

    @Test
    @DisplayName("values() includes all ranks in declared order")
    void testValuesOrder() {
        Rank[] ranks = Rank.values();
        assertArrayEquals(
            new Rank[]{Rank.PRIVATE, Rank.CORPORAL, Rank.CAPTAIN, Rank.MAJOR},
            ranks
        );

        assertEquals(4, ranks.length);
    }
}