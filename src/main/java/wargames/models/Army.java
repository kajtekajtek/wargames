package wargames.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import wargames.factories.SoldierFactory;

public class Army {

    private SoldierFactory soldierFactory;
    private List<Soldier>  soldiers;    

    // constructors
    public Army(SoldierFactory soldierFactory) {
        this.soldierFactory = soldierFactory;
        this.soldiers       = new ArrayList<Soldier>();
    }

    // accessors
    public List<Soldier> getSoldiers() {
        return this.soldiers;
    }    

    public int getTotalStrength() {
        int totalStrength = 0;
        for (Soldier s : this.soldiers) {
            totalStrength += s.getStrength();
        }
        return totalStrength;
    }

    // mutators
    public void addNewSoldierWithRank(Rank rank) {
        add(this.soldierFactory.createSoldierWithRank(rank));
    }

    public void add(Soldier s) {
        this.soldiers.add(s);
    }

    public void killAndRemoveRandom() {
        if (soldiers.isEmpty()) {
            return;
        }

        Random random = new Random();
        int idx = random.nextInt(soldiers.size());
        
        killAndRemove(this.soldiers.get(idx));
    }

    private void killAndRemove(Soldier soldier) {
        int soldierExp = soldier.getExp();
        soldier.decreaseExpByN(soldierExp); 

        remove(soldier);
    }

    private void remove(Soldier s) {
        this.soldiers.remove(s);
    }
}
