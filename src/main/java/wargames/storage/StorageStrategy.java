package wargames.storage;

import wargames.models.*;

public interface StorageStrategy {

    void load(General general);
    void save(General general);

}
