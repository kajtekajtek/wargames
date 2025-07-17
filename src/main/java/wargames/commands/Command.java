package wargames.commands;

import wargames.models.*;

public abstract class Command {

    public  General general;

    Command(General general) {
        this.general = general;
    }
    
    public abstract void execute() throws Exception;
}