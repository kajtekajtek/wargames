package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

import wargames.factories.SoldierFactory;

class ArmyTest {

    private static final int S1_STRENGTH = 2;
    private static final int S2_STRENGTH = 4;
    private static final int S3_STRENGTH = 6;
    private static final int S4_STRENGTH = 4;

    private Army           army;
    private SoldierFactory soldierFactory;
    private Soldier        s1, s2, s3, s4;

    @BeforeEach
    void setUp() {
        soldierFactory = new SoldierFactory();
        army = new Army();

        s1 = soldierFactory.createPrivate();
        s1.increaseExpByN(S1_STRENGTH / s1.getRank().getValue());

        s2 = soldierFactory.createCorporal();
        s2.increaseExpByN(S2_STRENGTH / s2.getRank().getValue());

        s3 = soldierFactory.createCaptain();
        s3.increaseExpByN(S3_STRENGTH / s3.getRank().getValue());

        s4 = soldierFactory.createMajor();
        s4.increaseExpByN(S4_STRENGTH / s4.getRank().getValue());
    }

    @Test
    @DisplayName("New army is empty and has total strength equal to 0")
    void testConstructor() {
        assertTrue(army.isEmpty());
        assertEquals(0, army.getTotalStrength());
    }

    @Test
    @DisplayName("add() adds soldier to the army")
    void testAddSingle() {
        army.add(s1);

        assertEquals(1, army.getSize(), 
                "1 soldier in the army expected");
        assertTrue(army.getSoldiers().contains(s1), 
                "Enlisted soldier should be in army");
        assertEquals(s1.getStrength(), army.getTotalStrength(), 
                "Total strength should equal to added soldier's strength");
    }

    @Test
    @DisplayName("add() adds multiple soldier's to the army")
    void testAddMultiple() {
        army.add(s1);
        army.add(s2);
        army.add(s3);
        army.add(s4);

        int expectedStrength = s1.getStrength() + s2.getStrength()
            + s3.getStrength() + s4.getStrength();
        assertEquals(4, army.getSize(),
                "There should be 4 soldiers in the army");
        assertEquals(expectedStrength, army.getTotalStrength(), 
                "Total strength should be equal to all of the soldier's strength");
    }

    @Test
    @DisplayName("killAndRemoveRandom() on an empty army does nothing")
    void testKillAndRemoveRandomEmptyArmy() {
        assertDoesNotThrow(() -> army.killAndRemoveRandom(), 
                "killAndRemoveRandom() should not throw an exception on the empty army");
        assertTrue(army.isEmpty(), 
                "Army should be empty");
        assertEquals(0, army.getTotalStrength(), 
                "Total army strength should equal to 0");
    }

    @Test
    @DisplayName("killAndRemoveRandom() kills and removes exactly one soldier")
    void testKillAndRemoveRandomRemovesOne() {
        army.add(s1);
        army.add(s2);
        army.add(s3);
        army.add(s4);
        List<Soldier> before = new ArrayList<>(army.getSoldiers());
        int beforeSize = before.size();
        int beforeStrength = army.getTotalStrength();

        army.killAndRemoveRandom();

        List<Soldier> after = army.getSoldiers();
        assertEquals(beforeSize - 1, after.size(), 
                "One soldier should have been removed");

        Set<Soldier> removed = before.stream()
            .filter(s -> !after.contains(s))
            .collect(Collectors.toSet());
        assertEquals(1, removed.size(), 
                "Exactly one soldier should have been removed");

        Soldier killed = removed.iterator().next();
        assertFalse(killed.isAlive(), 
                "Removed soldier should have been killed");

        int afterStrength = army.getTotalStrength();
        assertTrue(beforeStrength > afterStrength,
                     "Total strength should has been decreased by removed soldier's strength");
    }

    @Test
    @DisplayName("Multiple killAndRemoveRandom() eventually empty the army")
    void testMultipleKillAndRemoveRandomUntilEmpty() {
        army.add(s1);
        army.add(s2);
        army.add(s3);
        army.add(s4);

        army.killAndRemoveRandom();
        army.killAndRemoveRandom();
        army.killAndRemoveRandom();
        army.killAndRemoveRandom();

        assertTrue(army.isEmpty(), "Army should be empty");
        assertEquals(0, army.getTotalStrength(), 
                "Total army strength should equal to 0");
    }
}
