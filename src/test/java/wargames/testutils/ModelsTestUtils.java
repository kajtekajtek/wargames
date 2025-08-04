package wargames.testutils;

import static org.junit.jupiter.api.Assertions.*;

import wargames.models.*;

public final class ModelsTestUtils {

    private ModelsTestUtils() { }

    public static void assertEqualGenerals(General expected, General actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGold(), actual.getGold());

        Army expectedArmy = expected.getArmy();
        Army actualArmy   = actual.getArmy(); 
        assertEqualArmies(expectedArmy, actualArmy); 
    }

    public static void assertEqualArmies(Army expected, Army actual) {
        assertEquals(expected.getSize(), actual.getSize());

        for (int i = 0; i < expected.getSize(); i++) {
            Soldier expectedSoldier = expected.getSoldiers().get(i);
            Soldier actualSoldier   = actual.getSoldiers().get(i);
            assertEqualSoldiers(expectedSoldier, actualSoldier);
        }
    }

    public static void assertEqualSoldiers(Soldier expected, Soldier actual) {
        assertEquals(expected.getExp(),  actual.getExp());
        assertEquals(expected.getRank(), actual.getRank());
        assertEquals(expected.isAlive(), actual.isAlive());
    }
}
