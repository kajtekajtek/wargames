package wargames.factories;

import wargames.models.Soldier;
import wargames.models.Rank;

public class SoldierFactory {
    public Soldier createSoldier(Rank rank) {
        return new Soldier(rank);
    }

    public Soldier createSoldier(Rank rank, int exp) {
        return new Soldier(rank, exp);
    }

    public Soldier createPrivate() {
        return new Soldier(Rank.PRIVATE);
    }
    
    public Soldier createCorporal() {
        return new Soldier(Rank.CORPORAL);
    }

    public Soldier createCaptain() {
        return new Soldier(Rank.CAPTAIN);
    }

    public Soldier createMajor() {
        return new Soldier(Rank.MAJOR);
    }
}