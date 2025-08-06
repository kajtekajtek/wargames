package wargames.exceptions.StorageExceptions.JSONStorageExceptions;

import wargames.exceptions.StorageExceptions.SaveStorageException;

public class SaveJSONStorageException extends SaveStorageException {
    public SaveJSONStorageException(String message) {
        super(String.format(
            "could not save to JSON file: %s", message
        ));
    }
}
