package wargames.commands;

import java.util.List;

import wargames.models.*;
import wargames.events.publisher.EventDispatcher;
import wargames.exceptions.InsufficientGoldException;

public class DrillSoldiersCommand extends Command {
    
    public static final int EXP_INCREASE = 1;
    
    private final List<Soldier> soldiersToDrill;

    public DrillSoldiersCommand(General general,
                                List<Soldier> soldiers,
                                EventDispatcher dispatcher) {
        super(general, dispatcher);
        
        this.soldiersToDrill = soldiers;
    }
    
    @Override
    public void execute() throws InsufficientGoldException {
        if (this.soldiersToDrill == null || this.general == null) {
            throw new IllegalArgumentException("null argument passed to DrillSoldiersCommand");
        }
        
        int drillCost = calculateDrillCost(this.soldiersToDrill);
        this.general.subtractGold(drillCost);
        
        drillSoldiers(this.soldiersToDrill);
    }
    
    private int calculateDrillCost(List<Soldier> soldiers) {
        return soldiers.stream()
            .mapToInt(s -> s.getRank().getValue())
            .sum();
    }
    
    private void drillSoldiers(List<Soldier> soldiers) {
        List<Soldier> generalSoldiers = this.general.getArmy().getSoldiers();
        for (Soldier s : soldiers) {
            if (generalSoldiers.contains(s)) {
                s.increaseExpByN(EXP_INCREASE);
                continue;
            }
            throw new IllegalArgumentException("drilled soldiers must be part of drilling general's army");
        }
    }
}
