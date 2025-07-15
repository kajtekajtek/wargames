package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import wargames.factories.SoldierFactory;

class SoldierTest {

    private SoldierFactory  factory;
    private Soldier         soldier;

    @BeforeEach
    void setUp() {
        this.factory = new SoldierFactory();
        // rank = PRIVATE, exp = 1, alive = true
        this.soldier = factory.createPrivate(); 
    }

    @Test
    @DisplayName("Soldier creation from rank initializes soldier's rank to given, exp to 1 & alive to true")
    void testCreatePrivateInitializesFields() {
        assertEquals(Rank.PRIVATE, soldier.getRank(), "Incorrect rank");
        assertEquals(1, soldier.getExp(), "exp != 1");
        assertTrue(soldier.isAlive(), "Soldier should not be dead");
    }

    @Test
    @DisplayName("getStrength() returns rank.value * exp")
    void testGetStrength() {
        assertEquals(Rank.PRIVATE.getValue() * 1, soldier.getStrength());

        soldier.increaseExpByN(1);

        assertEquals(Rank.PRIVATE.getValue() * 2, soldier.getStrength());
    }

    @Nested
    @DisplayName("increaseExpByN() tests")
    class IncreaseExpByNTests {
        @Test
        @DisplayName("Increases exp by 1")
        void testIncreaseExpByN() {
            soldier.increaseExpByN(1);
            assertEquals(2, soldier.getExp());
            assertEquals(Rank.PRIVATE, soldier.getRank());
        }

        @ParameterizedTest(name = "Promotion when exp >= PROMOTION_RANK_MULTIPLIER * rank value ")
        @EnumSource(value = Rank.class,
                    names = {"PRIVATE","CORPORAL","CAPTAIN"},
                    mode  = EnumSource.Mode.INCLUDE)
        void testPromotionAtThreshold(Rank baseRank) {
            int threshold = Soldier.PROMOTION_RANK_MULTIPLIER * baseRank.getValue();
            int exp       = soldier.getExp();

            // arrange soldier with given rank
            switch(baseRank) {
                case PRIVATE:
                    break;
                case CORPORAL:
                    soldier = factory.createCorporal();
                    break;
                case CAPTAIN:
                    soldier = factory.createCaptain();
                    break;
                case MAJOR:
                    soldier = factory.createMajor();
                    break;
            }

            // increase exp to threshold
            soldier.increaseExpByN(threshold - exp);

            // assert promotion and exp equal to 1
            Rank expected = Rank.fromValue(baseRank.getValue() + 1);
            assertEquals(expected, soldier.getRank());
            assertEquals(1, soldier.getExp());
        }

        @Test
        @DisplayName("No promotion at max rank MAJOR")
        void testNoPromotionAtMaxRank() {
            soldier = factory.createMajor();
            int exp = soldier.getExp();

            soldier.increaseExpByN(Soldier.PROMOTION_RANK_MULTIPLIER * Rank.MAJOR.getValue() - exp);

            assertEquals(Rank.MAJOR, soldier.getRank());
            assertEquals(Rank.MAJOR.getValue() * Soldier.PROMOTION_RANK_MULTIPLIER, soldier.getExp());
        }

        @Test
        @DisplayName("No state change when soldier is dead")
        void testIncreaseExpOnDeadSoldier() {
            soldier.decreaseExpByN(1);
            assertFalse(soldier.isAlive());

            int beforeExp = soldier.getExp();
            soldier.increaseExpByN(1);

            assertEquals(beforeExp, soldier.getExp());
            assertFalse(soldier.isAlive());
        }

        @Test
        @DisplayName("Over threshold promotion")
        void testOverThresholdSinglePromotion() {
            int n = 7;
            int threshold = Soldier.PROMOTION_RANK_MULTIPLIER * Rank.PRIVATE.getValue();
            int expectedExpAfterPromotion = n - threshold + 2;

            soldier.increaseExpByN(n);

            assertEquals(Rank.CORPORAL, soldier.getRank());
            assertEquals(expectedExpAfterPromotion, soldier.getExp());
        }

        @Test
        @DisplayName("Cascade promotions after big exp increase")
        void testCascadePromotions() {
            int n          = 100;
            int expBefore  = soldier.getExp();
            int promotions = 3;

            int privateThreshold  = Soldier.PROMOTION_RANK_MULTIPLIER * Rank.PRIVATE.getValue();
            int corporalThreshold = Soldier.PROMOTION_RANK_MULTIPLIER * Rank.CORPORAL.getValue();
            int captainThreshold  = Soldier.PROMOTION_RANK_MULTIPLIER * Rank.CAPTAIN.getValue();

            int expectedExp = n - privateThreshold - corporalThreshold 
                - captainThreshold + expBefore + promotions;

            soldier.increaseExpByN(n);

            assertEquals(Rank.MAJOR, soldier.getRank());
            assertEquals(expectedExp, soldier.getExp());
        }

        @Test
        @DisplayName("increaseExpByN(0) – no state change")
        void testZeroIncreaseExpByN() {
            Rank    rankBefore  = soldier.getRank();
            int     expBefore   = soldier.getExp();
            boolean aliveBefore = soldier.isAlive();

            soldier.increaseExpByN(0);

            assertEquals(rankBefore, soldier.getRank(), "Ranga nie powinna się zmienić");
            assertEquals(expBefore, soldier.getExp(), "Exp nie powinno się zmienić");
            assertEquals(aliveBefore, soldier.isAlive(), "Stan alive nie powinien się zmienić");
        }

        @Test
        @DisplayName("increaseExpByN(-5) – no state change")
        void testNegativeIncreaseExpByN() {
            Rank    rankBefore  = soldier.getRank();
            int     expBefore   = soldier.getExp();
            boolean aliveBefore = soldier.isAlive();

            soldier.increaseExpByN(-5);

            assertEquals(rankBefore, soldier.getRank());
            assertEquals(expBefore, soldier.getExp());
            assertEquals(aliveBefore, soldier.isAlive());
        }
    }

    @Nested
    @DisplayName("decreaseExpByN() tests")
    class DecreaseExpByNTests {
        @Test
        @DisplayName("Exp should be decreased if it's above one")
        void testDecreaseExpAboveOne() {
            soldier.increaseExpByN(1); // exp=2

            soldier.decreaseExpByN(1);

            assertEquals(1, soldier.getExp());
            assertTrue(soldier.isAlive());
        }

        @Test
        @DisplayName("If exp < 1 -> soldier is dead")
        void testDecreaseExpAtOrBelowOneKills() {
            // exp == 0 -> death
            soldier.decreaseExpByN(1);

            assertFalse(soldier.isAlive());

            assertTrue(soldier.getExp() < 1);
        }

        @Test
        @DisplayName("Exp shouldn't get decreased further when it's already below 1")
        void testDecreaseExpOnDeadSoldier() {
            soldier.decreaseExpByN(1);
            assertFalse(soldier.isAlive());

            int beforeExp = soldier.getExp();
            soldier.decreaseExpByN(1);

            assertEquals(beforeExp, soldier.getExp());
            assertFalse(soldier.isAlive());
        }

        @Test
        @DisplayName("decreaseExpByN(0) – no state change")
        void testZeroDecreaseExpByN() {
            Rank    rankBefore  = soldier.getRank();
            int     expBefore   = soldier.getExp();
            boolean aliveBefore = soldier.isAlive();

            soldier.decreaseExpByN(0);

            assertEquals(rankBefore, soldier.getRank());
            assertEquals(expBefore, soldier.getExp());
            assertEquals(aliveBefore, soldier.isAlive());
        }

        @Test
        @DisplayName("decreaseExpByN(-3) – no state change")
        void testNegativeDecreaseExpByN() {
            Rank    rankBefore  = soldier.getRank();
            int     expBefore   = soldier.getExp();
            boolean aliveBefore = soldier.isAlive();

            soldier.decreaseExpByN(-3);

            assertEquals(rankBefore, soldier.getRank());
            assertEquals(expBefore, soldier.getExp());
            assertEquals(aliveBefore, soldier.isAlive());
        }
    }
}
