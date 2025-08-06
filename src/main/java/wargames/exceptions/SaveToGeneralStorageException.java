package wargames.exceptions;

import wargames.models.General;

public class SaveToGeneralStorageException extends GeneralStorageException {

    public SaveToGeneralStorageException(General general, String message) {
        super(String.format(
            "could not save the general state: %s: %s", 
            general.getName(), message
        ));
    }

}
