package wargames.factories;

import wargames.models.Soldier;
import wargames.models.Rank;

public class SoldierFactory {
    public Soldier createPrivate() {
        return Soldier.withRank(Rank.PRIVATE);
    }
    
    public Soldier createCorporal() {
        return Soldier.withRank(Rank.CORPORAL);
    }

    public Soldier createCaptain() {
        return Soldier.withRank(Rank.CAPTAIN);
    }

    public Soldier createMajor() {
        return Soldier.withRank(Rank.MAJOR);
    }
}