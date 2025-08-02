package wargames.models;

import wargames.commands.Command;
import wargames.exceptions.InsufficientGoldException;
import wargames.storage.*;

public class General {

    private Army   army;    
    private String name;
    private int    gold;
    private StorageStrategy storage;

    public General(Army army, String name, int gold, StorageStrategy storage) {
        this.army    = army;
        this.name    = name;
        this.gold    = gold;
        this.storage = storage;
    }

    public Army   getArmy() { return this.army; }
    public String getName() { return this.name; }
    public int    getGold() { return this.gold; }

    public void setArmy(Army army)   { this.army = army; }
    public void setName(String name) { this.name = name; }
    public void setGold(int gold)    { this.gold = gold; }
    public void setStorage(StorageStrategy storage) { this.storage = storage; }
    
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

    public void save() {
        this.storage.save(this);
    }

    public void load() {
        this.storage.load(this);
    }
}
