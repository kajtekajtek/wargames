package wargames.models;

import wargames.models.Rank;

public class Soldier
{
    private Rank    rank;
    private int     exp;
    private boolean alive;

    // constructors
    private Soldier(Rank rank, int exp, boolean alive) {
        this.rank  = rank;
        this.exp   = exp;
        this.alive = alive;
    }

    public static Soldier withRank(Rank rank) {
        return new Soldier(rank, 1, true);
    }

    // accessors
    public Rank getRank() {
        return this.rank;
    }

    public int getExp() {
        return this.exp;
    }

    public int getStrength() {
        return this.rank.getValue() * this.exp;
    }

    // predicates
    public boolean isAlive() {
        return this.alive;
    }

    // mutators
    public void incrementExp() {
        if (!this.alive) {
            return;
        }

        int rankValue = this.rank.getValue();

        this.exp += 1;

        if (this.exp >= 5 * rankValue && rankValue < Rank.MAJOR.getValue()) {
            this.rank = Rank.fromValue(rankValue + 1);
            this.exp = 1;
        }
    } 

    public void decrementExp() {
        if (!this.alive) {
            return;
        }

        if (this.exp <= 1) {
            this.alive = false;
        }

        this.exp -= 1;
    }
}
