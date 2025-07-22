package wargames.commands;

import wargames.models.General;
import wargames.models.Soldier;
import wargames.models.Rank;

import wargames.factories.SoldierFactory;

import wargames.exceptions.InsufficientGoldException;

public class RecruitSoldiersCommand extends Command {

    public static final int COST_PER_RANK = 10;

    private final SoldierFactory soldierFactory;
    private final int  quantity;
    private final Rank rank;

    public RecruitSoldiersCommand(General general, 
                           SoldierFactory factory,
                           int quantity,
                           Rank rank) {
        super(general);

        this.soldierFactory = factory;

        this.quantity = quantity;
        this.rank     = rank;
    }    
    
    @Override
    public void execute() throws InsufficientGoldException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("soldier quantity must be positive");
        }

        int recruitmentCost = calculateRecruitmentCost(quantity, rank);
        general.subtractGold(recruitmentCost);
        
        recruitSoldiers(quantity, rank); 
    }
    
    private void recruitSoldiers(int quantity, Rank rank) {
        for (int i = 0; i < quantity; i++) {
            Soldier newSoldier = soldierFactory.createSoldier(rank);
            this.general.getArmy().add(newSoldier);
        }       
    }
    
    private static int calculateRecruitmentCost(int quantity, Rank rank) {
        int costPerSoldier = COST_PER_RANK * rank.getValue();
        int totalCost = costPerSoldier * quantity;
        return totalCost;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public Rank getRank() {
        return this.rank;
    }
}