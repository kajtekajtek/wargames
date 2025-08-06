package wargames.exceptions;

import wargames.models.General;

public class LoadFromGeneralStorageException extends GeneralStorageException {

    public LoadFromGeneralStorageException(General general, String message) {
        super(String.format(
            "could not load the general state: %s: %s", 
            general.getName(), message
        ));
    }

}
