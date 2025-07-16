package wargames.models;

import wargames.exceptions.InsufficientGoldException;

import wargames.factories.*;

public class General {

    private final Army   army;    
    private final String name;
    private int gold;

    public static final int RECRUITMENT_COST_PER_RANK = 10;

    // constructors
    public General(int gold, String name, SoldierFactory soldierFactory) {
        this.army = new Army(soldierFactory);
        this.name = name;
        this.gold = gold;
    }

    // accessors
    public Army getArmy() {
        return this.army;
    }

    public String getName() {
        return this.name;
    }

    public int getGold() {
        return this.gold;
    }
 
    // predicates

    // mutators
    public void recruitNSoldiersByRank(int quantity, Rank rank) throws InsufficientGoldException {
        int totalCost = calculateRecruitmentCost(quantity, rank);
        if (totalCost > this.gold) { 
            throw new InsufficientGoldException(this.gold, totalCost);
        }

        this.gold -= totalCost;

        for (int i = 0; i < quantity; i++) {
            this.army.addNewSoldierWithRank(rank);
        }
    } 
    
    private static int calculateRecruitmentCost(int quantity, Rank rank) {
        int costPerSoldier = RECRUITMENT_COST_PER_RANK * rank.getValue();
        int totalCost = costPerSoldier * quantity;
        return totalCost;
    }
}
