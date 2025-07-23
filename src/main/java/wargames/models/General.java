package wargames.models;

import wargames.commands.Command;
import wargames.exceptions.InsufficientGoldException;

public class General {

    private final Army   army;    
    private final String name;

    private int gold;

    public General(String name, int gold) {
        this.army = new Army();
        this.name = name;
        this.gold = gold;
    }

    public Army getArmy() {
        return this.army;
    }

    public String getName() {
        return this.name;
    }

    public int getGold() {
        return this.gold;
    }
    
    public void addGold(int goldToAdd) {
        if (goldToAdd < 0) {
            throw new IllegalArgumentException("summand should be a positive value");
        }

        if (goldToAdd > Integer.MAX_VALUE - this.gold) {
            throw new ArithmeticException("Overflow: cannot add " + goldToAdd + " to current gold: " + this.gold);
        }

        this.gold += goldToAdd;
    }
    
    public void subtractGold(int goldToSubtract) throws InsufficientGoldException {
        if (goldToSubtract < 0) {
            throw new IllegalArgumentException("subtrahend should be a positive value");
        }

        if (goldToSubtract > this.gold) {
            throw new InsufficientGoldException(this.gold, goldToSubtract);
        }
        
        this.gold -= goldToSubtract;
    }
    
    public void executeCommand(Command cmd) throws Exception {
        cmd.executeAndUpdate();
    }
}
