package wargames.models;

// Soldier's rank
public enum Rank {
    PRIVATE(1),
    CORPORAL(2),
    CAPTAIN(3),
    MAJOR(4);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Rank fromValue(int value) {
        for (Rank r : Rank.values()) {
            if (r.getValue() == value) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown Rank value: " + value);
    }
}
