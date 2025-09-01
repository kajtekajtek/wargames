package wargames.storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

// Thread-safe ObjectMapper wrapper with lazy loading
// https://refactoring.guru/design-patterns/singleton/java/example#example-2
public class Mapper {
    private static volatile ObjectMapper instance;

    public static ObjectMapper getInstance() {
        /* 
            The reason for using the local reference result is that
            in cases where instance is already initialized,
            the volatile field is only accessed once, which can improve the 
            method's overall performance by as much as 40%

            https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        */
        ObjectMapper result = instance;
        if (result != null) {
            return result;
        }
        synchronized(Mapper.class) {
            if (instance == null) {
                ObjectMapper newMapper = new ObjectMapper();
                configureMapper(newMapper);
                instance = newMapper;
            }
            return instance;
        }
    }

    private static void configureMapper(ObjectMapper om) {
        om.configure(DeserializationFeature
            .FAIL_ON_UNKNOWN_PROPERTIES, false
        );
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }
}
