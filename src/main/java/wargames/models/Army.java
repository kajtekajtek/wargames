package wargames.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import wargames.factories.SoldierFactory;

public class Army {

    private SoldierFactory soldierFactory;
    private List<Soldier>  soldiers;    

    // constructors
    public Army() {
        this.soldierFactory = new SoldierFactory();
        this.soldiers = new ArrayList<Soldier>();
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
        Soldier newSoldier;
        switch(rank) {
            case PRIVATE:
                newSoldier = soldierFactory.createPrivate();
                break;
            case CORPORAL:
                newSoldier = soldierFactory.createCorporal();
                break;
            case CAPTAIN:
                newSoldier = soldierFactory.createCaptain();
                break;
            case MAJOR:
                newSoldier = soldierFactory.createMajor();
                break;
            default:
                throw new IllegalArgumentException(String.format(
                    "could not create new soldier with rank %s", rank.name()));
        }
        
        add(newSoldier);
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
