package wargames.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Army {

    private List<Soldier> soldiers;    

    // constructors
    public Army() {
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
