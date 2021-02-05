package fr.devlogic.util.http;

import java.util.Map;

/**
 * Paramètre d'une requête http. Le paramètre peut être un paramètre de chemin (pathParam) ou de requête (queryParam)
 */
public class RequestParam implements Map.Entry<String, Object> {
    private final String key;
    private final Object value;

    public RequestParam(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object setValue(Object value) {
        throw new UnsupportedOperationException();
    }
}
