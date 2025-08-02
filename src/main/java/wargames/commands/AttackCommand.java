package wargames.commands;

import wargames.events.publisher.EventDispatcher;
import wargames.exceptions.InsufficientGoldException;
import wargames.models.Army;
import wargames.models.General;
import wargames.models.Soldier;

public class AttackCommand extends Command {

    public static final double GOLD_LOOT_PERCENTAGE = 0.10;

    private final General attacking;
    private final General attacked;

    private General winner;
    private Boolean isOver;

    public AttackCommand(General attacking, 
                         General attacked, 
                         EventDispatcher dispatcher) {
        super(attacking, dispatcher);

        this.isOver = false;

        this.attacking = attacking;
        this.attacked  = attacked;
    }

    public General getAttacking() { return this.attacking; }
    public General getAttacked()  { return this.attacked; }
    public General getWinner()    { return this.winner; }

    public Boolean isAttackOver() { return this.isOver; }
    public Boolean isDraw()       { return this.isAttackOver() && this.getWinner() == null; }

    @Override
    public void execute() throws Exception {
        if (this.attacking.equals(this.attacked)) {
            throw new IllegalArgumentException("attacking and attacked are the same general");
        }
        throwIfEitherArmyIsEmpty();
        throwIfAttackerHasNoGold();

        Army attackingArmy = this.attacking.getArmy();
        Army attackedArmy  = this.attacked.getArmy();

        int attackingStrength = attackingArmy.getTotalStrength();
        int attackedStrength  = attackedArmy.getTotalStrength();

        // if draw -> each general shoots random soldier
        if (attackingStrength == attackedStrength) {
            handleDraw();

        // if winner -> handle loot, exp gained and lost
        } else if (attackingStrength > attackedStrength) {
            handleWinnerAndLoser(this.attacking, this.attacked);
        } else {
            handleWinnerAndLoser(this.attacked, this.attacking);
        }

        this.isOver = true;
    }

    private void throwIfEitherArmyIsEmpty() throws IllegalArgumentException {
        Army attackingArmy = this.attacking.getArmy();
        Army attackedArmy  = this.attacked.getArmy();

        if (attackingArmy.isEmpty() && attackedArmy.isEmpty()) {
            throw new IllegalArgumentException("both attacking and attacked armies are empty");

        } else if (attackingArmy.isEmpty()) {
            throw new IllegalArgumentException("attacking army is empty");

        } else if (attackedArmy.isEmpty()) {
            throw new IllegalArgumentException("attacked army is empty");
        }
    }

    private void throwIfAttackerHasNoGold() throws InsufficientGoldException {
        int attackerGold = this.attacking.getGold();
        if (attackerGold < 1) {
            throw new InsufficientGoldException(attackerGold, 1);
        }
    }

    private void handleDraw() {
        this.attacking.getArmy().killAndRemoveRandom();
        this.attacked.getArmy().killAndRemoveRandom();
    }

    private void handleWinnerAndLoser(General winner, General loser) throws InsufficientGoldException {
        transferLoot(winner, loser);
        handleExpGainAndLoss(winner, loser);
        this.winner = winner;
    }

    private void transferLoot(General winner, General loser) throws InsufficientGoldException {
        int loot = (int) (loser.getGold() * GOLD_LOOT_PERCENTAGE);
        winner.addGold(loot);
        loser.subtractGold(loot);
    }

    private void handleExpGainAndLoss(General winner, General loser) {
        Army winnerArmy = winner.getArmy();
        Army loserArmy  = loser.getArmy();

        for (Soldier s : winnerArmy.getSoldiers()) {
            s.increaseExpByN(1);
        }

        for (Soldier s : loserArmy.getSoldiers()) {
            s.decreaseExpByN(1);
        }
    }
}
