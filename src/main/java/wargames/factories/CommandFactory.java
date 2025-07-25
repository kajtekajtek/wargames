package wargames.factories;

import java.util.List;

import wargames.commands.*;
import wargames.models.*;
import wargames.events.publisher.EventDispatcher;

public class CommandFactory {
    private final EventDispatcher eventDispatcher;
    private final SoldierFactory  soldierFactory;

    public CommandFactory(EventDispatcher d, SoldierFactory sf) {
        this.eventDispatcher = d;
        this.soldierFactory  = sf;
    }

    public RecruitSoldiersCommand createRecruitSoldiers(General general,
                                                        int quantity,
                                                        Rank rank) {
        return new RecruitSoldiersCommand(general, 
                                          soldierFactory, 
                                          quantity, 
                                          rank, 
                                          eventDispatcher);
    }
    
    public DrillSoldiersCommand createDrillSoldiers(General general,
                                                    List<Soldier> soldiers) {
        return new DrillSoldiersCommand(general, soldiers, eventDispatcher);
    } 
}
