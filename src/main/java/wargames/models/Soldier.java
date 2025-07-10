package wargames.models;

import wargames.models.Rank;

public class Soldier
{
    private Rank    rank;
    private int     exp;
    private boolean alive;

    public static final int PROMOTION_RANK_MULTIPLIER = 5;

    // constructors
    public static Soldier withRank(Rank rank) {
        return new Soldier(rank, 1, true);
    }

    private Soldier(Rank rank, int exp, boolean alive) {
        this.rank  = rank;
        this.exp   = exp;
        this.alive = alive;
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
    public void increaseExpByN(int n) {
        for (int i = 0; i < n; i++) {
            incrementExp();
        }
    }

    public void decreaseExpByN(int n) {
        for (int i = 0; i < n; i++) {
            decrementExp();
        }
    }

    private void incrementExp() {
        if (!this.alive) {
            return;
        }

        this.exp += 1;

        promoteIfNeeded();
    } 

    private void decrementExp() {
        if (!this.alive) {
            return;
        }

        this.exp -= 1;

        killIfNeeded();
    }

    private void promoteIfNeeded() {
        int rankValue = this.rank.getValue();
        if (this.exp >= PROMOTION_RANK_MULTIPLIER * rankValue && rankValue < Rank.MAJOR.getValue()) {
            this.rank = Rank.fromValue(rankValue + 1);
            this.exp = 1;
        }
    }

    private void killIfNeeded() {
        if (this.exp < 1) {
            this.alive = false;
        }
    }
}
