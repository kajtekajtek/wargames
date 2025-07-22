package wargames.factories;

import wargames.commands.RecruitSoldiersCommand;
import wargames.events.publisher.EventDispatcher;
import wargames.models.*;

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
}
