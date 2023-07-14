package fr.devlogic.util.http;

import fr.devlogic.util.http.part.Part;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Set;

public interface MediaTypeProcessor {

    Set<MediaType> handledMediaTypes();
    <T> T getContent(InputStream is, ContentType contentType, Class<T> c);
    <C> C getContent(InputStream is, ContentType contentType, Type t);

    default <C> C getContent(InputStream is, ContentType contentType, GenericType<C> t) {
        return getContent(is, contentType, t.getType());
    }

    void writeContent(Object content, ContentType contentType);

    void writeContent(Part part);
}
