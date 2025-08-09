package wargames.factories;

import wargames.models.*;
import wargames.storage.*;

public class GeneralFactory {

    public General createGeneral(String name, int gold) {
        return new General(new Army(), name, gold, new JSONStorage());
    }

    public General createGeneral(String name, int gold, StorageStrategy storage) {
        return new General(new Army(), name, gold, storage);
    }

    public General createGeneral(Army army, String name, int gold, StorageStrategy storage) {
        return new General(army, name, gold, storage);
    }

}
