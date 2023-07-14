package fr.devlogic.util.http.impl.apache;

import fr.devlogic.util.http.MediaTypeProcessor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public abstract class ApacheHttpMediaTypeProcessor implements MediaTypeProcessor {
    private final ThreadLocal<HttpEntity> httpEntityThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<MultipartEntityBuilder> multipartEntityBuilderThreadLocal = new ThreadLocal<>();

    public HttpEntity getHttpEntity() {
        HttpEntity httpEntity = httpEntityThreadLocal.get();
        httpEntityThreadLocal.remove();
        return httpEntity;
    }

    void setHttpEntity(HttpEntity entity) {
        httpEntityThreadLocal.set(entity);
    }

    public void setMultipartEntityBuilder(MultipartEntityBuilder multipartEntityBuilder) {
        multipartEntityBuilderThreadLocal.set(multipartEntityBuilder);
    }

    MultipartEntityBuilder getMultipartEntityBuilder() {
        MultipartEntityBuilder multipartEntityBuilder = multipartEntityBuilderThreadLocal.get();
        multipartEntityBuilderThreadLocal.remove();
        return multipartEntityBuilder;
    }
}
