package fr.devlogic.util.http;

import fr.devlogic.util.http.exception.HttpRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class HttpRequestTemplateFactory {
    private final List<Class<HttpRequestTemplate>> PROVIDERS;

    public HttpRequestTemplateFactory() {
        PROVIDERS = StreamSupport.stream(Spliterators.spliteratorUnknownSize(ServiceLoader.load(HttpRequestTemplate.class).iterator(), Spliterator.ORDERED), false)
                .map(Object::getClass)
                .map(c -> (Class<HttpRequestTemplate>) c)
                .collect(Collectors.toList());

    }

    public HttpRequestTemplate getObject() {
        try {
            return PROVIDERS.get(0).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new HttpRuntimeException(e);
        }
    }
}
