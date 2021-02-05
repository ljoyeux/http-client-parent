package fr.devlogic.util.http;

import fr.devlogic.util.http.part.Part;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

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

    HttpEntity writeContent(Object content, ContentType contentType);

    void writeContent(MultipartEntityBuilder multipartEntityBuilder, Part part);
}
