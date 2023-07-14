package fr.devlogic.util.http;

import fr.devlogic.util.http.annotation.Nullable;

import java.nio.charset.Charset;
import java.util.function.Consumer;

public interface HttpRequestTemplate {
    HttpRequestTemplate header(String key, String value);

    HttpRequestTemplate basicAuth(String username, String password);

    HttpRequestTemplate accept(MediaType... mediaType);

    HttpRequestTemplate contentType(MediaType mediaType);

    HttpRequestTemplate contentType(ContentType contentType);

    HttpRequestTemplate addRequestPreProcess(Consumer<? extends HttpRequestTemplate> process);

    HttpRequestTemplate defaultCharset(Charset defaultCharset);

    HttpRequestTemplate url(String url);

    HttpRequestTemplate pathParam(String key, Object value);

    HttpRequestTemplate pathParam(Object value);

    HttpRequestTemplate queryParam(String key, Object value);

    HttpRequestTemplate setClient(HttpRequestTemplateConfiguration httpRequestTemplateConfiguration);

    HttpResponse get();

    HttpResponse post(@Nullable Object content);

    HttpResponse put(@Nullable Object content);

    HttpResponse delete(@Nullable Object content);

    HttpResponse trace();

    HttpResponse get(String url);

    HttpResponse post(String url, Object content);

    HttpResponse put(String url, Object content);

    HttpResponse delete(String url, @Nullable Object content);

    HttpResponse trace(String url);

    String url();

    HttpRequestTemplateConfiguration configuration();
}
