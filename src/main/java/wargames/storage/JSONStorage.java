package wargames.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import wargames.models.*;

public class JSONStorage implements StorageStrategy {

    private static final String DEFAULT_DIRECTORY_PATH = "data";
    private static final String FILE_EXTENSION = ".json";

    private String directoryPath;

    public JSONStorage() {
        this.directoryPath = DEFAULT_DIRECTORY_PATH;
    }

    public JSONStorage(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void save(General general) throws IllegalStateException {

    }

    public void load(General general) {

    }

}
