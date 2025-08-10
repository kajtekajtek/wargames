package wargames.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import wargames.exceptions.StorageExceptions.JSONStorageExceptions.*;
import wargames.models.*;

public class JSONStorage implements StorageStrategy {

    private static final String DEFAULT_DIRECTORY_PATH = "data";
    private static final String FILE_EXTENSION = ".json";

    private final ObjectMapper mapper = Mapper.getInstance();

    private String directoryPathString;

    public JSONStorage() {
        this.directoryPathString = DEFAULT_DIRECTORY_PATH;
    }

    public JSONStorage(String directoryPathString) {
        this.directoryPathString = directoryPathString;
    }

    public void save(General general) throws SaveJSONStorageException {
        Path filePath = prepareFilePath(general);
        createDirectories(filePath.getParent());

        writeValueToFile(filePath, general);
    }

    private Path prepareFilePath(General g) {
        return Path.of(directoryPathString, g.getName() + FILE_EXTENSION);
    }

    private void createDirectories(Path p) throws SaveJSONStorageException {
        try {
            Files.createDirectories(Path.of(directoryPathString));

        } catch (IOException e) {
            throw new SaveJSONStorageException(
                "could not create directory: " + getExceptionMessage(e)
            );

        }
    }

    private void writeValueToFile(Path path, General general) throws SaveJSONStorageException {
        try (
            FileChannel    channel = openFileChannel(path);
            FileLock       lock    = channel.lock();
            BufferedWriter writer  = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8
            );

        ) {
            this.mapper.writeValue(writer, general);

        } catch (StreamReadException e) {
            throw new SaveJSONStorageException(
                "unable to parse object to be saved at " + path + ": " 
                + getExceptionMessage(e)
            );
    
        } catch (DatabindException e) {
            throw new SaveJSONStorageException(
                "unable to bind data to save at " + path + ": " 
                + getExceptionMessage(e)
            );

        } catch (IOException e) {
            throw new SaveJSONStorageException(
                "unable to write " + path + ": " + getExceptionMessage(e)
            );

        }
    }

    private FileChannel openFileChannel(Path path) throws IOException {
        return FileChannel.open(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void load(General general) throws LoadJSONStorageException {
        Path    filePath = prepareFilePath(general);
        General loaded   = readValueFromFile(filePath, general);

        general.setArmy(loaded.getArmy());
        general.setName(loaded.getName());
        general.setGold(loaded.getGold());
    }

    private General readValueFromFile(Path path, General general) throws LoadJSONStorageException {
        try (
            BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8
            );
        ) {
            return this.mapper.readValue(reader, General.class);

        } catch (StreamReadException e) {
            throw new LoadJSONStorageException(
                "unable to parse " + path + ": " 
                + getExceptionMessage(e)
            );
        
        } catch (DatabindException e) {
            throw new LoadJSONStorageException(
                "unable to bind data from " + path + ": " 
                + getExceptionMessage(e)
            );

        } catch (IOException e) {
            throw new LoadJSONStorageException(
                "unable to read " + path + ": " + 
                getExceptionMessage(e)
            );
        }
    }

    private String getExceptionMessage(Exception e) {
        return e.getClass().getName() + ": " + e.getMessage();
    }
}
