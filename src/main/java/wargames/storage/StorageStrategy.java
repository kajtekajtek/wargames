package wargames.storage;

import wargames.models.General;

import wargames.exceptions.StorageExceptions.LoadStorageException;
import wargames.exceptions.StorageExceptions.SaveStorageException;

public interface StorageStrategy {

    void load(General g) throws LoadStorageException;
    void save(General g) throws SaveStorageException;

}
