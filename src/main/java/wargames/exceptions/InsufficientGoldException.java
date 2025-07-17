package wargames.exceptions;

public class InsufficientGoldException extends Exception {
    private final int have;
    private final int need;

    public InsufficientGoldException(int have, int need) {
        super(String.format(
            "insufficient funds: you have %d, you need %d",
            have, need
        ));
        
        if (need - have <= 0) {
            throw new IllegalArgumentException("tried to create insufficient gold exception with no deficit");
        }

        this.have = have;
        this.need = need;
    }

    public int getDeficit() {
        return need - have;
    }
}
