package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SoldierTest {
    private Soldier soldier;

    @BeforeEach
    void setUp() {
        // rank = PRIVATE, exp = 1, alive = true
        soldier = Soldier.withRank(Rank.PRIVATE);
    }

    @Test
    @DisplayName("Soldier.withRank() initializes soldier's rank, exp = 1 & alive = true")
    void testWithRankInitializesFields() {
        assertEquals(Rank.PRIVATE, soldier.getRank(), "Incorrect rank");
        assertEquals(1, soldier.getExp(), "exp != 1");
        assertTrue(soldier.isAlive(), "Soldier should not be dead");
    }

    @Test
    @DisplayName("getStrength() returns rank.value * exp")
    void testGetStrength() {
        assertEquals(Rank.PRIVATE.getValue() * 1, soldier.getStrength());

        soldier.incrementExp();

        assertEquals(Rank.PRIVATE.getValue() * 2, soldier.getStrength());
    }

    @Nested
    @DisplayName("incrementExp() tests")
    class IncrementExpTests {
        @Test
        @DisplayName("Increases exp by 1")
        void testIncrementExp() {
            soldier.incrementExp();
            assertEquals(2, soldier.getExp());
            assertEquals(Rank.PRIVATE, soldier.getRank());
        }

        @ParameterizedTest(name = "Promotion when exp >= 5 * rank value ")
        @EnumSource(value = Rank.class,
                    names = {"PRIVATE","CORPORAL","CAPTAIN","MAJOR"},
                    mode = EnumSource.Mode.INCLUDE)
        void testPromotionAtThreshold(Rank baseRank) {
            // arrange soldier with given rank
            soldier = Soldier.withRank(baseRank);
            int threshold = 5 * baseRank.getValue();

            // increment exp threshold times
            for (int i = 0; i < threshold; i++) {
                soldier.incrementExp();
            }

            // assert promotion and exp equal to 1
            Rank expected = Rank.fromValue(baseRank.getValue() + 1);
            assertEquals(expected, soldier.getRank());
            assertEquals(1, soldier.getExp());
        }

        @Test
        @DisplayName("No promotion at max rank MAJOR")
        void testNoPromotionAtMaxRank() {
            soldier = Soldier.withRank(Rank.MAJOR);

            for (int i = 0; i < 5 * Rank.MAJOR.getValue(); i++) {
                soldier.incrementExp();
            }
            assertEquals(Rank.MAJOR, soldier.getRank());
            assertEquals(Rank.MAJOR.getValue() * 5, soldier.getExp());
        }
    }
}
