package wargames.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import wargames.factories.*;
import wargames.exceptions.InsufficientGoldException;

class GeneralTest {

    private static final String TEST_GENERAL_NAME         = "Ferdinand Foch";
    private static final int    TEST_STARTING_GOLD        = 100;
    private static final int    RECRUITMENT_COST_PER_RANK = General.RECRUITMENT_COST_PER_RANK;

    private SoldierFactory soldierFactory;

    private General general;

    @BeforeEach
    void setUp() {
        this.soldierFactory = new SoldierFactory();
        general = new General(TEST_STARTING_GOLD, TEST_GENERAL_NAME, this.soldierFactory);
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
        assertTrue(general.getArmy().getSoldiers().isEmpty(), 
                "General's army should be empty");
        assertEquals(0, general.getArmy().getTotalStrength(), 
                "Empty army's strength should equal to 0");
    }

    @Test
    @DisplayName("recruitNSoldiersByRank – sufficient gold recruits soldiers")
    void testrecruitNSoldiersByRankWithSufficientGold() throws Exception {
        Rank soldiersRank          = Rank.PRIVATE;
        int  soldiersQuantity      = 3;
        int  expectedTotalStrength = soldiersQuantity * soldiersRank.getValue();

        int  costPerSoldier  = soldiersRank.getValue() * RECRUITMENT_COST_PER_RANK;
        int  recruitmentCost = costPerSoldier * soldiersQuantity;


        Army generalArmy = general.getArmy();

        general.recruitNSoldiersWithRank(soldiersQuantity, soldiersRank);

        assertEquals(soldiersQuantity, generalArmy.getSoldiers().size(),
                     String.format("%d soldiers should have been recruited", soldiersQuantity));
        assertEquals(soldiersQuantity, generalArmy.getTotalStrength(),
                     "Total army strength should equal to " + expectedTotalStrength);
        assertEquals(TEST_STARTING_GOLD - recruitmentCost, general.getGold(),
                     "General's gold should be reduced by the cost of recruitment");
    }

    @Test
    @DisplayName("recruitNSoldiersByRank – insufficient gold throws InsufficientGoldException")
    void testrecruitNSoldiersByRankWithInsufficientGold() {
        int lowGold = 5;
        Rank soldierRank = Rank.PRIVATE;
        int recruitmentCost = soldierRank.getValue() * RECRUITMENT_COST_PER_RANK * 1;

        General poor = new General(lowGold, "Poor", this.soldierFactory);

        InsufficientGoldException ex = assertThrows(
            InsufficientGoldException.class,
            () -> poor.recruitNSoldiersWithRank(1, soldierRank),
            "Should throw InsufficientGoldException"
        );
        assertTrue(ex.getMessage().contains(String.format("%d", lowGold)),
                   "Exception message should contain the amount of gold held");
        assertTrue(ex.getMessage().contains(String.format("%d", recruitmentCost)),
                   "Exception message should contain the amount of gold needed");
    }

    @Test
    @DisplayName("recruitNSoldiersByRank(... , 0) – does not recruit any soldiers")
    void testRecruitZeroQuantity() throws Exception {
        general.recruitNSoldiersWithRank(0, Rank.CAPTAIN);
        assertTrue(general.getArmy().getSoldiers().isEmpty(),
                   "No soldiers should have been added to the army");
    }

    @Test
    @DisplayName("recruitNSoldiersByRank(... , -n) – does not recruit any soldiers")
    void testRecruitNegativeQuantity() throws Exception {
        general.recruitNSoldiersWithRank(-3, Rank.MAJOR);
        assertTrue(general.getArmy().getSoldiers().isEmpty(),
                   "No soldiers should have been added to the army");
    }
}
