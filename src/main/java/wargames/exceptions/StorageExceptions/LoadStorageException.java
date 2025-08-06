package wargames.exceptions.StorageExceptions;

public class LoadStorageException extends Exception {

    public LoadStorageException(String message) {
        super(String.format(
            "could not load from storage: %s", message
        ));
    }
}
