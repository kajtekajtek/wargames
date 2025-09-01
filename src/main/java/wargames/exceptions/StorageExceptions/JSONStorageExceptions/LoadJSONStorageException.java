package wargames.exceptions.StorageExceptions.JSONStorageExceptions;

import wargames.exceptions.StorageExceptions.LoadStorageException;

public class LoadJSONStorageException extends LoadStorageException {

    public LoadJSONStorageException(String message) {
        super(String.format(
            "could not load from JSON file: %s", message
        ));
    }

}
