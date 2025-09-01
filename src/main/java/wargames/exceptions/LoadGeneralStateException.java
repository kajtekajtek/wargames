package wargames.exceptions;

import wargames.models.General;

public class LoadGeneralStateException extends Exception {

    public LoadGeneralStateException(General general, String message) {
        super(String.format(
            "could not load the general state: %s: %s", 
            general.getName(), message
        ));
    }

}
