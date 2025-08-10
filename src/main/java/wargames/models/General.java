package wargames.models;

import wargames.commands.Command;
import wargames.exceptions.InsufficientGoldException;
import wargames.exceptions.LoadGeneralStateException;
import wargames.exceptions.SaveGeneralStateException;
import wargames.exceptions.StorageExceptions.LoadStorageException;
import wargames.exceptions.StorageExceptions.SaveStorageException;
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

    public void save() throws SaveGeneralStateException {
        try {
            this.storage.save(this);
        
        } catch (NullPointerException e) {
            throw new SaveGeneralStateException(
                this, "no storage assigned to the general"
            );

        } catch (SaveStorageException e) {
            throw new SaveGeneralStateException(this, e.getMessage());

        }
    }

    public void load() throws LoadGeneralStateException {
        try {
            this.storage.load(this);

        } catch (NullPointerException e) {
            throw new LoadGeneralStateException(
                this, "no storage assigned to the general"
            );

        } catch (LoadStorageException e) {
            throw new LoadGeneralStateException(this, e.getMessage());

        }
    }
}
