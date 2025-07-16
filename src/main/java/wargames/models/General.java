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
    
    public void subtractGold(int gold) throws InsufficientGoldException {
        if (gold < 0) {
            throw new IllegalArgumentException("subtrahend should be a positive value");
        }

        if (gold > this.gold) {
            throw new InsufficientGoldException(this.gold, gold);
        }
        
        this.gold -= gold;
    }
    
    public void executeCommand(Command cmd) throws Exception {
        cmd.execute();
    }
}