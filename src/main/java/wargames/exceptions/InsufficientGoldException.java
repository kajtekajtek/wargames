package wargames.exceptions;

public class InsufficientGoldException extends Exception {
    private final int have;
    private final int need;

    public InsufficientGoldException(int have, int need) {
        super(String.format(
            "insufficient funds: you have %d, you need %d",
            have, need
        ));

        this.have = have;
        this.need = need;
    }

    public int getDeficit() {
        return have - need;
    }
}
