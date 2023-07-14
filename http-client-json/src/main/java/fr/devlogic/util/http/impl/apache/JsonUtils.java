package fr.devlogic.util.http.impl.apache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class JsonUtils {
    private static final Map<String, JavaType> CACHE = new HashMap<>();

    private JsonUtils() {
    }

    public static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper()
                    .findAndRegisterModules().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Pour la gestion de LocalDate & Co.
    public static final TypeFactory TYPE_FACTORY = OBJECT_MAPPER.getTypeFactory();

    public static JavaType getType(Type aType) {

        String typeName = aType.getTypeName();
        synchronized (CACHE) {
            CACHE.computeIfAbsent(typeName, n -> TYPE_FACTORY.constructType(aType));
            return CACHE.get(typeName);
        }
    }
}
