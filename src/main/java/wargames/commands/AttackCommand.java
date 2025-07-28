package wargames.commands;

import wargames.events.publisher.EventDispatcher;
import wargames.models.General;

public class AttackCommand extends Command {

    public static final double GOLD_LOOT_PERCENTAGE = 0.10;

    public AttackCommand(General attacking, 
                         General attacked, 
                         EventDispatcher dispatcher) {
        super(attacking, dispatcher);
    }

    @Override
    public void execute() { }
}
