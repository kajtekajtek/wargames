package wargames.testutils;

import wargames.models.*;

public final class ModelsTestUtils {

    private ModelsTestUtils() { }

    public static void assertEquals(General expected, General actual) {
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.getName(), actual.getName());
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.getGold(), actual.getGold());

        Army expectedArmy = expected.getArmy();
        Army actualArmy   = actual.getArmy(); 
        assertEquals(expectedArmy, actualArmy); 
    }

    public static void assertEquals(Army expected, Army actual) {
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.getSize(), actual.getSize());

        for (int i = 0; i < expected.getSize(); i++) {
            Soldier expectedSoldier = expected.getSoldiers().get(i);
            Soldier actualSoldier   = actual.getSoldiers().get(i);
            assertEquals(expectedSoldier, actualSoldier);
        }
    }

    public static void assertEquals(Soldier expected, Soldier actual) {
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.getExp(),  actual.getExp());
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.getRank(), actual.getRank());
        org.junit.jupiter.api.Assertions
            .assertEquals(expected.isAlive(), actual.isAlive());
    }
}
