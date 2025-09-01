package wargames.exceptions.StorageExceptions;

public class SaveStorageException extends Exception {
    public SaveStorageException(String message) {
        super(String.format(
            "could not save to storage: %s", message
        ));
    }
    
}
