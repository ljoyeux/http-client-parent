package fr.devlogic.util.http.spring;

import fr.devlogic.util.http.HttpRequestTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestTemplateFactory implements FactoryBean<HttpRequestTemplate> {
    private final fr.devlogic.util.http.HttpRequestTemplateFactory httpRequestTemplateFactory;

    public HttpRequestTemplateFactory() {
        httpRequestTemplateFactory = new fr.devlogic.util.http.HttpRequestTemplateFactory();
    }

    @Override
    public HttpRequestTemplate getObject() {
        return httpRequestTemplateFactory.getObject();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpRequestTemplate.class;
    }
}
