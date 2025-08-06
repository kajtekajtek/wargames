package wargames.exceptions;

import wargames.models.General;

public class SaveGeneralStateException extends Exception {

    public SaveGeneralStateException(General general, String message) {
        super(String.format(
            "could not save the general state: %s: %s", 
            general.getName(), message
        ));
    }

}
