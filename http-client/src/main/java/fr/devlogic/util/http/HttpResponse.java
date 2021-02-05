package fr.devlogic.util.http;

import fr.devlogic.util.http.annotation.Nullable;
import fr.devlogic.util.http.exception.HttpRuntimeException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface HttpResponse {
    String header(String key);

    int status();

    boolean statusMatches(HttpResponseCode code);

    <T> T thenReturn(Function<HttpResponse, T> fct, @Nullable Function<HttpResponse, T> defaultValue);

    HttpResponse thenReturn(Function<HttpResponse, ?> fct);

    HttpResponse thenReturnContentAs(Class<?> c);

    HttpResponse thenReturnContentAs(Type t);

    <T> HttpResponse thenReturnContentAs(GenericType<T> t);

    <T> T orElseThrows(Supplier<RuntimeException> supplier);

    HttpResponse when(Predicate<HttpResponse> p);

    HttpResponse when(HttpResponseCode code);

    HttpResponse whenNot(HttpResponseCode code);

    HttpResponse when(MediaType mediaType);

    HttpResponse thenThrowException() throws HttpRuntimeException;

    <E extends RuntimeException> HttpResponse thenThrowException(Class<E> exceptionClass) throws RuntimeException;

    Predicate<HttpResponse> statusMatch(HttpResponseCode code);

    <T> T orElse(Supplier<T> supplier);

    <T> T orElse(Function<HttpResponse, T> supplier);

    InputStream getContentStream();

    @Nullable
    <C> C getContent(Class<C> c);

    @Nullable
    <C> C getContent(GenericType<C> p);

    @Nullable
    <C> C getContent(Type t);

    ContentType contentType();
}
